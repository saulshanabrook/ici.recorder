(ns ici-recorder.clojush-test
  (:require [ici-recorder.clojush :refer [record-run, record-generation]]
            [clojure.test :as t]
            [ici-recorder.test-utils :as test-utils]
            [clojure.spec.gen :as gen]
            [clojure.spec.test]
            [clojure.spec.test :as stest]))



(-> 'ici-recorder.parquet.add-data
  stest/enumerate-namespace
  stest/instrument)

(-> 'ici-recorder.parquet.write
  stest/enumerate-namespace
  stest/instrument)

(-> 'ici-recorder.parquet.write-support
  stest/enumerate-namespace
  stest/instrument)

(-> 'ici-recorder.clojush
  stest/enumerate-namespace
  stest/instrument)


(t/deftest test-record-clojush
      (t/testing "configuration"
        (record-run
          test-utils/configuration-write-support
          "test-uuid"
          (gen/generate test-utils/configuration-gen)))

      (t/testing "generation"
        (record-generation
          test-utils/generation-write-support
          "test-uuid"
          0
          (gen/generate test-utils/generation-gen))))
