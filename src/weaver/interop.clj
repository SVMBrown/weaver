(ns weaver.interop
  (:require
   [clojure.tools.logging :as log]
   [clojure.java.shell :refer [sh]]
   [clojure.java.io :as io]
   [clojure.data.json :as json]))

(defn warn-and-exit [& msgs]
  (doseq [msg msgs]
    (log/error msg))
  (System/exit 1))

(defn get-env
  ([key]
   (get-env (name key) nil))
  ([key not-found]
   (if-some [val (System/getenv (name key))]
     val
     not-found)))

(defn shell-exec
  ([command]
   (shell-exec
    command
    nil))
  ([command opts]
   (when (not-empty opts)
     (log/warn "Java runtime version of weaver doesn't support opts on shell-exec yet. ignoring..."))
   (let [{:keys [exit out err]} (sh/sh command)]
     (if (= exit 0)
       out
       (warn-and-exit (str "Non-zero exit code from shell-exec."
                           "\ncommand: " command
                           "\nexit-code: " exit
                           "\nstderr: " err))))))

(defn read-file [path]
  (some-> path
   (io/file)
   (slurp)))

(defn write-file [content path]
  (some-> path
   (io/file)
   (spit content)))

(defn pretty-json [obj]
  (with-out-str (json/pprint obj)))
