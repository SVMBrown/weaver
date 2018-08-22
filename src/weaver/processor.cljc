(ns weaver.processor
  (:require
   [weaver.processors.multi :refer [process-node]]
   [weaver.processors.all]
   [weaver.interop :as x]
   [clojure.walk :as walk]))

(defn config-process-node [ctx node]
  (cond
    (and (keyword? node) (#{"config"} (namespace node)))
    (x/warn-and-exit "Cannot use config processors inside of a config file" "Offending Node: " node)

    :else
    (process-node ctx node)))

(defn process-config [ctx config]
  (walk/postwalk
   (partial config-process-node ctx)
   config))

(defn process-obj [ctx obj]
  (walk/postwalk
   (partial process-node ctx)
   obj))

(defn gen-process-fn [ctx]
  (let [ctx (update ctx :config #(process-config (dissoc ctx :config) %))]
    (partial process-obj ctx)))

(defn process
  "Processes the provided context and uses it to process the provided object.
  If you are using the same config for many objects, consider using gen-process-fn on your context map instead"
  [ctx obj]
  ((gen-process-fn ctx) obj))
