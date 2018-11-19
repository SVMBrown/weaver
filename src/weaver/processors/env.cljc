(ns weaver.processors.env
  (:require
   [weaver.interop :as x]
   [weaver.processors.multi :refer [pre-process-node process-node]]))

(defmethod pre-process-node [:keyword "kw-env"] [node]
  {:weaver.processor/id :env/get!
   :name (name node)})

(defmethod pre-process-node [:vector "vec-env"] [[action lookup default :as node]]
  (case (name action)
    "get" {:weaver.processor/id :env/get
           :name lookup
           :default (or default nil)}
    "get!" {:weaver.processor/id :env/get!
            :name lookup}
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
    val
    (x/warn-and-exit
     (str "Error from node: " (or original node))
     (str "Variable " lookup " not found in environment."))))
