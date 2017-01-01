(ns tenforty.webapp.core-test
  (:require [cljs.test :refer [deftest
                               testing
                               is]
             :include-macros true]
            [tenforty.webapp.core]))

(deftest init-test
  (testing "Webapp initialization"
    (tenforty.webapp.core/init)
    (is (not= 0 (aget (.querySelectorAll js/document "g.node") "length")))
    (is (not= 0 (aget (.querySelectorAll js/document "g.edgePath") "length")))))
