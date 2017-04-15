(ns ici-recorder.clojush-test
  (:require [ici-recorder.clojush :refer [record-run, record-generation]]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.profiling :as profiling]
            [clojure.test :as t]
            [ici-recorder.test-utils :as test-utils]
            [clojure.spec.gen :as gen]
            [clojure.spec.test]))



(t/deftest test-record-basic
  (t/testing "run"
    (record-run
      {:name [true :string]}
      "uuid"
      {:name "hi"})))

(t/deftest test-record-profile
  (clojure.spec.test/with-instrument-disabled
    ; (timbre/with-level :debug
      ; (profiling/profile :debug "record-run"
      ;   (t/testing "configuration"
      ;     (record-run
      ;       test-utils/p-configuration
      ;       "test-uuid"
      ;       (gen/generate test-utils/configuration-gen))))

      (t/testing "generation"
        (let [g (gen/generate test-utils/generation-gen)]
          ; (profiling/profile :debug "record-generation")
          (read-line)
          (record-generation
            test-utils/p-generation
            "test-uuid"
            0
            g)))))
