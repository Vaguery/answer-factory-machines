(ns answer-factory.genomes.plush-test
  (:use midje.sweet)
  (:use answer-factory.genomes.plush)
  (:require [clojure.zip :as zip])
  (:require [answer-factory.genomes.bb8 :as bb8])
  (:use clojure.pprint))


;; noop_open_paren
;; noop_delete_prev_paren_pair
;; :parentheses metadata
;; :close values
;; :close-open values
;; :close count
;; :silent metadata


(fact "clean-insert adds a thing to a zipper without leaving the nil placeholder in an empty sub-list"
  (zip/root (zip/insert-left (zip/next (zip/seq-zip '())) 99)) =>
    '(99 nil) ;; because of the placeholder nil
  (zip/root (clean-insert (zip/next (zip/seq-zip '())) 99)) =>
    '(99))


(fact "empty-program returns an empty program zipper"
  (zip/root (empty-program)) => '()
  (zip/node (empty-program)) => nil
  (bb8/root? (empty-program)) => false)


;; adding a single item to an existing program

;; basic cases

(fact "append-token adds an item to the program without branching"
  (zip/root
    (append-token (empty-program) 99)) => '(99)
  (zip/root
    (append-token (bb8/fast-forward (zip/seq-zip '(1 2 (3 4)))) 99)) =>
      '(1 2 (3 4 99)))


(fact "append-as-branch adds an item to the program in a new branch"
  (zip/root
    (append-as-branch (empty-program) 99)) => '((99))
  (zip/root
    (append-as-branch (bb8/fast-forward (zip/seq-zip '(1 2 (3 4)))) 99)) =>
      '(1 2 (3 4 (99))))


(fact "append-as-sibling adds an item to the program in a new branch"
  (zip/root
    (append-as-sibling
      (append-as-branch (empty-program) 88)
      99)) => '((88) (99))
  (zip/root
    (append-as-sibling
      (append-as-branch (bb8/fast-forward (zip/seq-zip '(1 2 (3 4)))) 88)
      99)) =>
      '(1 2 (3 4 (88) (99))))


;; genes

;; fixture

(def little-tree
  (append-as-branch (bb8/fast-forward (zip/seq-zip '(1 2 (3 4)))) 99))


(fact "a gene with no closes and an empty branch stack"
  (zip/root
    (apply-gene [(empty-program) []] {:item :foo :close 0} {})) =>
      '(:foo)
  (zip/root
    (apply-gene [little-tree []] {:item :foo :close 0} {})) =>
      '(1 2 (3 4 (99 :foo))))


(defn new-node-helper
  [tree-vector]
  (zip/node (first tree-vector)))


(defn new-root-helper
  [tree-vector]
  (zip/root (first tree-vector)))


(fact "move-up-safely works as intended"
  (zip/node little-tree) => 99
  (zip/node (move-up-safely little-tree)) => '(99)
  (zip/node (move-up-safely
              (move-up-safely little-tree))) => '(3 4 (99))
  (zip/node (move-up-safely
              (move-up-safely
                (move-up-safely little-tree)))) => '(3 4 (99))
  (zip/node (move-up-safely
              (move-up-safely
                (move-up-safely
                  (move-up-safely little-tree))))) => '(3 4 (99)))


(fact "close-up-one does nothing if the branch-stack is empty"
  (new-node-helper (close-up-one [little-tree '()])) => 99
  (new-node-helper (close-up-one [(zip/up little-tree) '()])) => '(99))



(fact "close-up-one pops one item off the branch-stack if possible"
  (second (close-up-one [little-tree '()])) => '()
  (second (close-up-one [little-tree '(:END)])) => '()
  (second (close-up-one [little-tree '(:END :foo)])) => '(:foo))


(fact "close-up-one moves the cursor up a level (until root) if the branch-stack ends in :END"
  (new-node-helper (close-up-one [little-tree '(:END)])) => '(99)
  (new-node-helper (close-up-one [(zip/up little-tree) '(:END)])) => '(3 4 (99)))



(fact "close-up-one moves the cursor up a level (until root) and starts a sibling if the branch-stack ends in :BRANCH"
  (new-root-helper (close-up-one [little-tree '(:BRANCH)])) => '(1 2 (3 4 (99) ()))
  (new-node-helper (close-up-one [little-tree '(:BRANCH)])) => nil
  
  (new-root-helper
    (close-up-one
      [(zip/up little-tree) '(:BRANCH)])) => '(1 2 (3 4 (99)) ())
  (new-node-helper
    (close-up-one
      [(zip/up little-tree) '(:BRANCH)])) => nil)







