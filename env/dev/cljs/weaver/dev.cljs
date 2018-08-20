(ns ^:figwheel-no-load weaver.dev
  (:require
    [weaver.core :as core]
    [devtools.core :as devtools]))


(enable-console-print!)

(devtools/install!)

(core/init!)
