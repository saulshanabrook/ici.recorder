(ns ici-recorder.parquet.write-support
  (:require [clojure.spec :as s]

            [ici-recorder.parquet.schema.spec :as p]
            [ici-recorder.parquet.add-data :refer [add-record-forms]]
            [ici-recorder.parquet.schema.interop :refer [->schema]]))

(defn ->write-context [schema]
  (org.apache.parquet.hadoop.api.WriteSupport$WriteContext.
    (->schema schema)
    {}))

(s/fdef ->write-context
  :args (s/cat :schema ::p/schema))

(defmacro ->write-support [schema-form]
  (let [record-symbol (gensym 'record)
        schema (eval schema-form)
        record-consumer-symbol (with-meta 'rc {:tag 'org.apache.parquet.io.api.RecordConsumer})]
    `(let [record-consumer# (atom nil)]
      (proxy [org.apache.parquet.hadoop.api.WriteSupport] []

        (~'init [configuration#]
          ;; pass in the unevaluted schema (schema-form)
          ;; or else `array-map`s get turned into regular maps
          (->write-context ~schema-form))

        (~'prepareForWrite [new-record-consumer#]
          (reset! record-consumer# new-record-consumer#))

        (~'write [~record-symbol]
          (let [~record-consumer-symbol @record-consumer#]
            ~@(add-record-forms schema record-symbol)))))))
