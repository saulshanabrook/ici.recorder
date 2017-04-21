(ns ici-recorder.clojush
  (:require [clojure.spec :as s]
            [ici-recorder.parquet.write :refer [write ->hadoop-config]]
            [ici-recorder.parquet.spec :as hadoop-s]
            [environ.core])
  (:import (org.apache.hadoop.fs)
           (java.net)))

(def -hadoop-config
  (->hadoop-config {}))

(def ^java.net.URI -base-uri (java.net.URI. (environ.core/env :clojush-parquet-uri "")))

; create the file at a temporary location, then move it.
; this is so that partially written files aren't in the path and won't break reading
(defn -write [write-support ^String path form]
  (let [uri (org.apache.hadoop.fs.Path. (.resolve -base-uri path))
        tmp-uri (org.apache.hadoop.fs.Path. (.resolve -base-uri (str "tmp/" path)))]
    (write
      write-support
      form
      {:path tmp-uri
       :write-mode "OVERWRITE"
       :validation true
       :compression-codec "SNAPPY"
       :hadoop-config -hadoop-config})

    (let [^org.apache.hadoop.fs.FileSystem fs (.getFileSystem (org.apache.hadoop.fs.Path. -base-uri) -hadoop-config)]
      (.mkdirs fs (.getParent uri))
      (assert (.rename fs tmp-uri uri))
      (.close fs))))

(s/fdef -write
  :args (s/cat :write-support ::hadoop-s/write-support
               :path any?
               :form any?))


(defn record-run [write-support uuid config]
  (-write
    write-support
    (str "configs/uuid=" uuid "/data.parquet")
    config))

(defn record-generation [write-support config-uuid index generation]
  (-write
    write-support
    (str "generations/run-uuid=" config-uuid "/" "index=" index "/data.parquet")
    generation))
