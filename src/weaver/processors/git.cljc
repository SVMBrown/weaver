(ns weaver.processors.git
  (:require
   [clojure.string :as string]
   [weaver.interop :as x]
   [weaver.processors.multi :refer [process-node pre-process-node]]))


(defn short-hash []
  (string/trim (x/shell-exec "git rev-parse --short HEAD")))

(defmethod process-node :git/short-hash [_ _]
  (short-hash))

(defmethod pre-process-node [:keyword "git"] [node]
  (case (name node)
    "short-hash" {:weaver.processor/id :git/short-hash}
    (x/warn-and-exit (str "Unrecognized git keyword processor: " node))))
