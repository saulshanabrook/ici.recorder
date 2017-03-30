(ns ici-recorder.parquet-test
  (:require [ici-recorder.parquet]
            [clojure.test :as t]))


(defn ->s [f]
  (ici-recorder.parquet/->schema f "name" org.apache.parquet.schema.Type$Repetition/OPTIONAL))

(defrecord Person [name])

(t/deftest collections-schema
  (t/testing "for collections"
    (t/testing "all work"
      (t/testing "set"
        (t/is (->s #{true})))
      (t/testing "vector"
        (t/is (->s [true])))
      (t/testing "list"
        (t/is (->s (list true))))
      (t/testing "lazy seq"
        (t/is (->s (lazy-seq [false])))))
    (t/testing "all same"
      (t/is (apply = (map ->s #{#{false true}
                                [false false]
                                (list true true)
                                (lazy-seq [true false])}))))
    (t/testing "empty"
      (t/is (nil? (->s []))))
    (t/testing "nil"
      (t/is (nil? (->s [nil]))))
    (t/testing "convert to string if different types"
      (t/is (= (->s [1 true])
               (->s ["string"])))

      (t/testing "for inner types"
        (t/is (= (->s [{:hi 1} {:hi true}])
                 (->s [{:hi ""}]))))
      (t/testing "for collection and primitive"
        (t/is (= (->s [{:hi 1} 12])
                 (->s [""])))
        (t/is (= (->s [{:hi [1]}
                       {:hi 1}])
                 (->s [{:hi ""}]))))))
  (t/testing "for maps"
    (t/testing "not same as collection"
      (t/is (not= (->s [true])
                  (->s {:key true}))))
    (t/testing "nil keys removed"
      (t/is (= (->s {:a nil})
               (->s {}))))
    (t/testing "empty collections work as values"
      (t/is (= (->s {:a []})
               (->s {})))))
  (t/testing "for records"
    (t/testing "works"
      (t/is (->s (Person. "hi"))))
    (t/testing "same as map"
      (t/is (= (->s (Person. "hi"))
               (->s {:name "hi"}))))))

(t/deftest literals-schema
  (t/testing "numbers"
    (t/testing "integer and long"
      (t/testing "work"
        (t/is (->s (int 1)))
        (t/is (->s (long 2))))
      (t/testing "are different"
        (t/is (not= (->s (int 1))
                    (->s (long 1)))))))

  (t/testing "symbols"
    (t/testing "works"
      (t/is (->s (symbol "hi" "there"))))
    (t/testing "turns into string"
      (t/is (= (->s (symbol "hi" "there"))
               (->s "a")))))

  (t/testing "keywords"
    (t/testing "works"
      (t/is (->s :test)))
    (t/testing "turns into string"
      (t/is (= (->s :test)
               (->s "a")))))
  (t/testing "records"
    (t/testing "works"
      (t/is (->s #(%))))
    (t/testing "turns into string"
      (t/is (= (->s #(%))
               (->s "a")))))

  (t/testing "byte array"
    (t/testing "works"
      (t/is (->s (byte-array []))))
    (t/testing "turns into string"
      (t/is (= (->s (byte-array []))
               (->s "a")))))

  (t/testing "uuid"
    (t/testing "works"
      (t/is (->s (java.util.UUID/randomUUID))))
    (t/testing "turns into string"
      (t/is (= (->s (java.util.UUID/randomUUID))
               (->s "a"))))))
