(ns weaver.core
  (:require
   [weaver.processor :as processor]))

(def process-config
  "[ctx config]
  Uses the same processor used on objects to pre-process a config map.
  Config map is used in :config/... processors"
  processor/process-config)

(def process-obj
  "[ctx obj]
  Expects a pre-processed processing context.
  Uses provided context to process the passed object"
  processor/process-obj)

(def gen-process-fn
  "[ctx]
  Returns a processor function based on the provided processing context.
  Currently, it pre-processes the config map and closes over the result."
  processor/gen-process-fn)

(def process
  "[ctx obj]
  pre-processes the provided context and uses it to process the passed object"
  processor/process)
