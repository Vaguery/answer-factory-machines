(ns answer-factory.genomes.plush
  (:require [clojure.zip :as zip]))


;; the loop is initialized with
;; - depth = 0
;; - openings = 0
;; - closings = 0
;;

(defn plush->push
  "translates"
  ([genome]
    (plush->push genome {}))
  ([genome branch-map]
  genome))

