(defproject weaver "0.1.6-SNAPSHOT"
  :description "Configuration templating DSL"
  :url "https://github.com/SVMBrown/weaver"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.339"]
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [clj-time "0.14.4"]]

  :min-lein-version "2.7.1"

  :source-paths ["src"]

  :aliases {"fig"       ["trampoline" "run" "-m" "figwheel.main"]
            "fig:build" ["trampoline" "run" "-m" "figwheel.main" "-b" "dev" "-r"]
            "fig:min"   ["run" "-m" "figwheel.main" "-O" "advanced" "-bo" "dev"]
            "fig:test"  ["run" "-m" "figwheel.main" "-co" "test.cljs.edn" "-m" weaver.test-runner]}

  :profiles {:dev {:source-paths ["src" "env/dev/clj"]
                   :resource-paths ["target"]
                   :dependencies [[com.bhauman/figwheel-main "0.1.9"]
                                  [com.bhauman/rebel-readline-cljs "0.1.4"]]
                   :clean-targets ^{:protect false} ["target"]}})
