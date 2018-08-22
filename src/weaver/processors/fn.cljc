(ns weaver.processors.fn
  (:require
   [weaver.interop :as x]
   [weaver.processors.multi :refer [process-node]]))

(def processor-fns #:fn{:str str})

(defmethod process-node [:vector "fn"] [{:keys [function-lookup]} [fn-kw & args :as node]]
  (if-let [processor (get (merge processor-fns function-lookup) fn-kw)]
    (apply processor args)
    (x/warn-and-exit (str
                      "Key: " fn-kw
                      " in node: " node
                      " is not allowed. If this should be allowed, please provide a function-lookup map in the processing context."))))
