(ns ^:figwheel-no-load weaver.dev
  (:require
    [weaver.core :as core]
    #_[devtools.core :as devtools]))


(enable-console-print!)

#_(devtools/install!)

(core/init!)
