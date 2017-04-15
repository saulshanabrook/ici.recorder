(ns ici-recorder.parquet.write-test
  (:require [ici-recorder.parquet.write :refer [write ->hadoop-config]]
            [ici-recorder.test-utils :as test-utils]

            [clojure.test :as t]
            [clojure.spec.gen :as gen]
            [clojure.spec.test :as stest]))

(-> 'ici-recorder.parquet.add-data
  stest/enumerate-namespace
  stest/instrument)

(-> 'ici-recorder.parquet.write
  stest/enumerate-namespace
  stest/instrument)


(defn ->configuraiton [path]
  {:path (org.apache.hadoop.fs.Path. path)
   :write-mode "OVERWRITE"
   :validation true
   :compression-codec "SNAPPY"
   :hadoop-config (->hadoop-config {})})

(t/deftest test-write
  (t/testing "configuration"
    (write
      test-utils/p-configuration
      (gen/generate test-utils/configuration-gen)
      (->configuraiton "config")))
  (t/testing "generation"
    (write
      test-utils/p-generation
      (gen/generate test-utils/generation-gen)
      (->configuraiton "generation"))))
