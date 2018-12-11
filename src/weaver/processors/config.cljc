(ns weaver.processors.config
  (:require
   [weaver.interop :as x]
   [weaver.processors.multi :refer [pre-process-node process-node context-required-for-processor]]))

;; PROCESSING

(defmethod context-required-for-processor :config/get-in [_]
  #{:config})

(defmethod process-node :config/get-in [{:keys [config]} {path :path
                                                          default :default}]
  (get-in config path default))

(defmethod context-required-for-processor :config/get-in! [_]
  #{:config})

(defmethod process-node :config/get-in! [{:keys [config]} {path :path
                                                           original :weaver.processor/original
                                                           :as node}]
  (if-some [result (get-in config path)]
    result
    (x/warn-and-exit
     (str "Error from node: " (or original node))
     (str "Value not found in config at path: " path ".")
     (str
      " if this should be nilable, please use [:config/get-in " path "] "
      "or [:config/get-in " path " <default-value>]"))))

;; PRE-PROCESSING

(def vector-transformers
  {"get" (fn [[_ lookup default]]
           {:weaver.processor/id :config/get-in
            :path [lookup]
            :default default})
   "get-in" (fn [[_ lookup default]]
              {:weaver.processor/id :config/get-in
               :path lookup
               :default default})
   "get!" (fn [[_ lookup]]
            {:weaver.processor/id :config/get-in!
             :path [lookup]})
   "get-in!" (fn [[_ lookup]]
               {:weaver.processor/id :config/get-in!
                :path lookup})})

(defmethod pre-process-node [:vector "config"] [[action lookup default :as node]]
  (let [transformer (get vector-transformers (name action)
                         #(x/warn-and-exit
                           (str "Unrecognized environment node type: " node)))]
    (transformer node)))

(defmethod pre-process-node [:keyword "config"] [node]
  (if (contains? vector-transformers (name node))
    node
    {:weaver.processor/id :config/get-in!
     :path [(keyword (name node))]}))
