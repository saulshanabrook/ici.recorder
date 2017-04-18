(ns ici-recorder.clojush-benchmark
  (:require [ici-recorder.clojush :refer [record-run record-generation]]
            [ici-recorder :refer [->write-support]]
            [ici-recorder.test-utils :as test-utils]
            [clojure.spec.gen :as gen]
            [criterium.core :as criterium]))

(defn -main []
  (clojure.spec.test/with-instrument-disabled
    (let [g (gen/generate test-utils/generation-gen)]
      (if (read-string (environ.core/env :benchmark "false"))
        (criterium/with-progress-reporting
          (criterium/quick-bench
            (record-generation
              test-utils/generation-write-support
              (str (java.util.UUID/randomUUID))
              0
              g)
            :verbose))
        (do
          (println "Ready...")
          (read-line)
          (println "Starting")
          (time
            (record-generation
              test-utils/generation-write-support
              (str (java.util.UUID/randomUUID))
              0
              g)))))))
