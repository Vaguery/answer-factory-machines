(ns answer-factory.genomes.bb8
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



(defn count-cursorpoints
  "returns the number of valid cursor positions in the zipper, not counting the root or end positions (that is, from the head to the tail only)"
  [z]
  (loop [c 1
         loc (rewind z)]
    (if (zip/end? (zip/next loc))
      c
      (recur
        (inc c)
        (zip/next loc)))))



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
    (root? (zip/prev z))
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
  "moves the cursor up, or to the head if it's already in the main list"
  [z]
  (cond
    (root? z)
      (rewind z)
    (root? (zip/up z))
      (rewind z)
    (zip/end? z)
      (rewind z)
    :else
      (if (nil? (zip/up z))
        (rewind z)
        (zip/up z))
      ))


(defn goto-rightmost
  "moves the cursor to the rightmost position of the current subtree, or the rightmost of the main tree if at root or end"
  [z]
  (cond
    (root? z)
      (-> z rewind zip/rightmost)
    (zip/end? z)
      (-> z rewind zip/rightmost)
    :else
      (zip/rightmost z)))




(defn step-down
  "moves the cursor down, or leave it where it is if it isn't sitting on a branch; if at the root or end, move it to the head first"
  [z]
  (cond
    (root? z)
      (rewind z)
    (zip/end? z)
      (rewind z)
    :else
      (if (nil? (zip/down z))
        z
        (zip/down z))))



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


(defn scroll-to-index
  "takes a zipper and an integer, and steps (using zip/next) to the indicated 0-based index, starting at the head"
  [z idx]
  (nth (iterate zip/next (rewind z)) idx))


(defn jump-to
  "calculates index in Push style (modulo count-cursorpoints) and scrolls to that position"
  [z idx]
  (let [i (mod idx (count-cursorpoints z))]
    (scroll-to-index z i)))


(defn move-cursor
  "takes a zipper and a move instruction, and returns the modified zipper"
  [z m]
  (cond 
    (integer? m)   (jump-to z m)
    (= m :head)    (rewind z)
    (= m :tail)    (fast-forward z)
    (= m :subhead) (goto-leftmost z)
    (= m :append)  (goto-rightmost z)
    (= m :left)    (wrap-left z)
    (= m :right)   (wrap-right z)
    (= m :prev)    (wrap-prev z)
    (= m :next)    (wrap-next z)
    (= m :up)      (step-up z)
    (= m :down)    (step-down z)
    :else z))


(defn apply-gene
  "takes a gene tuple and applies it to the zipper z, returning that"
  [tuple z]
  (let [mv   (:from tuple)
        put  (:put tuple)
        item (:item tuple)]
    (cond (empty-zipper? z)
            (if (nil? item) z (zip/down (zip/seq-zip (list item))))
          (= put :L)
            (put-left (move-cursor z mv) item)
          (= put :R)
            (put-right (move-cursor z mv) item)
          :else
            z)))


(defn bb8->push
  "takes a vector of tuples, and returns the Push program (as a vector)"
  [tuples]
  (into []
    (zip/root
      (reduce
        (fn [z g] (apply-gene g z))
        (zip/next (zip/seq-zip '()))
        tuples))))



