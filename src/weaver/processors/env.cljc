(ns weaver.processors.env
  (:require
   [weaver.interop :as x]
   [weaver.processors.multi :refer [pre-process-node process-node]]))

;; PROCESS
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

;; PRE-PROCESS

(def vector-transformers
  {"get" (fn [[_ lookup default]]
           {:weaver.processor/id :env/get
            :name lookup
            :default default})
   "get!" (fn [[_ lookup]]
            {:weaver.processor/id :env/get!
             :name lookup})})

(defmethod pre-process-node [:vector "vec-env"] [[action lookup default :as node]]
  ((get vector-transformers (name action)
        (fn [node]
          (x/warn-and-exit
           (str "Unrecognized environment node type: " node))))
   node))

(defmethod pre-process-node [:vector "env"] [[action lookup default :as node]]
  ((get vector-transformers (name action)
        (fn [node]
          (x/warn-and-exit
           (str "Unrecognized environment node type: " node))))
   node))

(defmethod pre-process-node [:keyword "kw-env"] [node]
  {:weaver.processor/id :env/get!
   :name (name node)})

(defmethod pre-process-node [:keyword "env"] [node]
  (if (contains? vector-transformers (name node))
    node
    {:weaver.processor/id :env/get!
     :name (name node)}))

