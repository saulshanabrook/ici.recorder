(ns ici-recorder.parquet.write
  (:require [clojure.spec :as s]
            [ici-recorder.parquet.schema.spec :as p]
            [ici-recorder.parquet.spec :as hadoop-s])
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

(defn ->builder ^org.apache.parquet.hadoop.ParquetWriter$Builder [write-support path]
  (proxy [org.apache.parquet.hadoop.ParquetWriter$Builder] [path]
    (getWriteSupport [configuration]
      write-support)
    (self []
       this)))
(s/fdef ->parquet-writer
  :args (s/cat :write-support ::hadoop-s/write-support :path ::path))


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
  (s/keys :req-un [::hadoop-s/path ::write-mode ::validation ::hadoop-config ::compression-codec]))

(defn ->parquet-writer ^org.apache.parquet.hadoop.ParquetWriter [write-support options]
  (-> (->builder write-support (:path options))
    (.withWriteMode
      (Enum/valueOf org.apache.parquet.hadoop.ParquetFileWriter$Mode (:write-mode options)))
    (.withValidation (:validation options))
    (.withConf (:hadoop-config options))
    (.withCompressionCodec
      (Enum/valueOf org.apache.parquet.hadoop.metadata.CompressionCodecName (:compression-codec options)))
    ; (.withWriterVersion org.apache.parquet.column.ParquetProperties$WriterVersion/PARQUET_2_0)
    .build))

(s/fdef ->parquet-writer
  :args (s/cat :write-support ::hadoop-s/write-support :options ::options))


(defn write
  [write-support form options]
  (let [^org.apache.parquet.hadoop.ParquetWriter parquet-writer (->parquet-writer write-support options)]
    (.write parquet-writer form)
    (.close parquet-writer)))

(s/fdef write
  :args (s/cat :write-support ::hadoop-s/write-support :form any? :options ::options))
