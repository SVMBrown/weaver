(ns weaver.processor
  (:require
   [weaver.processors.multi :as multi :refer [pre-process-node context-required-for-processor]]
   [weaver.processors.all]
   [weaver.interop :as x]
   [clojure.walk :as walk]))

(defn process-node [ctx node]
  (try
    (multi/process-node ctx node)
    (catch #?(:clj Exception
              :cljs :default) e
      (x/warn-and-exit e
                      (str "Unhandled error while processing node: " node))
      (throw e))))

(defn pre-process
  [template]
  (walk/postwalk
   pre-process-node
   template))

(defn- required-context-preprocessed
  [template]
  (cond
    (map? template)
    (reduce
     (fn [acc [k v]]
       (into acc (required-context-preprocessed v)))
     (if (contains? template :weaver.processor/id)
       (context-required-for-processor template)
       #{})
     template)

    (coll? template)
    (reduce
     (fn [acc v]
       (into acc (required-context-preprocessed v)))
     #{}
     template)

    :else
    #{}))

(defn required-context
  [template]
  (required-context-preprocessed
   (pre-process template)))


(defn process-obj [ctx obj]
  (walk/postwalk
   (partial process-node ctx)
   obj))

(defn process-config [ctx config]
  (if (contains? (required-context config) :config)
    (x/warn-and-exit config "Config template cannot depend on config context!")
    (process-obj ctx config)))

(defn gen-process-fn [ctx]
  (let [ctx (update ctx :config #(process-config (dissoc ctx :config) %))]
    (partial process-obj ctx)))

(defn process
  "Processes the provided context and uses it to process the provided template.
  If you are using the same config for many templates, consider using gen-process-fn on your context map instead"
  [ctx template]
  ((gen-process-fn ctx) template))

