(ns ici.parquet
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [java-time])
  (:import (org.apache.parquet.schema)
           (clojure.lang)
           (org.apache.parquet.hadoop.api)
           (org.apache.parquet.hadoop.metadata)
           (org.apache.hadoop.conf)
           (org.apache.parquet.column)
           (org.apache.hadoop.fs)
           (org.apache.parquet.io.api)
           (java.time)))

(defprotocol UnionType
  (union [self other]))

(def -union-primitive
  (.getDeclaredMethod
    org.apache.parquet.schema.PrimitiveType
    "union"
    (into-array Class [org.apache.parquet.schema.Type])))
(.setAccessible -union-primitive true)

(def -union-group
  (.getDeclaredMethod
    org.apache.parquet.schema.GroupType
    "union"
    (into-array Class [org.apache.parquet.schema.Type])))
(.setAccessible -union-group true)

(extend-protocol UnionType
  org.apache.parquet.schema.PrimitiveType
    (union [self other]
      (.invoke -union-primitive self (into-array [other])))

  org.apache.parquet.schema.GroupType
    (union [self other]
      (.invoke -union-group self (into-array [other]))))


(defprotocol WriteParquet
  (->schema [self name_ repitition])
  (->schema-root [self name_])
  (add-value [self record-consumer schema])
  (add-value-root [self record-consumer schema]))

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

(defn -map->field-schemas [m]
  (map ->schema
    (vals m)
    (map name (keys m))
    (repeat org.apache.parquet.schema.Type$Repetition/OPTIONAL)))

(defn -add-map-fields [m record-consumer schema]
  (->> m
    (map (fn [[k v]]
           (let [name_ (name k)
                 index (.getFieldIndex schema name_)]
            {:name name_
             :form v
             :index index
             :inner-schema (.getType schema index)})))
    (sort-by :index)
    (map (fn [{:keys [name_ form index inner-schema]}]
            (.startField record-consumer name_ index)
            (add-value form record-consumer inner-schema)
            (.endField record-consumer name_ index)))
    doall))

(defn seq->schema [s name_]
  (->> (map ->schema s (repeat name_) (repeat org.apache.parquet.schema.Type$Repetition/REPEATED))
    (reduce union)))

