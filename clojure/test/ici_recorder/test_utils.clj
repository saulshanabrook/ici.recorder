(ns ici-recorder.test-utils
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [environ.core]

            [ici-recorder.parquet.write-support :refer [->write-support]]))
(s/def ::duration
  int?)
  ; (s/with-gen
  ;   (partial instance? java.time.Duration)
  ;   #(gen/fmap java-time/duration (gen/int))))
(def p-duration :integer)

(s/def ::problem-file string?)
(def p-problem-file :string)
(def test-keywords (set (map first (s/exercise keyword? 20))))
(s/def ::parameters (s/map-of test-keywords any?))
(def p-parameters [:string false :string])
(s/def ::clojush-version string?)
(def p-clojush-version :string)
(s/def ::initialization-time ::duration)
(def p-initialization-time p-duration)
(s/def ::git-commit string?)
(def p-git-commit :string)


(s/def ::configuration (s/keys :req-un [::problem-file ::parameters ::clojush-version ::initialization-time]
                               :opt-un [::git-commit]))
(def p-configuration
  (array-map
   :problem-file [true p-problem-file]
   :parameters [true p-parameters]
   :clojush-version [true p-clojush-version]
   :initialization-time [true p-initialization-time]
   :git-commit [false p-git-commit]))

(s/def ::error double?)
(def p-error :double)
(s/def ::errors (s/coll-of ::error :count 20))
(def p-errors [true p-error])
(s/def ::uuid string?)
(def p-uuid :string)
; (s/def ::instruction string?)
(s/def ::instruction (set (map first (s/exercise any? 200))))
(def p-instruction :string)
(s/def ::random-insertion boolean?)
(def p-random-insertion :boolean)
(s/def ::silent boolean?)
(def p-silent :boolean)
(s/def ::random-closes int?)
(def p-random-closes :integer)
(s/def ::parent-uuid ::uuid)
(def p-parent-uuid p-uuid)
(s/def ::ancestors ::errors)
(def p-ancestors p-errors)
(s/def ::program (s/coll-of ::instruction  :count 200))
(def p-program [true p-instruction])
(s/def ::total-error ::error)
(def p-total-error p-error)
(s/def ::normalized-error ::error)
(def p-normalized-error p-error)
(s/def ::meta-errors (s/map-of #{"first" "second" "third"} ::error :count 3))
(def p-meta-errors [:string true p-error])
(s/def ::history ::errors)
(def p-history p-errors)
(s/def ::parent-uuids (s/coll-of ::uuid  :count 2))
(def p-parent-uuids [true p-uuid])
(s/def ::genetic-operators (s/coll-of (set (map first (s/exercise string? 7))) :count 2))
(def p-genetic-operators :string)
(s/def ::is-random-replacement boolean?)
(def p-is-random-replacement :boolean)
(s/def ::plush-instruction-map (s/keys :req-un [::instruction]
                                       :opt-un [::uuid ::random-insertion ::silent ::random-closes ::parent-uuid]))
(def p-plush-instruction-map
  (array-map
   :instruction [true p-instruction]
   :uuid [false p-uuid]
   :random-insertion [false p-random-insertion]
   :silent [false p-silent]
   :random-closes [false p-random-closes]
   :parent-uuid [false p-parent-uuid]))
(s/def ::plush-genome (s/coll-of ::plush-instruction-map :count 200))
(def p-plush-genome [true p-plush-instruction-map])

(s/def ::individual (s/keys :req-un [::plush-genome ::program]
                            :opt-un [::errors ::total-error ::normalized-error ::meta-errors ::history ::ancestors ::uuid ::parent-uuids ::genetic-operators ::is-random-replacement]))
(def p-individual
  (array-map
   :plush-genome [true p-plush-genome]
   :program [true p-program]
   :errors [false p-errors]
   :total-error [false p-total-error]
   :normalized-error [false p-normalized-error]
   :meta-errors [false p-meta-errors]
   :history [false p-history]
   :ancestors [false p-ancestors]
   :uuid [false p-uuid]
   :parent-uuids [false p-parent-uuids]
   :genetic-operators [false p-genetic-operators]
   :is-random-replacement [false p-is-random-replacement]))

(s/def ::individuals (s/coll-of ::individual :count (Integer/parseInt (environ.core/env :n-individuals "10"))))
(def p-individuals [true p-individual])
(s/def ::reproduction-time ::duration)
(def p-reproduction-time p-duration)
(s/def ::fitness-time ::duration)
(def p-fitness-time p-duration)
(s/def ::other-time ::duration)
(def p-other-time p-duration)

(s/def ::best-generalization-errors ::errors)
(def p-best-generalization-errors p-errors)
(s/def ::best ::individual)
(def p-best p-individual)
(s/def ::best-simplification ::individual)
(def p-best-simplification p-individual)
(s/def ::outcome #{:success :failure :continue})
(def p-outcome :string)
(s/def ::lexicase (s/keys :opt-un [::best ::best-simplification]))
(def p-lexicase
  (array-map
   :best [false p-best]
   :best-simplification [false p-best-simplification]))
(s/def ::report-time ::duration)
(def p-report-time p-duration)
(s/def ::problem-specific (s/keys :opt-un [::best-generalization-errors]))
(def p-problem-specific
  (array-map
   :best-generalization-errors [false p-best-generalization-errors]))

(s/def ::report (s/keys :req-un [::outcome]
                        :opt-un [::best ::best-simplification ::lexicase ::report-time ::problem-specific]))
(def p-report
  (array-map
   :outcome [true p-outcome]
   :best [false p-best]
   :best-simplification [false p-best-simplification]
   :lexicase [false p-lexicase]
   :report-time [false p-report-time]
   :problem-specific [false p-problem-specific]))

(s/def ::generation (s/keys :req-un [::individuals ::reproduction-time ::fitness-time ::other-time]
                            :opt-un [::report]))
(def p-generation
  (array-map
   :individuals [true p-individuals]
   :reproduction-time [true p-reproduction-time]
   :fitness-time [true p-fitness-time]
   :other-time [true p-other-time]
   :report [false p-report]))

(def configuration-gen (s/gen ::configuration))
(def generation-gen (s/gen ::generation))
(def configuration-write-support (->write-support p-configuration))
(def generation-write-support (->write-support p-generation))
