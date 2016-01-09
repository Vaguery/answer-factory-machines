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


(defn wrap-left
  "moves the cursor to the left one step, or to the rightmost if it's already at the left"
  [z]
  (cond
    (root? z)
      (zip/rightmost (zip/next z))
    (nil? (zip/left z))
      (zip/rightmost z)
    :else
      (zip/left z)))


(defn wrap-right
  "moves the cursor to the right one step, or to the rightmost if it's already at the right"
  [z]
  (cond
    (root? z)
      (zip/right (zip/next z))
    (nil? (zip/right z))
      (zip/leftmost z)
    :else
      (zip/right z)))


(defn wrap-prev
  "moves the cursor to the previous position, or to the tail if it's already at the head"
  [z]
  (cond
    (root? z)
      (fast-forward z)
    (zip/end? z)
      (fast-forward z)
    (= z (rewind z))
      (fast-forward z)
    :else
      (zip/prev z)))



(defn wrap-next
  "moves the cursor to the next position, or to the head if it's already at the tail"
  [z]
  (cond
    (root? z)
      (zip/next (zip/down z))
    (zip/end? z)
      (rewind z)
    (= z (fast-forward z))
      (rewind z)
    :else
      (zip/next z)))


(defn step-up
  "moves the cursor up position, or to the head if it's already in the main list"
  [z]
  (cond
    (root? z)
      (rewind z)
    (root? (zip/up z))
      (rewind z)
    (zip/end? z)
      (rewind z)
    :else
      (zip/up z)))




(defn put-left
  "inserts the item to the left of the cursor, unless it's nil"
  [z item]
  (if (nil? item)
    z
    (if (nil? (zip/node z))
      (zip/replace z item)
      (zip/insert-left z item))))


(defn put-right
  "inserts the item to the right of the cursor, unless it's nil"
  [z item]
  (if (nil? item)
    z
    (if (nil? (zip/node z))
      (zip/replace z item)
      (zip/insert-right z item))))


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
        [:left :L]
          (-> z wrap-left (put-left item))
        [:left :R]
          (-> z wrap-left (put-right item))
        [:right :L]
          (-> z wrap-right (put-left item))
        [:right :R]
          (-> z wrap-right (put-right item))
        [:prev :L]
          (-> z wrap-prev (put-left item))
        [:prev :R]
          (-> z wrap-prev (put-right item))
        [:next :L]
          (-> z wrap-next (put-left item))
        [:next :R]
          (-> z wrap-next (put-right item))
        [:up :L]
          (-> z step-up (put-left item))
        [:up :R]
          (-> z step-up (put-right item))
        z))))


(defn zip->push
  "takes a vector of tuples, and returns the Push program (as a vector)"
  [tuples]
  tuples)