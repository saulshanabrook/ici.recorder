(ns ici.clojush
  (:require [ici.parquet]
            [environ.core])
  (:import (org.apache.hadoop.fs)
           (java.net)))

(defn -write [path form]
  (ici.parquet/write-
    form
    {:path path
     :write-mode "CREATE"
     :validation false
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

(def -uri (java.net.URI. (environ.core/env :clojush-uri "")))
(def -config-uri (.resolve -uri "configs/"))
(def -generations-uri (.resolve -uri "generations/"))
(defn record-config [uuid config]
  (-write
    (org.apache.hadoop.fs.Path. (.resolve -config-uri (str uuid "/data.parquet")))
    config))

(defn record-generation [config-uuid index generation]
  (-write
    (org.apache.hadoop.fs.Path. (.resolve -generations-uri (str config-uuid "/" index "/data.parquet")))
    generation))
