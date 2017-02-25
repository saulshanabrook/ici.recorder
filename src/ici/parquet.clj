(ns ici.parquet
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]))


(s/def :parquet.schema.name string?)



(defmulti type #(map (partial get %) [:primitive :logical :nested]))


;; primitive types
(s/def :parquet.type/primitive
  #{:parquet.type.primitive/boolean
    :parquet.type.primitive/int32
    :parquet.type.primitive/int64
    :parquet.type.primitive/int96
    :parquet.type.primitive/float
    :parquet.type.primitive/double
    :parquet.type.primitive/byte-array})
(doall (map #(derive % :parquet.type/primitive) (s/form (s/get-spec :parquet.type/primitive))))
(s/def :parquet.type.primitive/length (s/and int? pos?))

; make each of the primitive types a :parquet.schema.type/primitive
; so that :parquet.schema.type/primitive will match them all
(defmethod type [:parquet.type/primitive nil nil] [_]
  (s/keys :req [:parquet.type/primitive]))

;; byte-array requires a length
(prefer-method bar [:parquet.type.primitive/byte-array nil nil] [:parquet.type/primitive nil nil])
(def -byte-array-inner
  (s/keys :req [:parquet.type/primitive
                :parquet.type.primitive/length]))
(defmethod type [:parquet.type.primitive/byte-array nil nil] [_] -byte-array-inner)


;; logical types
(defmethod type [:parquet.type.primitive/byte-array :parquet.type.logical/utf8 nil] [_]
  (s/merge -byte-array-inner
           (s/keys :req []):primitive))
(defmethod type {:primitive :int64} [_]
  (s/keys :req :primitive))

(defmethod type {:primitive :int64} [_]
  (s/keys :req :primitive))

(s/def ::field (s/keys :req-un [::name ::repitition ::type]))
(s/def ::fields (s/coll-of ::field))

(s/def ::message (s/keys :req [::name ::fields]))


{
  :name "hi"
  :fields
    [
      {:name "key"
       :repitition :required
       :type :double}
      {:name "third"
       :repitition :optional
       :map [{:type {:primitive :boolean}}
             {:repitition :required
              :type {:primitive :boolean}}]}
      {:name "third"
       :repitition :optional
       :map [{:type :double}
             {:repitition :required
              :type {:primitive :boolean}}]}
      {:name "forth"
       :repitition :optional
       :group [{}]}]}


{:map {:keys ints :vals ints}}

{:hi 123 :there 22}
