(ns weaver.processors.env
  (:require
   [weaver.interop :as x]
   [weaver.processors.multi :refer [pre-process-node process-node]]))

(defmethod pre-process-node [:keyword "env"] [node]
  {:weaver.processor/id :env/get!
   :lookup (name node)})

(defmethod pre-process-node [:vector "env"] [[action lookup default :as node]]
  (case (name action)
    "get" {:weaver.processor/id :env/get
           :lookup lookup
           :default (or default nil)}
    "get!" {:weaver.processor/id :env/get!
            :lookup lookup}
    (x/warn-and-exit
     (str "Unrecognized environment node type: " node))))

(defmethod process-node :env/get [_ {lookup :name
                                     default :default
                                     :or {default nil}}]
  (x/get-env (name lookup) default))

(defmethod process-node :env/get! [_ {lookup :name
                                      original :weaver.processor/original
                                      :as node}]
  (if-some [val (x/get-env (name lookup))]
    lookup
    (x/warn-and-exit
     (str "Error from node: " (or original node))
     (str "Variable " lookup " not found in environment."))))
