(ns ici-recorder.clojush-test
  (:require [ici-recorder.clojush]
            [clojure.test :as t]))

(t/deftest record
  (t/testing "configuration"
    (t/is (not (ici-recorder.clojush/record-run "hi" {:hi 1})))))
