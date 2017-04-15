(ns ici-recorder.parquet.write
  (:require [clojure.spec :as s]
            [ici-recorder.parquet.add-data :refer [add-record]]
            [ici-recorder.parquet.schema.interop :refer [->schema]]
            [ici-recorder.parquet.schema.spec :as p]
            [taoensso.timbre.profiling :as profiling :refer [defnp p] :rename {p pp}])

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

(defn enum->values [enum-values]
  (->> enum-values
    (map #(.name %))
    set))

(defn ->write-context [schema]
  (org.apache.parquet.hadoop.api.WriteSupport$WriteContext. (->schema schema) {}))

(s/fdef ->write-context
  :args (s/cat :schema ::p/schema))


(defn ->write-support [schema]
  (let [record-consumer (atom nil)]
    (proxy [org.apache.parquet.hadoop.api.WriteSupport] []

      (init [configuration]
        (->write-context schema))

      (prepareForWrite [record-consumer_]
        (compare-and-set! record-consumer nil record-consumer_))

      (write [record]
        (add-record schema record @record-consumer)))))

(s/fdef ->write-support
  :args (s/cat :schema ::p/schema))


(s/def ::path (partial instance? org.apache.hadoop.fs.Path))

(defn ->builder ^org.apache.parquet.hadoop.ParquetWriter$Builder [schema path]
  (proxy [org.apache.parquet.hadoop.ParquetWriter$Builder] [path]
    (getWriteSupport [configuration]
      (->write-support schema))
    (self []
       this)))
(s/fdef ->parquet-writer
  :args (s/cat :schema ::p/schema :path ::path))


(s/def ::hadoop (s/map-of string? string?))
(defn ->hadoop-config [hadoop]
  (let [conf (org.apache.hadoop.conf.Configuration.)]
    (doseq [[k v] hadoop]
      (.set conf k v))
    conf))

(s/fdef ->hadoop-config
  :args (s/cat :hadoop ::hadoop))

(s/def ::write-mode (enum->values (org.apache.parquet.hadoop.ParquetFileWriter$Mode/values)))
(s/def ::validation boolean?)
(s/def ::compression-codec (enum->values (org.apache.parquet.hadoop.metadata.CompressionCodecName/values)))
(s/def ::hadoop-config (partial instance? org.apache.hadoop.conf.Configuration))
(s/def ::options
  (s/keys :req-un [::path ::write-mode ::validation ::hadoop-config ::compression-codec]))

(defnp ->parquet-writer ^org.apache.parquet.hadoop.ParquetWriter [schema options]
  (-> (->builder schema (:path options))
    (.withWriteMode
      (Enum/valueOf org.apache.parquet.hadoop.ParquetFileWriter$Mode (:write-mode options)))
    (.withValidation (:validation options))
    (.withConf (:hadoop-config options))
    (.withCompressionCodec
      (Enum/valueOf org.apache.parquet.hadoop.metadata.CompressionCodecName (:compression-codec options)))
    ; (.withWriterVersion org.apache.parquet.column.ParquetProperties$WriterVersion/PARQUET_2_0)
    .build))

(s/fdef ->parquet-writer
  :args (s/cat :schema ::p/schema :options ::options))


(defn write
  [schema form options]
  (let [^org.apache.parquet.hadoop.ParquetWriter parquet-writer (->parquet-writer schema options)]
    (.write parquet-writer form)
    (pp :close
      (.close parquet-writer))))

(s/fdef write
  :args (s/cat :schema ::p/schema :form any? :options ::options))
