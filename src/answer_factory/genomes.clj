(ns answer-factory.genomes
  (:require [clojure.zip :as zip]))


(defn empty-zipper?
  "returns true if the zipper's root is an empty list"
  [z]
  (empty? (zip/root z)))


(defn root?
  "returns true if the cursor is at the root"
  [z]
  (= (zip/root z) (zip/node z)))


(defn rewind
  "moves the cursor of a zipper to the global head"
  [z]
  (zip/down (zip/seq-zip (zip/root z))))


(defn fast-forward
  "moves the cursor of a zipper to the global tail"
  [z]
  (loop [loc z]
    (if (zip/end? (zip/next loc))
      loc
      (recur (zip/next loc)))))


(defn goto-leftmost
  "moves the cursor to the leftmost item in the sublist, or the head if at root or end"
  [z]
  (let [lefty (zip/leftmost z)]
    (if (root? lefty)
      (zip/down lefty)
      lefty)))


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
    (if (empty-zipper? z)
        (if (nil? item)
          z
          (zip/down (zip/seq-zip (list item))))
      (condp = from-put
        [:head :L]
          (-> z rewind (put-left item))
        [:head :R]
          (-> z rewind (put-right item))
        [:tail :L]
          (-> z fast-forward (put-left item))
        [:tail :R]
          (-> z fast-forward (put-right item))
        [:subhead :L]
          (-> z goto-leftmost (put-left item))
        [:subhead :R]
          (-> z goto-leftmost (put-right item))
        z))))


(defn zip->push
  "takes a vector of tuples, and returns the Push program (as a vector)"
  [tuples]
  tuples)