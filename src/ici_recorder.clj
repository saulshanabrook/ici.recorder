(ns ici-recorder
  (:require [potemkin]
            [ici-recorder.clojush]))

(potemkin/import-vars
  [ici-recorder.clojush
    record-run
    record-generation])
