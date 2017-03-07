(ns ici.test
  (:require [ici.spec]
            [clojure.spec :as s]
            [clojure.spec.gen]
            [ici.clojush]
            [clj-uuid]
            [taoensso.nippy]
            [taoensso.timbre.profiling  :refer (pspy p defnp profile)]
            [taoensso.timbre]))

(def uuid (clj-uuid/v1))
; (def s "123")

(def gs (repeat 1 (first (taoensso.nippy/thaw-from-file "/tmp/gs.edn"))))
(defn -main []
  ; (println "i")
  ; (println (read-line))
  ; (println "NEXT")
  ; (taoensso.tufte/add-basic-println-handler! {})
  ; (taoensso.tufte.timbre/add-timbre-logging-handler! {})
  ; (println "hi")
  ; (taoensso.timbre/info "This will print")
  ; (taoensso.nippy/freeze-to-file "/tmp/c.edn" (clojure.spec.gen/generate ici.spec/configuration-gen))
  ; (taoensso.nippy/freeze-to-file "/tmp/gs.edn" (repeatedly 1 (partial clojure.spec.gen/generate ici.spec/generation-gen))))
  (profile
    :info :App
    (do
      (ici.clojush/record-config uuid (taoensso.nippy/thaw-from-file "/tmp/c.edn"))
      (dorun (map-indexed (partial ici.clojush/record-generation uuid) gs)))))
  ; (profile :info :App ; Profile any `p` forms called during body execution
  ;   (dotimes [_ 5]
  ;     (p :get-x (get-x))
  ;     (p :get-y (get-y)))))
