(ns ici-recorder.clojush
  (:require [ici-recorder.parquet.write :refer [write ->hadoop-config]]
            [environ.core])
  (:import (org.apache.hadoop.fs)
           (java.net)))

(def hadoop-config
  (->hadoop-config {"fs.alluxio.impl" "alluxio.hadoop.FileSystem"
                    "fs.alluxio-ft.impl" "alluxio.hadoop.FaultTolerantFileSystem"
                    "fs.AbstractFileSystem.alluxio.impl" "alluxio.hadoop.AlluxioFileSystem"}))

(def -base-uri (java.net.URI. (environ.core/env :clojush-parquet-uri "")))

; create the file at a temporary location, then move it.
; this is so that partially written files aren't in the path and won't break reading
(defn -write [schema path form]
  (let [uri (org.apache.hadoop.fs.Path. (.resolve -base-uri path))
        tmp-uri (org.apache.hadoop.fs.Path. (.resolve -base-uri (str "tmp/" path)))
        fs (.getFileSystem tmp-uri hadoop-config)]
    (write
      schema
      form
      {:path tmp-uri
       :write-mode "OVERWRITE"
       :validation true
       :compression-codec "GZIP"
       :hadoop-config hadoop-config})

    (.rename fs tmp-uri uri)))

(defn record-run [schema uuid config]
  (-write
    schema
    (str "configs/uuid=" uuid "/data.parquet")
    config))

(defn record-generation [schema config-uuid index generation]
  (-write
    schema
    (str "generations/run-uuid=" config-uuid "/" "index=" index "/data.parquet")
    generation))
