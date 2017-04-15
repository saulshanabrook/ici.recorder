(ns ici-recorder.parquet.schema.interop-test
  (:require [ici-recorder.parquet.schema.interop :refer [->schema]]
            [ici-recorder.test-utils :as test-utils]
            [clojure.test :as t]))



(t/deftest test-sample-schema
  (t/testing "configuraiton"
    (t/is (->schema test-utils/p-configuration))
    (t/testing "generation"
      (t/is (->schema test-utils/p-generation)))))
