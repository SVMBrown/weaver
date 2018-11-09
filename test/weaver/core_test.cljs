(ns weaver.core-test
  (:require [weaver.core :as c]
            [cljs.test :as t :include-macros true]))

(t/deftest empty-maps
  (t/is (= {} (c/process {} {}))))
