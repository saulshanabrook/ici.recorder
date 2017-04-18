;; rules for buildings forms:
;; 1. all functions that wrap forms in other forms, should take the inner
;;    forms as a list as their last argument
;; 2. If a functions returns multiple forms, it should end in `-forms`
(ns ici-recorder.parquet.add-data
  (:require [clojure.spec :as s]
            ; [taoensso.timbre :as timbre]
            ; [taoensso.timbre.profiling :as profiling :refer [defnp p] :rename {p pp}]
            ; [taoensso.timbre :refer [trace spy]]

            [ici-recorder.parquet.schema.spec :as p])
  (:import (org.apache.parquet.io.api)))

(defn add-boolean [self ^org.apache.parquet.io.api.RecordConsumer rc]
  (.addBoolean rc (boolean self)))

(defn add-integer [self ^org.apache.parquet.io.api.RecordConsumer rc]
  (.addInteger rc (int self)))

(defn add-long [self ^org.apache.parquet.io.api.RecordConsumer rc]
  (.addLong rc (long self)))

(defn add-float [self ^org.apache.parquet.io.api.RecordConsumer rc]
  (.addFloat rc (float self)))

(defn add-double [self ^org.apache.parquet.io.api.RecordConsumer rc]
  (.addDouble rc (double self)))

(defn add-string [self ^org.apache.parquet.io.api.RecordConsumer rc]
  (->> self
    ((if (keyword? self) name str))
    org.apache.parquet.io.api.Binary/fromString
    (.addBinary rc)))

(defn add-instant [self ^org.apache.parquet.io.api.RecordConsumer rc]
  (add-long self rc))


(defn rc-form
  [method & arg-forms]
  `(~method ~'rc ~@arg-forms))

(defn group-forms
  [inner-forms]
  `(~(rc-form '.startGroup)
    ~@inner-forms
    ~(rc-form '.endGroup)))

(defn field-forms
  [name_
   index
   inner-forms]
  `(~(rc-form '.startField name_ index)
    ~@inner-forms
    ~(rc-form '.endField name_ index)))

(s/fdef field-forms
  :args (s/cat :name string?
               :index int?
               :inner-forms (s/nilable (s/coll-of any?))))

(defn when-forms
  [checker-form inner-forms]
  (if (nil? checker-form)
    inner-forms
    `((when ~checker-form
        ~@inner-forms))))

(declare add-value-forms)

(defn add-group-fields-forms
  [group
   map-symbol]
  (->> group
    (map-indexed
      (fn [i [name-key [required? type]]]
        (let [name_ (name name-key)
              form-symbol (gensym 'form)]
          `(let [~form-symbol (~name-key ~map-symbol)]
            ~@(when-forms (when-not required? `(some? ~form-symbol))
                (field-forms name_ i
                  (add-value-forms type form-symbol)))))))))

(s/fdef add-group-fields-forms
  :args (s/cat :group ::p/group :map-symbol symbol?))


(defmulti add-value-forms
  (fn [type form-symbol]
    (first (s/conform ::p/type type))))

(s/fdef add-value-forms
  :args (s/cat :type ::p/type :form any?))

(defmethod add-value-forms :not-nested
  [not-nested
   form-symbol]
  (let [method-symbol (symbol "ici-recorder.parquet.add-data" (str "add-" (name not-nested)))]
    `((~method-symbol ~form-symbol ~'rc))))

(defmethod add-value-forms :group
  [group
   map-symbol]
  (when-forms `(some? ~map-symbol)
    (group-forms
      (add-group-fields-forms group map-symbol))))



(defmethod add-value-forms :list
  [[item-required? item-type]
   list-symbol]
  (let [item-symbol (gensym 'item)]
    (group-forms
      (when-forms `(not-empty ~list-symbol)
        (field-forms "list" 0
          `((doseq [~item-symbol ~list-symbol]
              ~@(group-forms
                  (when-forms (when-not item-required? `(some? ~item-symbol))
                     (field-forms "element" 0
                       (add-value-forms item-type item-symbol)))))))))))


(defmethod add-value-forms :map
  [[key-type value-required? value-type]
   map-symbol]
  (let [key-symbol (gensym 'key)
        value-symbol (gensym 'value)]
    (group-forms
      (when-forms `(not-empty ~map-symbol)
        (field-forms  "key_value" 0
           `((doseq [[~key-symbol ~value-symbol] ~map-symbol]
               ~@(group-forms
                   (concat
                     (field-forms "key" 0
                         (add-value-forms key-type key-symbol))
                     (when-forms (when-not value-required? `(some? ~value-symbol))
                        (field-forms "value" 1
                          (add-value-forms value-type value-symbol))))))))))))



(defn add-record-forms
    [schema
     form-symbol]
  `(~(rc-form '.startMessage)
    ~@(add-group-fields-forms schema form-symbol)
    ~(rc-form '.endMessage)))

(s/fdef add-record-forms
  :args (s/cat :schema ::p/schema :form-symbol symbol?))
