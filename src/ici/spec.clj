(ns ici.spec
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [java-time]
            [java-time.repl])

  (:import [java.time.Duration]))

(s/def ::duration
  int?)
  ; (s/with-gen
  ;   (partial instance? java.time.Duration)
  ;   #(gen/fmap java-time/duration (gen/int))))

(s/def ::run (s/keys :req-un [::configuration]
                     :opt-un [::generations]))

(s/def ::configuration (s/keys :req-un [::problem-file ::arguments ::parameters ::clojush-version ::initialization-time ::git-commit]))
                              ;  :opt-un [::git-commit]))
(s/def ::problem-file string?)
(def test-keywords (set (map first (s/exercise keyword? 20))))
(s/def ::arguments (s/map-of test-keywords string?))
(s/def ::parameters (s/map-of test-keywords string?))
(s/def ::clojush-version string?)
(s/def ::initialization-time ::duration)
(s/def ::git-commit string?)

(s/def ::generations (s/coll-of ::generation))
(s/def ::generation (s/keys :req-un [::inidividuals ::reproduction-time ::fitness-time ::other-time ::report]))
                            ; :opt-un [::report]))
(s/def ::inidividuals (s/coll-of ::individual :count 1000))
(s/def ::reproduction-time number?)
(s/def ::fitness-time ::duration)
(s/def ::other-time ::duration)

(s/def ::report (s/keys :req-un [::outcome ::best ::best-simplification ::lexicase ::report-time ::problem-specific]))
                        ; :opt-un [::best ::best-simplification ::lexicase ::report-time]));; ::problem-specific]))
(s/def ::outcome #{:success :failure :continue})
(s/def ::lexicase (s/keys :req-un [::best ::best-simplification])) ;:opt-un [::best ::best-simplification]))
(s/def ::report-time ::duration)
(s/def ::problem-specific (s/keys :req-un [::best-generalization-errors])) ;:opt-un [::best-generalization-errors]))
(s/def ::error double?)
(s/def ::errors (s/coll-of ::error :count 20))
(s/def ::best-generalization-errors ::errors)
(s/def ::individual (s/keys :req-un [::plush-genome ::program ::errors ::total-error ::normalized-error ::meta-errors ::history ::ancestors ::uuid ::parent-uuids ::genetic-operators ::is-random-replacement]))
                            ; :opt-un [::errors ::total-error ::normalized-error ::meta-errors ::history ::ancestors ::uuid ::parent-uuids ::genetic-operators ::is-random-replacement]))
(s/def ::best ::individual)
(s/def ::best-simplification ::individual)

(s/def ::plush-genome (s/coll-of ::plush-instruction-map :count 200))
(s/def ::plush-instruction-map (s/keys :req-un [::instruction ::uuid ::random-insertion ::silent ::random-closes ::parent-uuid]))
                                      ;  :opt-un [::uuid ::random-insertion ::silent ::random-closes ::parent-uuid]))
(s/def ::uuid string?)
; (s/def ::instruction string?)
(s/def ::instruction (set (map first (s/exercise string? 200))))
(s/def ::random-insertion boolean?)
(s/def ::silent boolean?)
(s/def ::random-closes int?)
(s/def ::parent-uuid ::uuid)
(s/def ::ancestors ::errors)
(s/def ::program (s/coll-of ::instruction  :count 200))
(s/def ::total-error ::error)
(s/def ::normalized-error ::error)
(s/def ::meta-errors (s/map-of #{"first" "second" "third"} ::error :count 3))
(s/def ::history ::errors)
(s/def ::parent-uuids (s/coll-of ::uuid  :count 2))
(s/def ::genetic-operators (s/coll-of (set (map first (s/exercise string? 7))) :count 2))
(s/def ::is-random-replacement boolean?)


(def configuration-gen (s/gen ::configuration))
(def generation-gen (s/gen ::generation))
