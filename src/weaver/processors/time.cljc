(ns weaver.processors.time
  (:require
   [weaver.interop :as x]
   [weaver.processors.multi :refer [pre-process-node process-node context-required-for-processor]]
   #?@(:clj [[clj-time.core :as t]
             [clj-time.coerce :as tc]
             [clj-time.format :as tf]]
       :cljs [[cljs-time.core :as t]
              [cljs-time.coerce :as tc]
              [cljs-time.format :as tf]])))

(defmethod process-node :time/from-long [_ {:keys [long]}]
  (tc/from-long long))

(defmethod process-node :time/format [{{config-time-zone :time-zone
                                        config-format-string :format-string
                                        :or {config-time-zone "UTC"
                                             config-format-string "yyyy-MM-ddTHH:mm:ss.SSSZZZ"}} :time-config}
                                      {:keys [time time-zone format-string]}]
  (let [formatter (tf/formatter
                   (or format-string config-format-string)
                   (t/time-zone-for-id (or time-zone config-time-zone)))]
    (tf/unparse formatter time)))
