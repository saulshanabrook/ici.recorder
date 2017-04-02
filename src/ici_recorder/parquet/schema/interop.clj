(ns ici-recorder.parquet.schema.interop
  (:require [clojure.spec :as s]
            [clojure.spec.test :as stest]
            
            [ici-recorder.parquet.schema.spec :as p]))


(defmulti ->type (fn [field name_] (first (s/conform ::p/type (second field)))))

(s/fdef ->type
  :args (s/cat :field (s/spec ::p/field)
               :name string?))


(defn required?->repetition [required?] ^org.apache.parquet.schema.Type$Repetition
  (let [repetition (if required? "REQUIRED" "OPTIONAL")]
    (Enum/valueOf org.apache.parquet.schema.Type$Repetition repetition)))


(def not-nested->names
  {:boolean ["BOOLEAN"]
   :integer ["INT32"]
   :long ["INT64"]
   :float ["FLOAT"]
   :double ["DOUBLE"]
   :string ["BINARY" "UTF8"]
   :instant ["INT64" "TIMESTAMP_MILLIS"]})

(defmethod ->type :not-nested
  [[required? not-nested] name_]
  (let [[primitive original length] (not-nested->names not-nested)]
    (org.apache.parquet.schema.PrimitiveType.
      (required?->repetition required?)
      (Enum/valueOf org.apache.parquet.schema.PrimitiveType$PrimitiveTypeName primitive)
      (or length 0)
      name_
      (when original (Enum/valueOf org.apache.parquet.schema.OriginalType original)))))


(defn map->field-types  ^java.util.List [map_]
  (for [[k v] map_]
    (->type v (name k))))

(s/fdef map->field-types
  :args (s/cat :map_ ::p/group))

(defmethod ->type :group
  [[required? group] name_]
  (org.apache.parquet.schema.GroupType.
    (required?->repetition required?)
    name_
    (map->field-types group)))


(defmethod ->type :list
  [[required? field] name_]
  (org.apache.parquet.schema.ConversionPatterns/listOfElements
    (required?->repetition required?)
    name_
    (->type field "element")))


(defmethod ->type :map
  [[required? [keyType & valueField]] name_]
  (org.apache.parquet.schema.ConversionPatterns/mapType
    (required?->repetition required?)
    name_
    (->type [true keyType] "key")
    (->type valueField "value")))



(defn ->schema [root]
  (org.apache.parquet.schema.MessageType. "root" (map->field-types root)))

(s/fdef ->schema
  :args (s/cat :root ::p/schema))

