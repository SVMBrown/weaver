(ns weaver.processors.git
  (:require
   [clojure.string :as string]
   [weaver.interop :as x]
   [weaver.processors.multi :refer [process-node]]))


(defn short-hash []
  (string/trim (x/shell-exec "git rev-parse --short HEAD")))

(defmethod process-node [:keyword "git"] [_ node]
  (case (name node)
    "short-hash" (short-hash)
    (x/warn-and-exit (str "Unrecognized git processor: " node))))
