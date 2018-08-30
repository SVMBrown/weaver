(ns weaver.processors.time
  (:require
   [weaver.interop :as x]
   [weaver.processors.multi :refer [pre-process-node process-node context-required-for-processor]]
   #?@(:clj [[clj-time.core :as t]
             [clj-time.coerce :as tc]
             [clj-time.format :as tf]])))

(defmethod process-node :time/from-long [_ {:keys [long]}]
  #?(:clj (tc/from-long long)
     :cljs (js/Date. long)))

(defmethod process-node :time/format [{{config-time-zone :time-zone
                                        config-format-string :format-string
                                        :or {config-time-zone "UTC"
                                             config-format-string "yyyy-MM-ddTHH:mm:ss.SSSZZZ"}} :time-config}
                                      {:keys [time time-zone format-string]}]
  #?(:clj
     (let [formatter (tf/formatter
                      (or format-string config-format-string)
                      (t/time-zone-for-id (or time-zone config-time-zone)))]
       (tf/unparse formatter time))

     :cljs
     (.toLocaleString time "en-CA" (clj->js {:time-zone (or time-zone config-time-zone)}))))
