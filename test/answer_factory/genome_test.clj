(ns answer-factory.genome-test
  (:use midje.sweet)
  (:require [clojure.zip :as zip])
  (:use [answer-factory.genomes]))


(def test-zipper (zip/seq-zip '(1 2 3 (4 5 (6)))))
(def empty-zipper (zip/seq-zip '()))
(def silly-zipper (zip/seq-zip '(:foo :bar :baz)))


(fact "empty-zipper? returns true if the arg is an empty seq"
  (empty-zipper? empty-zipper) => true
  (empty-zipper? test-zipper) => false)


(fact "insert-at-head adds a new node at the root"

  test-zipper => ['(1 2 3 (4 5 (6))) nil]
  empty-zipper => ['() nil]

  (zip/root (insert-at-head empty-zipper 99)) => '(99)
  (zip/root (insert-at-head test-zipper 99)) => '(99 1 2 3 (4 5 (6)))

  (zip/root (insert-at-head (insert-at-head empty-zipper 99) 88)) =>
    '(88 99)


  (zip/root (insert-at-head
              test-zipper 
              (zip/root (insert-at-head 
                silly-zipper
                (zip/root empty-zipper))))) =>
    '(                     ;; test-zipper
      (()                  ;; empty-zipper
        :foo :bar :baz)    ;; silly-zipper
      1 2 3 (4 5 (6))) 

)
