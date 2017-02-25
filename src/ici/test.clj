(ns ici.test
  (:require [ici.spec]
            [ici.avro]
            [clojure.spec :as s]
            [abracad.avro]
            [clojure.spec.gen]))



(def g (clojure.spec.gen/generate ici.spec/configuration-gen))

(def g-dec
  (->> g
    (abracad.avro/binary-encoded ici.avro/configuration)
    (abracad.avro/decode ici.avro/configuration)))

(s/def ::list-of-ints (s/coll-of int?))
(s/def ::list-of-floats (s/coll-of float?))
(s/def ::a (s/or :ints ::list-of-ints :floats ::list-of-floats))
