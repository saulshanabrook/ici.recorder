(ns ici-recorder
  (:require [potemkin]
            [taoensso.timbre]
            [ici-recorder.clojush]))

(taoensso.timbre/set-level! :error)
(potemkin/import-vars
  [ici-recorder.clojush
    record-run
    record-generation])
