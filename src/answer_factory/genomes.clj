(ns answer-factory.genomes
  (:require [clojure.zip :as zip]))


(defn empty-zipper?
  "returns true if the zipper passed in refers to an empty seq"
  [z]
  (empty? (zip/root z)))


(defn insert-at-head
  "inserts the specified item at the head of the zipper, returning a modified zipper"
  [z item]
  (if (empty-zipper? z)
    (zip/replace z (list item))
    (-> z
        zip/down
        (zip/insert-left item)
        zip/up)))