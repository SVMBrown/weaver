(ns weaver.processors.multi
  (:require [weaver.interop :as x]))


(defn process-node-dispatch [_ node]
  (cond
    (map-entry? node)
    ::no-op

    (or (qualified-keyword? node)
        (and (vector? node) (qualified-keyword? (first node))))
    :pre-process

    (and (map? node) (:weaver.processor/id node))
    (:weaver.processor/id node)

    :else
    ::no-op))

(defmulti process-node #'process-node-dispatch)

;;TODO: Ensure that we log or error if a weaver id is unrecognized
;;TODO: Change dispatch to use explicit fallback, and change default to warn and exit

(defmethod process-node :default [_ node]
  (if (and (map? node) (:weaver.processor/id node))
    (throw (ex-info (str "No processor found with id " processor)
                    {:node node}))
    node))

(defmethod process-node ::no-op [_ node]
  node)


(defn pre-process-node-dispatch [node]
  (cond
    ;; Skip map-entries and entities that have a matching method
    (or (map-entry? node) (contains? (methods process-node) node))
    ;; NOTE: `methods` might be expensive, could manage this manually (as before)
    ::no-op

    (qualified-keyword? node)
    [:keyword (namespace node)]

    (and (vector? node) (qualified-keyword? (first node)))
    [:vector (namespace (first node))]

    :else
    ::no-op))

(defmulti pre-process-node #'pre-process-node-dispatch)

(defmethod pre-process-node :default [node]
  node)

(defmethod pre-process-node ::no-op [node]
  node)

(defmethod process-node :pre-process [ctx node]
  (let [pre-processed (pre-process-node node)]
    (cond
      (map? pre-processed)
      (process-node ctx
                    (assoc pre-processed :weaver.processor/original node))

      (= node pre-processed)
      node

      :else (process-node pre-processed))))

(defmulti context-required-for-processor :weaver.processor/id)

(defmethod context-required-for-processor :default [node]
  #{})
