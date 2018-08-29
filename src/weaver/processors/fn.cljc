(ns weaver.processors.fn
  (:require
   [weaver.interop :as x]
   [weaver.processors.multi :refer [pre-process-node process-node]]))

(def default-lookup #:fn{:str str
                         := =
                         :< <
                         :<= <=
                         :> >
                         :>= >=})

(defmethod pre-process-node [:vector "fn"] [[fn-kw & args :as node]]
  {:weaver.processor/id :fn/call
   ::id fn-kw
   :args args})

(defmethod process-node :fn/call [{:keys [function-lookup]}
                                  {fn-kw ::id
                                   args :args
                                   original :weaver.processor/original
                                   :as node}]
  (if-let [function (get (merge default-lookup function-lookup) fn-kw)]
    (apply function args)
    (x/warn-and-exit (str
                      "Function id: " fn-kw
                      " in node: " (or original node)
                      " is not allowed. If this should be allowed, please provide a function-lookup map in the processing context."))))
