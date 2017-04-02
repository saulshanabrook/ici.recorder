(ns ici-recorder.clojush
  (:require [ici-recorder.parquet.write :refer [write]]
            [environ.core])
  (:import (org.apache.hadoop.fs)
           (java.net)))

(defn -write [schema path form]
  (write
    schema
    form
    {:path (org.apache.hadoop.fs.Path. path)
     :write-mode "OVERWRITE"
     :validation true
     :compression-codec "GZIP"
     :hadoop {"fs.alluxio.impl" "alluxio.hadoop.FileSystem"
              "fs.alluxio-ft.impl" "alluxio.hadoop.FaultTolerantFileSystem"
              "fs.AbstractFileSystem.alluxio.impl" "alluxio.hadoop.AlluxioFileSystem"
              "alluxio.security.group.mapping.class" "alluxio.security.group.provider.IdentityUserGroupsMapping"
              "alluxio.user.file.writetype.default" "ASYNC_THROUGH"
              "alluxio.security.authorization.permission.umask" "000"}}))
              ; "alluxio.security.login.username" "root"
              ; "alluxio.security.authorization.permission.enabled" "false"}}))
              ; "alluxio.security.authentication.type" "NOSASL"}}))

(def -uri (java.net.URI. (environ.core/env :clojush-parquet-uri "")))
(def -config-uri (.resolve -uri "configs/"))
(def -generations-uri (.resolve -uri "generations/"))
(defn record-run [schema uuid config]
  (-write
    schema
    (.resolve -config-uri (str "uuid=" uuid "/data.parquet"))
    config))

(defn record-generation [schema config-uuid index generation]
  (-write
    schema
    (.resolve -generations-uri (str "run-uuid=" config-uuid "/" "index=" index "/data.parquet"))
    generation))
