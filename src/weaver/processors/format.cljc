(ns weaver.processors.format
  (:require
   [weaver.interop :as x]
   [weaver.processors.multi :refer [pre-process-node process-node]]
   [clojure.pprint :as pp]))

(defmethod process-node :format/cl-format [_ {:keys [string args] :as node}]
  (cond
    (not (string? string)) (x/warn-and-exit (ex-info "String argument to :format/cl-format must be a string"
                                                     {:node node}))
    (not (coll? args)) (x/warn-and-exit (ex-info ":args argument to :format/cl-format must be a collection"
                                                 {:node node}))
    :else (apply pp/cl-format false string args)))
