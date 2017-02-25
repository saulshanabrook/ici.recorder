(ns ici.avro
  (:require [abracad.avro]
            [abracad.avro.edn]
            [abracad.avro.write]
            [java-time])
  (:import [java.time.Duration]
           [org.apache.avro.io.Encoder]))

(defn ->bytes [^java.time.Duration duration]
  (bytes (byte-array 12 [(byte 0) (byte 0) (byte (.toMillis duration))])))

;; extend the Duration class to emit a fixed byte array of size 12,
;; where the last number is the number of milliseconds
;; as per http://avro.apache.org/docs/1.8.1/spec.html#Duration
(extend-type java.time.Duration
  abracad.avro.write/HandleBytes

  (count-bytes [^java.time.Duration duration]
    (abracad.avro.write/count-bytes (->bytes duration)))

  (emit-bytes [^java.time.Duration duration ^org.apache.avro.io.Encoder encoder]
    (abracad.avro.write/emit-bytes (->bytes duration) encoder))

  (emit-fixed [^java.time.Duration duration ^org.apache.avro.io.Encoder encoder]
    (abracad.avro.write/emit-fixed (->bytes duration) encoder)))

(def duration
  (abracad.avro/parse-schema
    {:type :fixed
     :name "Duration"
     :logical-type :duration
     :size 12}))


(->>  (java-time/duration 10 :millis)
  (abracad.avro/binary-encoded duration)
  (abracad.avro/decode duration))


(def configuration
  (abracad.avro/parse-schema
    duration
    ;; add EDN schema for use in paramaters as "abracad.avro.edn.Element"
    ;; (abracad.avro.edn/new-schema)
    {:type :record
     :name "Configuration"
     :fields [{:name "problem-file" :type :string}
              {:name "arguments" :type {:type :map :values :string}}
              {:name "parameters" :type {:type :map :values :string}}
              {:name "clojush-version" :type :string}
              {:name "initialization-time" :type "Duration"}
              {:name "git-commit" :type [:string :null]}]}))
