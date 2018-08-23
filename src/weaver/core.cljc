(ns weaver.core
  (:require
   [weaver.processor :as processor]))

(def required-context
  "[template]
  Checks the required context keys for a template."
  processor/required-context)

(def process-config
  "[ctx config]
  Uses the same processor used on objects to pre-process a config map.
  Config map is used in :config/... processors"
  processor/process-config)

(def process-template
  "[ctx template]
  Expects a pre-processed processing context.
  Uses provided context to process the passed template"
  processor/process-obj)

(def gen-process-fn
  "[ctx]
  Returns a processor function based on the provided processing context.
  Currently, it pre-processes the config map and closes over the result."
  processor/gen-process-fn)

(def process
  "[ctx template]
  pre-processes the provided context and uses it to process the passed template"
  processor/process)
