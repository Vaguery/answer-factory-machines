(ns answer-factory.genomes
  (:require [clojure.zip :as zip]))


(defn empty-zipper?
  "returns true if the zipper's root is an empty list"
  [z]
  (empty? (zip/root z)))


(defn rewind
  "moves the cursor of a zipper to the global head"
  [z]
  (zip/down (zip/seq-zip (zip/root z))))


(defn put-left
  "inserts the item to the left of the cursor, unless it's nil"
  [z item]
  (if (nil? item)
    z
    (zip/insert-left z item)))


(defn put-right
  "inserts the item to the right of the cursor, unless it's nil"
  [z item]
  (if (nil? item)
    z
    (zip/insert-right z item)))


(defn edit-with
  "takes a gene tuple and applies it to the zipper z, returning that"
  [tuple z]
  (let [from-put [(:from tuple) (:put tuple)]
        item (:item tuple)]
    (cond
      (empty-zipper? z)
        (if (nil? item)
          z
          (zip/down (zip/seq-zip (list item))))
      (= from-put [:head :L])
        (-> z
            rewind
            (put-left item))
      (= from-put [:head :R])
        (-> z
            rewind
            (put-right item))
      :else z
      )
  ))


(defn zip->push
  "takes a vector of tuples, and returns the Push program (as a vector)"
  [tuples]
  tuples)