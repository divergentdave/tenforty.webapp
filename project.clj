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
                             :compiler {:output-to "resources/public/js/cljs.js"
                                        :output-dir "resources/public/js"
                                        :optimizations :whitespace
                                        :pretty-print true
                                        :source-map "resources/public/js/cljs.js.map"}}
                       :prod {:source-paths ["src-cljs"]
                              :compiler {:output-to "resources/public/js-min/cljs-min.js"
                                         :output-dir "resources/public/js-min"
                                         :optimizations :advanced
                                         :pretty-print false
                                         :source-map "resources/public/js-min/cljs-min.js.map"}}}}
  :profiles {:dev {:plugins [[lein-cljfmt "0.5.6"]]}})
