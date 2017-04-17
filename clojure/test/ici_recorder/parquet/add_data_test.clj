(ns ici-recorder.parquet.add-data-test
  (:require [ici-recorder.parquet.add-data :refer [add-record-forms]]
            [ici-recorder.test-utils :as test-utils]

            [clojure.test :as t]
            [clojure.spec.gen :as gen]
            [clojure.spec.test :as stest]))

(-> 'ici-recorder.parquet.add-data
  stest/enumerate-namespace
  stest/instrument)

(t/deftest test-add-record-forms
  (t/testing "configuration"
    (t/is
      (add-record-forms
        test-utils/p-configuration
        'record)))
  (t/testing "generation"
    (t/is
      (add-record-forms
        test-utils/p-generation
        'record))))
