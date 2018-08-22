(ns weaver.processors.config
  (:require
   [weaver.interop :as x]
   [weaver.processors.multi :refer [process-node]]))

(defmethod process-node [:keyword "config"] [{:keys [config]} node]
  (if-some [result (get config (keyword (name node)))]
    result
    (x/warn-and-exit (str
                      "Key: " (keyword (name node))
                      " not found in config."
                      " if this should be nilable, please use [:config/get " (keyword (name node)) "] "
                      "or [:config/get " (keyword (name node)) " <default-value>]"))))


(defmethod process-node [:vector "config"] [{:keys [config]} [action lookup default :as node]]
  (case (name action)
    "get" (get config lookup default)
    "get!" (if-some [val (get config lookup)]
             lookup
             (x/warn-and-exit
              (str "Error from node: " node)
              (str "Key " lookup " not found in config.")))
    "get-in" (get-in config lookup default)
    "get-in!" (if-some [val (get-in config lookup)]
                lookup
                (x/warn-and-exit
                 (str "Error from node: " node)
                 (str "Key " lookup " not found in config.")))
    (x/warn-and-exit
     (str "Unrecognized environment node type: " node))))
