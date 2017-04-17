(ns ici-recorder.parquet.spec
  (:require [clojure.spec :as s]))

(s/def ::write-support (partial instance? org.apache.parquet.hadoop.api.WriteSupport))
(s/def ::path (partial instance? org.apache.hadoop.fs.Path))
