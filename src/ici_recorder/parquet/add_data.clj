(ns ici-recorder.parquet.add-data
  (:require [clojure.spec :as s]
            [taoensso.timbre :as timbre]
            
            [ici-recorder.parquet.schema.spec :as p])

  (:import (org.apache.parquet.schema)
           (clojure.lang)
           (org.apache.parquet.hadoop.api)
           (org.apache.parquet.hadoop.metadata)
           (org.apache.hadoop.conf)
           (org.apache.parquet.column)
           (java.lang.reflect)
           (org.apache.hadoop.fs)
           (org.apache.parquet.io.api)
           (java.time)))

(defmulti add-not-nested
  (fn [not-nested form _]
    [not-nested (type form)]))

(defmethod add-not-nested [:boolean java.lang.Object]
  [_ form ^org.apache.parquet.io.api.RecordConsumer record-consumer]
  (.addBoolean record-consumer (boolean form)))

(defmethod add-not-nested [:integer java.lang.Object]
  [_ form ^org.apache.parquet.io.api.RecordConsumer record-consumer]
  (.addInteger record-consumer (int form)))

(defmethod add-not-nested [:long java.lang.Object]
  [_ form ^org.apache.parquet.io.api.RecordConsumer record-consumer]
  (.addLong record-consumer (long form)))

(defmethod add-not-nested [:float java.lang.Object]
  [_ form ^org.apache.parquet.io.api.RecordConsumer record-consumer]
  (.addFloat record-consumer (float form)))

(defmethod add-not-nested [:double java.lang.Object]
  [_ form ^org.apache.parquet.io.api.RecordConsumer record-consumer]
  (.addDouble record-consumer (double form)))

(defmethod add-not-nested [:string java.lang.Object]
  [_ form ^org.apache.parquet.io.api.RecordConsumer record-consumer]
  (->> form
    (str)
    org.apache.parquet.io.api.Binary/fromString
    (.addBinary record-consumer)))

(defmethod add-not-nested [:instant java.time.Instant]
  [_ form ^org.apache.parquet.io.api.RecordConsumer record-consumer]
  (add-not-nested :instant (long (.toEpochMilli form)) record-consumer))

(defmethod add-not-nested [:instant java.lang.Long]
  [_ form ^org.apache.parquet.io.api.RecordConsumer record-consumer]
  (.addLong form record-consumer))

(s/fdef add-not-nested
  :args (s/cat :not-nested ::p/not-nested :form any? :record-consumer any?))

(defmulti add-value
  (fn [field form record-consumer]
    (first (s/conform ::p/type field))))

(s/fdef add-value
  :args (s/cat :type ::p/type :form any? :record-consumer any?))

(defmethod add-value :not-nested
  [not-nested form record-consumer]
  (add-not-nested not-nested form record-consumer))


(defn trim-str [f]
  (let [s (str f)]
    (subs s 0 (min (count s) 40))))
(defn add-group-fields [group m ^org.apache.parquet.io.api.RecordConsumer record-consumer]
  (let [group-seq (seq group)]
    (dotimes [i (count group-seq)]
      (let [[name-key [required? type]] (nth group-seq i)
            form (name-key m)
            name_ (name name-key)]
        (when (and (some? form))
          (timbre/trace "adding field"
            name_
            (trim-str type)
            (trim-str form))
          (.startField record-consumer name_ i)
          (add-value type form record-consumer)
          (.endField record-consumer name_ i))))))

(s/fdef add-group-fields
  :args (s/cat :group ::p/group :m any? :record-consumer any?))

(defmethod add-value :group
  [group map_ record-consumer]
  (when (some? map_)
    (.startGroup record-consumer)
    (add-group-fields group map_ record-consumer)
    (.endGroup record-consumer)))

(defn add-list-field [item-type list_ record-consumer]
  (when (not-empty list_)
    (.startField record-consumer "list" 0)
    (doseq [f list_]
      (.startGroup record-consumer)
      (when (some? f)
        (.startField record-consumer "element" 0)
        (add-value item-type f record-consumer)
        (.endField record-consumer "element" 0))
      (.endGroup record-consumer))
    (.endField record-consumer "list" 0)))

(s/fdef add-list-field
  :args (s/cat :item-type ::p/type
               :list (s/coll-of any?)
               :record-consumer any?))

(defmethod add-value :list
  [[item-required? item-type] list_ record-consumer]
  (.startGroup record-consumer)
  (add-list-field item-type list_ record-consumer)
  (.endGroup record-consumer))


(defn add-map-field [key-type value-type map_ record-consumer]
  (when (not-empty map_)
    (.startField record-consumer "key_value" 0)

    (doseq [[k v] map_]
      (.startGroup record-consumer)
      
      (.startField record-consumer "key" 0)
      (add-value key-type k record-consumer)
      (.endField record-consumer "key" 0)
      
      (when (some? v)
        (.startField record-consumer "value" 1)
        (add-value value-type v record-consumer)
        (.endField record-consumer "value" 1))
    
      (.endGroup record-consumer))

    (.endField record-consumer "key_value" 0)))

(s/fdef add-map-field
  :args (s/cat :key-type ::p/type
               :item-type ::p/type
               :map_ (s/map-of any? any?)
               :record-consumer any?))


(defmethod add-value :map
  [[key-type value-required? value-type] map_ record-consumer]
  (.startGroup record-consumer)
  (add-map-field key-type value-type map_ record-consumer)
  (.endGroup record-consumer))

(s/fdef add-value
  :args (s/cat :type ::p/type :form any? :record-consumer any?))



(defn add-record
    [schema
     form
     ^org.apache.parquet.io.api.RecordConsumer record-consumer]
  (.startMessage record-consumer)
  (add-group-fields schema form record-consumer)
  (.endMessage record-consumer))

(s/fdef add-record
  :args (s/cat :schema ::p/schema :form any? :record-consumer any?))
