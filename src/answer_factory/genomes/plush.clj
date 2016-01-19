(ns answer-factory.genomes.plush
  (:require [clojure.zip :as zip])
  (:require [answer-factory.genomes.bb8 :as bb8]))



(defn clean-insert
  "inserts the given item at the current cursor position in the indicated zipper; if the cursor is in an empty sub-list, it removes the `nil` placeholder"
  [z item]
  (if (nil? (zip/node z))
    (zip/replace z item)
    (-> z (zip/insert-right item) zip/right)))


(defn empty-program
  "returns a seq-zip on an empty list, with the cursor advanced into the list"
  []
  (zip/next (zip/seq-zip '())))


(defn append-token
  [z item]
  (clean-insert z item))


(defn append-as-branch
  [z item]
  (-> (clean-insert z '())
      zip/next
      (clean-insert item)))


(defn move-up-safely
  [program]
  (if (bb8/root? (zip/up program))
      (zip/rightmost program)
      (-> (zip/up program)
          zip/rightmost)))


(defn append-as-sibling
  [z item]
  (-> (move-up-safely z)
      (clean-insert '())
      zip/down
      (clean-insert item)))




; - increment close-count
; - (noop_open-paren)
;   - create a branch (push :END)
; - noop_delete_prev_paren_pair
;   - ?
; - item is in branching table
;   - push new branch stuff onto branch stack
; - append an item
; - consume closes if (pos? close-count)
;   - move up, consuming branches, that many times
;   - e.g. [:END :BRANCH :END]


; 1. check for tree-editing instructions
; 2. implement branching items
; 3. implement boring items
; 4. do "closing"





(defn close-up-one
  [[program branch-stack]]
  (let [outcome (first branch-stack)]
    (vector
      (cond
        (nil? outcome) program
        (= outcome :BRANCH)
          (-> (move-up-safely program) (zip/insert-right '()) zip/right zip/down)
        :else (move-up-safely program))
    (drop 1 branch-stack))))





