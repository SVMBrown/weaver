(ns weaver.processors.multi)

;;NOTE: This ad-hoc dispatch injecting args via keyword name is probably ill advised as a general pattern, and should be refactored coerce to a vectorized version before calling to provide a nice shorthand without making dispatch dependent on type.

;;NOTE: Should prefix all abstract namespaces with :weaver... so that it can be used safely with arbitrary edn configs.

(defn process-node-dispatch [_ node]
  (cond
    (and (keyword? node) (namespace node))
    [:keyword (namespace node)]

    (and (vector? node) (keyword? (first node)) (namespace (first node)))
    [:vector (namespace (first node))]

    (and (map? node) (:preprocessor/id node))
    (:preprocessor/id node)

    :else
    node))

(defmulti process-node #'process-node-dispatch)

(defmethod process-node :default [_ node]
  node)
