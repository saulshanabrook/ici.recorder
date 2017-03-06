(ns ici.test
  (:require [ici.spec]
            [ici.avro]
            [clojure.spec :as s]
            [abracad.avro]
            [clojure.spec.gen]
            [ici.parquet])
  (:import (org.apache.hadoop.fs)))
          ;  (org.apache.alluxio.hadoop)))

  ; (org.apache.hadoop.fs.Path. s))


; (def c (clojure.spec.gen/generate ici.spec/configuration-gen))

(def gs
  (repeatedly 1 (partial clojure.spec.gen/generate ici.spec/generation-gen)))
; (def g-dec
;   (->> g
;     (abracad.avro/binary-encoded ici.avro/configuration)
;     (abracad.avro/decode ici.avro/configuration)))
;
; (s/def ::list-of-ints (s/coll-of int?))
; (s/def ::list-of-floats (s/coll-of float?))
; (s/def ::a (s/or :ints ::list-of-ints :floats ::list-of-floats))

(ici.parquet/write-
  (first gs)
  {:path (ici.parquet/->path "alluxio://192.81.213.31:19998/dir/ttt.parquet")
   :write-mode "CREATE"
   :validation true
   :hadoop {"fs.alluxio.impl" "alluxio.hadoop.FileSystem"
            "fs.alluxio-ft.impl" "alluxio.hadoop.FaultTolerantFileSystem"
            "fs.AbstractFileSystem.alluxio.impl" "alluxio.hadoop.AlluxioFileSystem"}})

(spit "data.edn" (with-out-str (pr (first gs))))
