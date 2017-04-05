(ns ici-recorder
  (:require [potemkin]
            [taoensso.timbre]
            [ici-recorder.clojush]))

(taoensso.timbre/set-level! :info)
(potemkin/import-vars
  [ici-recorder.clojush
    record-run
    record-generation])
