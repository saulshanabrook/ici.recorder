(ns ici.clojush
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]))

;
(s/def ::run (s/keys :req [::configuration]
                     :opt [::generations]))

(gen/generate (s/gen ::generation))

(s/def ::configuration (s/keys :req [::problem-file ::arguments ::parameters ::clojush-version ::initialization-time]
                               :opt [::git-commit]))
(s/def ::problem-file string?)
(s/def ::arguments (s/map-of keyword? string?))
(s/def ::parameters (s/map-of keyword? string?))
(s/def ::clojush-version string?)
(s/def ::initialization-time number?)
(s/def ::git-commit string?)

(s/def ::generations (s/coll-of ::generation))
(s/def ::generation (s/keys :req [::inidividuals ::reproduction-time ::fitness-time ::other-time]
                            :opt [::report]))
(s/def ::inidividuals (s/coll-of ::individual))
(s/def ::reproduction-time number?)
(s/def ::fitness-time number?)
(s/def ::other-time number?)

(s/def ::report (s/keys :req [::outcome]
                        :opt [::best ::best-simplification ::lexicase ::report-time ::problem-specific]))
(s/def ::outcome #{:success :failure :continue})
(s/def ::best ::individual)
(s/def ::best-simplification ::individual)
(s/def ::lexicase (s/keys :opt [::best ::best-simplification]))
(s/def ::report-time number?)
(s/def ::problem-specific (s/keys :opt [::best-generalization-errors]))
(s/def ::best-generalization-errors ::errors)
(s/def ::errors (s/coll-of ::error))
(s/def ::individual (s/keys :req [::plush-genome ::program]
                            :opt [::errors ::total-error ::normalized-error ::meta-errors ::history ::ancestors ::uuid ::parent-uuids ::genetic-operators ::is_random-replacement]))

(s/def ::plush-genome (s/coll-of ::plush-instruction-map))
(s/def ::plush-instruction-map (s/keys :req [::instruction]
                                       :opt [::uuid ::random-insertion ::silent ::random-closes ::parent-uuid]))
(s/def ::uuid string?)
(s/def ::instruction string?)
(s/def ::random-insertion boolean?)
(s/def ::silent boolean?)
(s/def ::random-closes int?)
(s/def ::parent-uuid ::uuid)
(s/def ::ancestors ::errors)
(s/def ::program (s/coll-of ::instruction))
(s/def ::total-error ::error)
(s/def ::normalized-error ::error)
(s/def ::meta-errors (s/map-of string? ::error))
(s/def ::history ::errors)
(s/def ::parent-uuids (s/coll-of ::uuid))
(s/def ::genetic-operators (s/coll-of string?))
(s/def ::is_random-replacement boolean?)
(s/def ::error number?)