(defn -add-seq-value [s record-consumer schema]
  (dorun (map #(add-value % record-consumer schema) s)))


(extend-protocol WriteParquet
  Long
    (->schema [_ name_ repitition]
      (-primitive-schema name_ repitition "INT64"))
    (add-value [self record-consumer _]
      (.addLong record-consumer self))

  Double
    (->schema [_ name_ repitition]
      (-primitive-schema name_ repitition "DOUBLE"))
    (add-value [self record-consumer _]
      (.addDouble record-consumer self))

  Boolean
    (->schema [_ name_ repitition]
      (-primitive-schema name_ repitition "BOOLEAN"))
    (add-value [self record-consumer _]
      (.addBoolean record-consumer self))

  String
    (->schema [_ name_ repitition]
      (-primitive-schema name_ repitition "BINARY" "UTF8"))
    (add-value [self record-consumer _]
      (->> self
        org.apache.parquet.io.api.Binary/fromString
        (.addBinary record-consumer)))

  clojure.lang.Keyword
    (->schema [self name_ repitition]
      (->schema (name self) name_ repitition))
    (add-value [self record-consumer schema]
      (add-value (name self) record-consumer schema))

  java.time.LocalDate
    (->schema [_ name_ repitition]
      (-primitive-schema name_ repitition "INT32" "DATE"))
    (add-value [self record-consumer _]
      (->> self
        .toEpochDay
        int
        (.addInteger record-consumer)))

  java.time.LocalTime
    (->schema [_ name_ repitition]
      (-primitive-schema name_ repitition "INT32" "TIME_MILLIS"))
    (add-value [self record-consumer _]
      (->> (java-time/property self :milli-of-day)
        java-time/value
        int
        (.addInteger record-consumer)))

  java.time.Instant
    (->schema [_ name_ repitition]
      (-primitive-schema name_ repitition "INT64" "TIMESTAMP_MILLIS"))
    (add-value [self record-consumer _]
      (->> self
        .toEpochMilli
        (.addLong record-consumer)))

  java.time.Duration
    (->schema [_ name_ repitition]
      (-primitive-schema name_ repitition "FIXED_LEN_BYTE_ARRAY" "INTERVAL" 12))
    (add-value [self record-consumer _]
      (->> self
        .toMillis
        (conj [0 0])
        (byte-array 12)
        bytes
        org.apache.parquet.io.api.Binary/fromConstantByteArray
        (.addBinary record-consumer)))

  clojure.lang.IPersistentList
    ; (->schema [self name_]
    ;   (->> (map ->schema self (repeat "element"))
    ;     (reduce union)
    ;     (org.apache.parquet.schema.ConversionPatterns/listOfElements
    ;       -default-repition
    ;       name_)))
    ; (add-value [self record-consumer]
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
    (->schema [self name_ _]
      (seq->schema self name_))
    (add-value [self record-consumer schema]
      (-add-seq-value self record-consumer schema))

  clojure.lang.PersistentVector
    (->schema [self name_ _]
      (seq->schema self name_))
    (add-value [self record-consumer schema]
      (-add-seq-value self record-consumer schema))


  clojure.lang.APersistentMap
    (->schema [self name_ repitition]
      (org.apache.parquet.schema.GroupType. repitition name_ (-map->field-schemas self)))
    (->schema-root [self name_]
      (org.apache.parquet.schema.MessageType. name_ (-map->field-schemas self)))

    (add-value [self record-consumer schema]
      (.startGroup record-consumer)
      (-add-map-fields self record-consumer schema)
      (.endGroup record-consumer))
    (add-value-root [self record-consumer schema]
      (.startMessage record-consumer)
      (-add-map-fields self record-consumer schema)
      (.endMessage record-consumer)))

(def data
  {:an-int 123
   :another-int 2332
   :a-double 232.2323
   :a-boolean true
   :a-group {:bool true :another 343}
   :a-string "hie"
   :a-date (java-time/local-date)
   :time (java-time/local-time)
   :a-list '({:hi 1} {:hi 2} {:there 3})
   :primitive-list '(1 2 3)
   :vector [true false]
   :vector-2 [{:inner-vec [{:hi 1}]}
              {:first 123 :inner-vec [{:there 2 :hi 3}]}]})

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


(defn ->path [s]
  (org.apache.hadoop.fs.Path. s))

(defn ->builder [schema path]
  (proxy [org.apache.parquet.hadoop.ParquetWriter$Builder] [path]
    (getWriteSupport [configuration]
      (->write-support schema))
    (self []
       this)))

(defn ->configuration [hadoopOptions]
  (let [conf (org.apache.hadoop.conf.Configuration.)]
    (dorun (map (fn [[k v]] (.set conf k v)) hadoopOptions))
    conf))


(defn ->parquet-writer [schema options]
  (-> (->builder schema (:path options))
    (.withWriteMode
      (Enum/valueOf org.apache.parquet.hadoop.ParquetFileWriter$Mode (:write-mode options)))
    (.withValidation (:validation options))
    (.withConf (->configuration (:hadoop options)))


    ; (.withWriterVersion org.apache.parquet.column.ParquetProperties$WriterVersion/PARQUET_2_0)
    .build))

(defn write- [form options]
  (let [schema (->schema-root form "root")
        parquet-writer (->parquet-writer schema options)]
    (.write parquet-writer form)
    (.close parquet-writer)))

(defn -main []
  (write-
    data
    {:path (->path "t.parquet")
     :write-mode "OVERWRITE"
     :validation true
     :hadoop {"fs.alluxio.impl" "alluxio.hadoop.FileSystem"
              "fs.alluxio-ft.impl" "alluxio.hadoop.FaultTolerantFileSystem"
              "fs.AbstractFileSystem.alluxio.impl" "alluxio.hadoop.AlluxioFileSystem"}}))
