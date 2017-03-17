(ns ici-recorder.parquet
  (:require [java-time])
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

(defprotocol WriteParquet
  (->schema [self ^String name_ ^org.apache.parquet.schema.Type$Repetition repitition])
  (->schema-root [self ^String name_])
  (add-value [self
              ^org.apache.parquet.io.api.RecordConsumer record-consumer
              ^org.apache.parquet.schema.Type schema])
  (add-value-root [self
                   ^org.apache.parquet.io.api.RecordConsumer record-consumer
                   ^org.apache.parquet.schema.Type schema]))



(defn -primitive-schema
  ([name_ repitition primitive] (-primitive-schema name_ repitition primitive  nil))
  ([name_ repitition primitive original] (-primitive-schema name_ repitition primitive  original 0))
  ([name_ repitition primitive original length]
   (org.apache.parquet.schema.PrimitiveType.
     repitition
     (Enum/valueOf org.apache.parquet.schema.PrimitiveType$PrimitiveTypeName primitive)
     length
     name_
     (when original (Enum/valueOf org.apache.parquet.schema.OriginalType original)))))


(defn string-schema [name_ repitition]
  (-primitive-schema name_ repitition "BINARY" "UTF8"))


(defprotocol UnionType
  (union [self other]))

; (set! *warn-on-reflection* true)

(def ^java.lang.reflect.Method -union-primitive
  (.getDeclaredMethod
    org.apache.parquet.schema.PrimitiveType
    "union"
    (into-array Class [org.apache.parquet.schema.Type])))
(.setAccessible -union-primitive true)

(defn merge-fields
  "returns a list of the fields merged for these types"
  [^org.apache.parquet.schema.GroupType gt1
   ^org.apache.parquet.schema.GroupType gt2]
  (concat
    (for [f1 (.getFields gt1)
          :let [n1 (.getName f1)]]
      (if (.containsField gt2 n1)
        (union f1 (.getType gt2 n1))
        f1))
    (for [f2 (.getFields gt2)
          :let [n2 (.getName f2)]
          :when (not (.containsField gt1 n2))]
      f2)))
(extend-protocol UnionType
  org.apache.parquet.schema.PrimitiveType
    (union [self other]
      (if other
        (try
          (.invoke -union-primitive self (into-array [other]))
          (catch java.lang.reflect.InvocationTargetException _
            (string-schema
              (.getName other)
              (.getRepetition other))))
        self))

  org.apache.parquet.schema.GroupType
    (union [self other]
      (if other
        (if self
          (org.apache.parquet.schema.GroupType.
            (.getRepetition other)
            (.getName other)
            (merge-fields self other))
          other)
        self))

  nil
    (union [_ other]
      other))


(defn map-key [key]
  (if (keyword? key)
    (name key)
    (str (map name key))))

(defn -map->field-schemas ^java.util.List [m]
  (for [[k v] m
        :let [s (->schema v (map-key k) org.apache.parquet.schema.Type$Repetition/OPTIONAL)]
        :when s]
    s))


(defn -add-value-wrapped
  "wraps add-value, to see if the schemas is a string. if so, first converts
   value to a string"
  [form
   ^org.apache.parquet.io.api.RecordConsumer record-consumer
   ^org.apache.parquet.schema.Type schema]
  (add-value
    (if (= (.getOriginalType schema)
           org.apache.parquet.schema.OriginalType/UTF8)
      (str form)
      form)
    record-consumer
    schema))
(defn -add-map-fields [m ^org.apache.parquet.io.api.RecordConsumer record-consumer ^org.apache.parquet.schema.GroupType schema]
  (dotimes [i (.getFieldCount schema)]
    (let [inner-schema (.getType schema i)
          name_ (.getName inner-schema)
          form ((keyword name_) m)]
      (when (and (some? form)
                 (if (seq? form)
                   (not-empty form)
                   true))
        (.startField record-consumer name_ i)
        (-add-value-wrapped form record-consumer inner-schema)
        (.endField record-consumer name_ i)))))

(defn seq->schema [s name_]
  (->> (for [f s] (->schema f name_ org.apache.parquet.schema.Type$Repetition/REPEATED))
    (reduce union nil)))

(defn -add-seq-value [s record-consumer schema]
  (doseq [f s] (-add-value-wrapped f record-consumer schema)))

(extend-protocol WriteParquet
  java.lang.Long
    (->schema [_ ^String name_ ^org.apache.parquet.schema.Type$Repetition repitition]
      (-primitive-schema name_ repitition "INT64"))
    (add-value [self ^org.apache.parquet.io.api.RecordConsumer record-consumer ^org.apache.parquet.schema.Type _]
      (.addLong record-consumer self))

  java.lang.Integer
    (->schema [_ ^String name_ ^org.apache.parquet.schema.Type$Repetition repitition]
      (-primitive-schema name_ repitition "INT32"))
    (add-value [self ^org.apache.parquet.io.api.RecordConsumer record-consumer ^org.apache.parquet.schema.Type _]
      (.addInteger record-consumer self))

  Double
    (->schema [_ ^String name_ ^org.apache.parquet.schema.Type$Repetition repitition]
      (-primitive-schema name_ repitition "DOUBLE"))
    (add-value [self ^org.apache.parquet.io.api.RecordConsumer record-consumer ^org.apache.parquet.schema.Type _]
      (.addDouble record-consumer self))

  Boolean
    (->schema [_ ^String name_ ^org.apache.parquet.schema.Type$Repetition repitition]
      (-primitive-schema name_ repitition "BOOLEAN"))
    (add-value [self ^org.apache.parquet.io.api.RecordConsumer record-consumer ^org.apache.parquet.schema.Type _]
      (.addBoolean record-consumer self))

  String
    (->schema [_ ^String name_ ^org.apache.parquet.schema.Type$Repetition repitition]
      (string-schema name_ repitition))
    (add-value [self ^org.apache.parquet.io.api.RecordConsumer record-consumer ^org.apache.parquet.schema.Type _]
      (->> self
        org.apache.parquet.io.api.Binary/fromString
        (.addBinary record-consumer)))

  clojure.lang.Keyword
    (->schema [self ^String name_ ^org.apache.parquet.schema.Type$Repetition repitition]
      (->schema "" name_ repitition))
    (add-value [self ^org.apache.parquet.io.api.RecordConsumer record-consumer ^org.apache.parquet.schema.Type schema]
      (add-value (name self) record-consumer schema))


  Object
    (->schema [self ^String name_ ^org.apache.parquet.schema.Type$Repetition repitition]
      (string-schema name_ repitition))


  java.time.LocalDate
    (->schema [_ ^String name_ ^org.apache.parquet.schema.Type$Repetition repitition]
      (-primitive-schema name_ repitition "INT32" "DATE"))
    (add-value [self ^org.apache.parquet.io.api.RecordConsumer record-consumer ^org.apache.parquet.schema.Type _]
      (->> self
        .toEpochDay
        int
        (.addInteger record-consumer)))

  java.time.LocalTime
    (->schema [_ ^String name_ ^org.apache.parquet.schema.Type$Repetition repitition]
      (-primitive-schema name_ repitition "INT32" "TIME_MILLIS"))
    (add-value [self ^org.apache.parquet.io.api.RecordConsumer record-consumer ^org.apache.parquet.schema.Type _]
      (->> (java-time/property self :milli-of-day)
        java-time/value
        int
        (.addInteger record-consumer)))

  java.time.Instant
    (->schema [_ ^String name_ ^org.apache.parquet.schema.Type$Repetition repitition]
      (-primitive-schema name_ repitition "INT64" "TIMESTAMP_MILLIS"))
    (add-value [self ^org.apache.parquet.io.api.RecordConsumer record-consumer ^org.apache.parquet.schema.Type _]
      (->> self
        .toEpochMilli
        (.addLong record-consumer)))

  java.time.Duration
    (->schema [_ ^String name_ ^org.apache.parquet.schema.Type$Repetition repitition]
      (-primitive-schema name_ repitition "FIXED_LEN_BYTE_ARRAY" "INTERVAL" 12))
    (add-value [self ^org.apache.parquet.io.api.RecordConsumer record-consumer ^org.apache.parquet.schema.Type _]
      (->> self
        .toMillis
        (conj [0 0])
        (byte-array 12)
        bytes
        org.apache.parquet.io.api.Binary/fromConstantByteArray
        (.addBinary record-consumer)))

  java.util.Collection
    ; (->schema [self ^String name_]
    ;   (->> (map ->schema self (repeat "element"))
    ;     (reduce union)
    ;     (org.apache.parquet.schema.ConversionPatterns/listOfElements
    ;       -default-repition
    ;       name_)))
    ; (add-value [self ^org.apache.parquet.io.api.RecordConsumer record-consumer ^org.apache.parquet.schema.Type]
    ;   (.startGroup record-consumer)
    ;   (.startField record-consumer "list" 0)
    ;   (dorun
    ;     (map
    ;       (fn [form]
    ;         (.startGroup record-consumer)
    ;         (-add-field record-consumer 0 ["element" form])
    ;         (.endGroup record-consumer))
    ;       self))
    ;   (.endField record-consumer "list" 0)
    ;   (.endGroup record-consumer))
    (->schema [self ^String name_ _]
      (seq->schema self ^String name_))
    (add-value [self ^org.apache.parquet.io.api.RecordConsumer record-consumer ^org.apache.parquet.schema.Type schema]
      (-add-seq-value self record-consumer schema))

  java.util.Map
    (->schema [self ^String name_ ^org.apache.parquet.schema.Type$Repetition repitition]
      (org.apache.parquet.schema.GroupType. repitition name_ (-map->field-schemas self)))
    (->schema-root [self ^String name_]
      (org.apache.parquet.schema.MessageType. name_ (-map->field-schemas self)))

    (add-value [self ^org.apache.parquet.io.api.RecordConsumer record-consumer ^org.apache.parquet.schema.Type schema]
      (when-not (.isEmpty self)
        (.startGroup record-consumer)
        (-add-map-fields self record-consumer schema)
        (.endGroup record-consumer)))
    (add-value-root [self ^org.apache.parquet.io.api.RecordConsumer record-consumer ^org.apache.parquet.schema.Type schema]
      (.startMessage record-consumer)
      (-add-map-fields self record-consumer schema)
      (.endMessage record-consumer))

  nil
    (->schema [_ _ _]
      nil))
(defn ->write-context [schema]
  (org.apache.parquet.hadoop.api.WriteSupport$WriteContext. schema {}))

(defn ->write-support [schema]
  (let [record-consumer (atom nil)]
    (proxy [org.apache.parquet.hadoop.api.WriteSupport] []

      (init [configuration]
        (->write-context schema))

      (prepareForWrite [record-consumer_]
        (compare-and-set! record-consumer nil record-consumer_))

      (write [record]
        (add-value-root record @record-consumer schema)))))

(defn ->builder ^org.apache.parquet.hadoop.ParquetWriter$Builder [schema path]
  (proxy [org.apache.parquet.hadoop.ParquetWriter$Builder] [path]
    (getWriteSupport [configuration]
      (->write-support schema))
    (self []
       this)))

(defn ->configuration [hadoopOptions]
  (let [conf (org.apache.hadoop.conf.Configuration.)]
    (doseq [[k v] hadoopOptions]
      (.set conf k v))
    conf))


(defn ->parquet-writer ^org.apache.parquet.hadoop.ParquetWriter [schema options]
  (-> (->builder schema (:path options))
    (.withWriteMode
      (Enum/valueOf org.apache.parquet.hadoop.ParquetFileWriter$Mode (:write-mode options)))
    (.withValidation (:validation options))
    (.withConf (->configuration (:hadoop options)))
    (.withCompressionCodec
      (Enum/valueOf org.apache.parquet.hadoop.metadata.CompressionCodecName (:compression-codec options)))
    ; (.withWriterVersion org.apache.parquet.column.ParquetProperties$WriterVersion/PARQUET_2_0)
    .build))


(defn write- [form options]
  (let [schema (->schema-root form "root")
        ^org.apache.parquet.hadoop.ParquetWriter parquet-writer (->parquet-writer schema options)]
    (.write parquet-writer form)
    (.close parquet-writer)))
