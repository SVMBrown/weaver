(ns user
 #_(:require [figwheel-sidecar.repl-api :as ra]))

#_#_#_(defn start-fw []
 (ra/start-figwheel!))

(defn stop-fw []
 (ra/stop-figwheel!))

(defn cljs []
 (ra/cljs-repl))
