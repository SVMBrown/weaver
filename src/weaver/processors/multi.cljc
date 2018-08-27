(ns weaver.processors.multi
  (:require [weaver.interop :as x]))

(defn pre-process-node-dispatch [node]
  (cond
    (and (keyword? node) (namespace node))
    [:keyword (namespace node)]

    (and (vector? node) (keyword (first node)) (namespace (first node)))
    [:vector (namespace (first node))]

    :else
    node))

(defmulti pre-process-node #'pre-process-node-dispatch)

(defmethod pre-process-node :default [node]
  node)

(defn process-node-dispatch [_ node]
  (cond
    (or (and (keyword? node) (namespace node))
        (and (vector? node) (keyword? (first node)) (namespace (first node))))
    :pre-process

    (and (map? node) (:weaver.processor/id node))
    (:weaver.processor/id node)

    :else
    node))

(defmulti process-node #'process-node-dispatch)

;;TODO: Ensure that we log or error if a weaver id is unrecognized
;;TODO: Change dispatch to use explicit fallback, and change default to warn and exit

(defmethod process-node :default [_ node]
  (if-not (map? node)
    node
    (let [{processor :weaver.processor/id :or {processor ::no-op}} node]
      (if (= processor ::no-op)
        node
        (throw (ex-info (str "No processor found with id " processor)
                        {:node node}))))))

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
