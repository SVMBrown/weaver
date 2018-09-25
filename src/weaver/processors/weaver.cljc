(ns weaver.processors.weaver
  (:require
   [weaver.interop :as x]
   [weaver.processors.multi :refer [pre-process-node process-node context-required-for-processor]]))

(defmulti pre-process-weaver-vec #'first)

(defmethod pre-process-weaver-vec :default [node]
  (throw (ex-info (str "Unrecognized node: " node)
                  {:node node})))

(defmulti pre-process-weaver-kw #'identity)

(defmethod pre-process-weaver-kw :default [node]
  node)

(defmethod pre-process-node [:vector "weaver"] [node]
  (pre-process-weaver-vec node))

(defmethod pre-process-node [:keyword "weaver"] [node]
  (pre-process-weaver-kw node))

(defmethod pre-process-weaver-vec :weaver/drop-nils [[_ coll]]
  {:weaver.processor/id :weaver/drop-nils
   :coll coll})

(defmethod process-node :weaver/drop-nils [_ {:keys [coll]}]
  (let [result (remove nil? coll)]
    (cond
      (vector? coll) (vec result)
      (seq? coll) result
      :else (do (x/warn-log "Unrecognized collection type, returning vec")
                (vec result)))))

(defmethod pre-process-weaver-vec :weaver/if [[_ pred pass fail]]
  {:weaver.processor/id :weaver/if
   :pred pred
   :if pass
   :else fail})

(defmethod process-node :weaver/if [_ {pred :pred
                                       pass :if
                                       fail :else}]
  (if pred
    pass
    fail))

(defmethod pre-process-weaver-vec :weaver/when [[_ pred val]]
  {:weaver.processor/id :weaver/when
   :pred pred
   :val  val})

(defmethod process-node :weaver/when [_ {pred :pred
                                         val :val}]
  (when pred
    val))

(defmethod pre-process-weaver-vec :weaver/cond [[_ & args]]
  (if (odd? (count args))
    (throw (ex-info ":weaver/cond must have an even number of args."
                    {:args args}))
    {:weaver.processor/id :weaver/cond
     :clauses (partition 2 args)}))

(defmethod process-node :weaver/cond [_ {:keys [clauses]}]
  (let [result (first (drop-while (fn [[pred _]] (not pred)) clauses))]
    (when result
      (second result))))

(defmethod pre-process-weaver-vec :weaver/and [[_ & args]]
  {:weaver.processor/id :weaver/and
   :args args})

(defmethod process-node :weaver/and [_ {:keys [args]}]
  (every? identity args))

(defmethod pre-process-weaver-vec :weaver/or [[_ & args]]
  {:weaver.processor/id :weaver/or
   :args args})

(defmethod process-node :weaver/or [_ {:keys [args]}]
  (some identity args))
