(defproject tenforty.webapp "0.1.0-SNAPSHOT"
  :description "Webapp for analyzing U.S. taxes"
  :url "https://github.com/divergentdave/tenforty.webapp"
  :license {:name "GNU GPL v2"
            :url "https://www.gnu.org/licenses/gpl-2.0.en.html"}
  :source-paths ["src-cljs"]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.293"]
                 [tenforty "0.1.0-SNAPSHOT"]]
  :plugins [[lein-cljsbuild "1.1.5"]]
  :cljsbuild {:builds {:dev {:source-paths ["src-cljs"]
                             :compiler {:main tenforty.webapp.core
                                        :asset-path "js/compiled/out"
                                        :output-to "resources/public/js/compiled/tenforty.webapp.js"
                                        :output-dir "resources/public/js/compiled/out"
                                        :optimizations :whitespace
                                        :pretty-print true
                                        :source-map "resources/public/js/compiled/tenforty.webapp.js.map"}}
                       :prod {:source-paths ["src-cljs"]
                              :compiler {:main tenforty.webapp.core
                                         :asset-path "js/compiled/out-min"
                                         :output-to "resources/public/js/compiled/tenforty.webapp-min.js"
                                         :output-dir "resources/public/js/compiled/out-min"
                                         :optimizations :advanced
                                         :externs ["resources/public/js/externs.js"]
                                         :pretty-print false
                                         :source-map "resources/public/js/compiled/tenforty.webapp-min.js.map"}}
                       :test {:source-paths ["src-cljs" "test-cljs"]
                              :compiler {:asset-path "js/compiled/out-test"
                                         :output-to "resources/test/js/compiled/test.js"
                                         :output-dir "resources/test/js/compiled/out-test"
                                         :optimizations :whitespace
                                         :pretty-print true
                                         :source-map "resources/test/js/compiled/test.js.map"}}
                       :test-min {:source-paths ["src-cljs" "test-cljs"]
                                  :compiler {:asset-path "js/compiled/out-test-min"
                                             :output-to "resources/test/js/compiled/test-min.js"
                                             :output-dir "resources/test/js/compiled/out-test-min"
                                             :optimizations :advanced
                                             :externs ["resources/public/js/externs.js"]
                                             :pretty-print true
                                             :source-map "resources/test/js/compiled/test-min.js.map"}}}
              :test-commands {"unit" ["phantomjs"
                                      "resources/test/test.js"
                                      "resources/test/test.html"]
                              "unit-min" ["phantomjs"
                                          "resources/test/test.js"
                                          "resources/test/test-min.html"]}}
  :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                    "resources/test/js/compiled"
                                    :target-path]
  :profiles {:dev {:plugins [[lein-cljfmt "0.5.6"]]}})
