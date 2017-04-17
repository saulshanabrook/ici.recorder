(ns ici-recorder.parquet.write-support-test
  (:require [ici-recorder.parquet.write-support :refer [->write-support]]
            [ici-recorder.test-utils :as test-utils]

            [clojure.test :as t]
            [clojure.spec.gen :as gen]
            [clojure.spec.test :as stest]))

(-> 'ici-recorder.parquet.add-data
  stest/enumerate-namespace
  stest/instrument)

(-> 'ici-recorder.parquet.write-support
  stest/enumerate-namespace
  stest/instrument)

(t/deftest test-->write-support
  (t/testing "configuration"
    (t/is
      (->write-support
        test-utils/p-configuration)))
  (t/testing "generation"
    (t/is
      (->write-support
        test-utils/p-generation))))
