(ns ici-recorder.parquet.schema.spec
  (:require [clojure.spec :as s]))

(s/def ::not-nested
  #{:boolean
    :integer
    :long
    :float
    :double
    :string
    :instant})

(s/def ::field
  (s/cat :required? boolean?
         :type ::type))

(s/def ::map
  (s/spec (s/cat :key ::not-nested
                 :value ::field)))

(s/def ::list
  (s/spec (s/cat :item ::field)))

(s/def ::group
  (s/and
    (s/map-of keyword? ::field)
    (partial instance? clojure.lang.PersistentArrayMap)))

(s/def ::type
  (s/or :not-nested ::not-nested
        :map ::map
        :list ::list
        :group ::group))

(s/def ::schema ::group)
