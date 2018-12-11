(ns weaver.processor
  (:require
   [weaver.processors.multi :as multi :refer [pre-process-node context-required-for-processor]]
   [weaver.processors.all]
   [weaver.interop :as x]
   [clojure.walk :as walk]))

(defn process-node
  "Process a specific node"
  [ctx node]
  (try
    (multi/process-node ctx node)
    (catch #?(:clj Exception
              :cljs :default) e
      (x/warn-and-exit e
                      (str "Unhandled error while processing node: " node)))))

(defn pre-process
  "Pre-process syntax sugar such that the only remaining template nodes are in map form"
  [template]
  (walk/postwalk
   pre-process-node
   template))

(defn- required-context-preprocessed
  "impl. for weaver.processor/required-context"
  [template]
  (cond
    (map? template)
    (reduce
     (fn [acc [k v]]
       (into acc (required-context-preprocessed v)))
     ;; Check if current node has required context, and conjoin it with required context for its children
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
  "Given a template, pre-process it and compute the context keys required to fully process the template"
  [template]
  (required-context-preprocessed
   (pre-process template)))


(defn process-obj
  "Process an object using a pre-processed context."
  [ctx obj]
  (walk/postwalk
   (partial process-node ctx)
   obj))

(defn process-config
  "Process the config map with the given context.
   Error if config has circular dependency."
  [ctx config]
  (if (contains? (required-context config) :config)
    (x/warn-and-exit config "Config template cannot depend on config context!")
    (process-obj ctx config)))

(defn process-context
  "Processes the provided context."
  [{:keys [config] :as ctx}]
  (let [processed-config (process-config (dissoc ctx :config) config)
        context (assoc ctx :config processed-config)]
    context))

(defn gen-process-fn
  "Generates a processor function from a provided context.
   Should only be used if the same context will be used for many templates."
  [ctx]
  (partial process-obj (process-context ctx)))

(defn process
  "Processes the provided context and uses it to process the provided template.
  If you are using the same config for many templates, consider using gen-process-fn on your context map instead"
  [ctx template]
  ((gen-process-fn ctx) template))

