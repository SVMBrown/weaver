(ns weaver.interop
  (:require
   [clojure.string :as string]
   ["fs" :as fs]))


(def ^:dynamic *exit-fn*
  (fn []
    (js/process.exit 1)))

(def error-log js/console.error)
(def warn-log js/console.warn)
(def info-log js/console.info)
(def debug-log js/console.debug)

(defn- convert-and-error-log [arg]
  (js/console.error
   (if (or (string? arg) (= (type arg) js/Error))
     arg
     (clj->js arg))))

(defn warn-and-exit [& msgs]
  (doseq [msg msgs]
    (convert-and-error-log msg))
  (*exit-fn*))

(defn get-env
  ([key]
   (get-env (name key) nil))
  ([key not-found]
   (if-some [result (aget (.-env js/process) (name key))]
     result
     not-found)))

(def ^:private default-shell-opts {:timeout 60000
                                   :encoding "UTF-8"})

(defonce child-process (js/require "child_process"))

(defn shell-exec
  ([command]
   (shell-exec
    command
    {}))
  ([command opts]
   (try
     (child-process.execSync
      command
      (clj->js (merge default-shell-opts opts)))
     (catch js/Error e
       (warn-and-exit e
                      (str
                       "An error occurred while trying to evaluate shell expression: "
                       command
                       " With Opts "
                       (merge default-shell-opts opts)))))))

(defn read-file [path]
  (fs/readFileSync path #js {:encoding "UTF-8"}))

(defn write-file [content path]
  (fs/writeFileSync content path))

(defn pretty-json [obj]
  (-> obj
      (clj->js)
      (js/JSON.stringify nil 2)))
