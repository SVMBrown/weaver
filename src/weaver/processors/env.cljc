(ns weaver.processors.env
  (:require
   [weaver.interop :as x]
   [weaver.processors.multi :refer [process-node]]))

(defmethod process-node [:keyword "env"] [_ node]
  (if-some [val (x/get-env (name node))]
    val
    (x/warn-and-exit
     (str "Variable " (name node) " not found in environment.")
     (str "If this variable is optional, please use: [:env/get " (name node) "] or [:env/get " (name node) " <default-value>] instead of " node "."))))

(defmethod process-node [:vector "env"] [_ [action lookup default :as node]]
  (case (name action)
    "get" (x/get-env lookup default)
    "get!" (if-some [val (x/get-env lookup)]
             lookup
             (x/warn-and-exit
              (str "Error from node: " node)
              (str "Variable " lookup " not found in environment.")))
    (x/warn-and-exit
     (str "Unrecognized environment node type: " node))))
