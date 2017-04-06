(ns ici-recorder.clojush-test
  (:require [ici-recorder.clojush :refer [record-run]]
            
            [clojure.test :as t]))



(t/deftest test-record-run
  (t/testing "configuration"
    (record-run
      {:name [true :string]}
      "uuid"
      {:name "hi"})))
