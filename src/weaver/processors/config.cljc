(ns weaver.processors.config
  (:require
   [weaver.interop :as x]
   [weaver.processors.multi :refer [pre-process-node process-node context-required-for-processor]]))

(defmethod pre-process-node [:keyword "config"] [node]
  {:weaver.processor/id :config/get-in!
   :path [(keyword (name node))]})


(defmethod pre-process-node [:vector "config"] [[action lookup default :as node]]
  (case (name action)
    "get" {:weaver.processor/id :config/get-in
           :path [lookup]
           :default default}

    "get-in" {:weaver.processor/id :config/get-in
              :path lookup
              :default default}

    "get!" {:weaver.processor/id :config/get-in!
            :path [lookup]}

    "get-in!" {:weaver.processor/id :config/get-in
               :path lookup}

    (x/warn-and-exit
     (str "Unrecognized environment node type: " node))))

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
