(ns ici-recorder.clojush-benchmark
  (:require [ici-recorder.clojush :refer [record-run record-generation]]
            [taoensso.timbre :as timbre]
            [ici-recorder.test-utils :as test-utils]
            [clojure.spec.gen :as gen]))


(defn -main []
  (clojure.spec.test/with-instrument-disabled
    (timbre/with-level :error
      (let [g (gen/generate test-utils/generation-gen)]
        (println "Ready...")
        (read-line)
        (println "Starting")
        (time
          (record-generation
            test-utils/p-generation
            "test-uuid"
            0
            g))))))
