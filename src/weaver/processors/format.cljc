(ns weaver.processors.format
  (:require
   [weaver.interop :as x]
   [weaver.processors.multi :refer [pre-process-node process-node]]
   [clojure.pprint :as pp]))

(defmethod pre-process-node [:vector "format"] [[kw & args]]
  (case kw
    :format/cl-format {:weaver.processor/id :format/cl-format
                       :string (first args)
                       :args (rest args)}
    {:weaver.processor/id kw
     :args args}))

(defmethod process-node :format/cl-format [_ {:keys [string args] :as node}]
  (cond
    (not (string? string)) (x/warn-and-exit (ex-info "String argument to :format/cl-format must be a string"
                                                     {:node node}))
    (not (coll? args)) (x/warn-and-exit (ex-info ":args argument to :format/cl-format must be a collection"
                                                 {:node node}))
    :else (apply pp/cl-format false string args)))
