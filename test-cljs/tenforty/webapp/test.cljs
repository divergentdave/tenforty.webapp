(ns tenforty.webapp.test
  (:require [cljs.test :refer-macros [run-all-tests]]
            [tenforty.webapp.core-test]))

(enable-console-print!)

(defn ^:export run
  [callback]
  (defmethod cljs.test/report [:cljs.test/default :end-run-tests] [m]
    (callback (cljs.test/successful? m)))
  (run-all-tests #"tenforty\.webapp\..*-test"))
