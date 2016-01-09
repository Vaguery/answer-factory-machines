(ns answer-factory.genome-test
  (:use midje.sweet)
  (:require [clojure.zip :as zip])
  (:use [answer-factory.genomes])
  (:use clojure.pprint))


;; fixtures


(def test-zipper (zip/seq-zip '(1 2 3 (4 5 (6)))))
(def empty-zipper (zip/seq-zip '()))
(def simple-zipper (zip/seq-zip '(:foo :bar :baz)))

;;                                            V cursor
(def stubby-zipper (-> (zip/seq-zip '(() () (( ))))
                        zip/next
                        zip/next
                        zip/next
                        zip/next
                        zip/next
                        zip/next
                        zip/next))


;; helpers

(fact "empty-zipper? returns true if the zipper argument is an empty seq"
  (empty-zipper? test-zipper) => false
  (empty-zipper? empty-zipper) => true)


;; movers

(fact "rewind moves the cursor of a zipper to its head (not its root!)"
  (zip/node (rewind test-zipper)) => 1

  (zip/node (rewind empty-zipper)) => nil
  (zip/end? (rewind empty-zipper)) => false

  (zip/node (rewind simple-zipper)) => :foo)


(fact "fast-forward moves the cursor to the tail (not the end!)"
  (zip/node (fast-forward test-zipper)) => 6

  (zip/node (fast-forward empty-zipper)) => nil
  (zip/end? (fast-forward empty-zipper)) => false

  (zip/node (fast-forward simple-zipper)) => :baz)


(fact "wrap-left moves the cursor one step left"
  (zip/node (wrap-left (zip/down test-zipper))) => '(4 5 (6))
  (zip/node (wrap-left (zip/next (zip/next test-zipper)))) => 1
  (zip/node (wrap-left test-zipper)) => '(4 5 (6))

  (zip/node (wrap-left empty-zipper)) => nil

  (zip/node (wrap-left (zip/down simple-zipper))) => :baz)

(fact "wrap-left stays inside a sublist"
  (let [at-5
    (-> test-zipper zip/next zip/next zip/next zip/next zip/next zip/next)]
    (zip/node at-5) => 5
    (zip/node (wrap-left at-5)) => 4
    (zip/node (wrap-left (wrap-left at-5))) => '(6)
    (zip/node (wrap-left (wrap-left (wrap-left at-5)))) => 5))



;; head moves


(fact "a tuple with :head as its move leaves the cursor at the head of the scratch zipper"
  (zip/node (edit-with {:from :head :put :L :item 99} test-zipper)) => 1
  (zip/node (edit-with {:from :head :put :L :item 99} simple-zipper)) => :foo)



(fact ":head tuples"
  (zip/root (edit-with {:from :head :put :L :item 99} test-zipper)) => 
    '(99 1 2 3 (4 5 (6)))
  (zip/root (edit-with {:from :head :put :R :item 99} test-zipper)) => 
    '(1 99 2 3 (4 5 (6)))

  (zip/root (edit-with {:from :head :put :L :item 99} empty-zipper)) => 
    '(99)
  (zip/root (edit-with {:from :head :put :R :item 99} empty-zipper)) => 
    '(99)

  (zip/root (edit-with {:from :head :put :L :item 99} simple-zipper)) => 
    '(99 :foo :bar :baz)
  (zip/root (edit-with {:from :head :put :R :item 99} simple-zipper)) => 
    '(:foo 99 :bar :baz)

  (zip/root (edit-with {:from :head :put :L :item 99} stubby-zipper)) => 
    '(99 () () (())))



(fact ":head nil tuples"
  (zip/root (edit-with {:from :head :put :L :item nil} test-zipper)) => 
    '(1 2 3 (4 5 (6)))
  (zip/root (edit-with {:from :head :put :R} test-zipper)) => 
    '(1 2 3 (4 5 (6)))

  (zip/root (edit-with {:from :head :put :L :item nil} empty-zipper)) => 
    '()
  (zip/root (edit-with {:from :head :put :R} empty-zipper)) => 
    '()

  (zip/root (edit-with {:from :head :put :L :item nil} simple-zipper)) => 
    '(:foo :bar :baz)
  (zip/root (edit-with {:from :head :put :R} simple-zipper)) => 
    '(:foo :bar :baz))



;; tail moves


(fact "a tuple with :tail as its move leaves the cursor at the tail of the zipper"
  (zip/node (edit-with {:from :tail :put :L :item 99} test-zipper)) => 6
  (zip/node (edit-with {:from :tail :put :L :item 99} simple-zipper)) => :baz)



(fact ":tail tuples"
  (zip/root (edit-with {:from :tail :put :L :item 99} test-zipper)) => 
    '(1 2 3 (4 5 (99 6)))
  (zip/root (edit-with {:from :tail :put :R :item 99} test-zipper)) => 
    '(1 2 3 (4 5 (6 99)))

  (zip/root (edit-with {:from :tail :put :L :item 99} empty-zipper)) => 
    '(99)
  (zip/root (edit-with {:from :tail :put :R :item 99} empty-zipper)) => 
    '(99)

  (zip/root (edit-with {:from :tail :put :L :item 99} simple-zipper)) => 
    '(:foo :bar 99 :baz)
  (zip/root (edit-with {:from :tail :put :R :item 99} simple-zipper)) => 
    '(:foo :bar :baz 99)

  (zip/root (edit-with {:from :tail :put :L :item 99} stubby-zipper)) => 
    '(() () ((99)))
  (zip/root (edit-with {:from :tail :put :L :item 99}
    (-> stubby-zipper zip/prev zip/prev))) => '(() () ((99)))
  )


(fact ":tail nil tuples"
  (zip/root (edit-with {:from :tail :put :L :item nil} test-zipper)) => 
    '(1 2 3 (4 5 (6)))
  (zip/root (edit-with {:from :tail :put :R} test-zipper)) => 
    '(1 2 3 (4 5 (6)))
  (zip/node (edit-with {:from :tail :put :R} test-zipper)) => 6

  (zip/root (edit-with {:from :tail :put :L :item nil} empty-zipper)) => 
    '()
  (zip/root (edit-with {:from :tail :put :R} empty-zipper)) => 
    '()
  (zip/node (edit-with {:from :tail :put :R} empty-zipper)) => '()  

  (zip/root (edit-with {:from :tail :put :L :item nil} simple-zipper)) => 
    '(:foo :bar :baz)
  (zip/root (edit-with {:from :tail :put :R} simple-zipper)) => 
    '(:foo :bar :baz)
  (zip/node (edit-with {:from :tail :put :R} simple-zipper)) => 
    :baz  )


;; subhead moves


(fact "a tuple with :subhead as its move leaves the cursor at the leftmost in its level"
  (let [at-5
    (-> test-zipper zip/next zip/next zip/next zip/next zip/next zip/next)]
    (zip/node at-5) => 5
    (zip/node (edit-with {:from :subhead :put :L :item 99} at-5)) => 4
    (zip/node (edit-with {:from :subhead :put :R :item 99} at-5)) => 4))


(fact ":subtree tuples"
  (let [at-5
    (-> test-zipper zip/next zip/next zip/next zip/next zip/next zip/next)]

    (zip/root (edit-with {:from :subhead :put :L :item 99} at-5)) => 
      '(1 2 3 (99 4 5 (6)))
    (zip/root (edit-with {:from :subhead :put :R :item 99} at-5)) => 
      '(1 2 3 (4 99 5 (6)))

    (zip/root (edit-with {:from :subhead :put :L :item 99} empty-zipper)) => 
      '(99)
    (zip/root (edit-with {:from :subhead :put :R :item 99} empty-zipper)) => 
      '(99)

    (zip/root (edit-with {:from :subhead :put :L :item 99} simple-zipper)) => 
      '(99 :foo :bar :baz)
    (zip/root (edit-with {:from :subhead :put :R :item 99} simple-zipper)) => 
      '(:foo 99 :bar :baz)

    (zip/root (edit-with {:from :subhead :put :L :item 99} stubby-zipper)) => 
      '(() () ((99)))
    (zip/root (edit-with {:from :subhead :put :L :item 99}
      (zip/prev stubby-zipper))) => '(() () (99 ()))
    (zip/root (edit-with {:from :subhead :put :L :item 99}
      (-> stubby-zipper zip/prev zip/prev))) => '(99 () () (()))
      ))


(fact ":subhead nil tuples"
  (let [at-5
    (-> test-zipper zip/next zip/next zip/next zip/next zip/next zip/next)]

    (zip/root (edit-with {:from :subhead :put :L :item nil} at-5)) => 
      '(1 2 3 (4 5 (6)))
    (zip/root (edit-with {:from :subhead :put :R} at-5)) => 
      '(1 2 3 (4 5 (6)))
    (zip/node (edit-with {:from :subhead :put :R} at-5)) => 4

    (zip/root (edit-with {:from :subhead :put :L :item nil} empty-zipper)) => 
      '()
    (zip/root (edit-with {:from :subhead :put :R} empty-zipper)) => 
      '()
    (zip/node (edit-with {:from :subhead :put :R} empty-zipper)) => '()  

    (zip/root (edit-with {:from :subhead :put :L :item nil} simple-zipper)) => 
      '(:foo :bar :baz)
    (zip/root (edit-with {:from :subhead :put :R} simple-zipper)) => 
      '(:foo :bar :baz)
    (zip/node (edit-with {:from :subhead :put :R} simple-zipper)) => 
      :foo  ))


;; left moves


(fact "a tuple with :left as its move leaves the cursor in the right place"
  (let [at-5
    (-> test-zipper zip/next zip/next zip/next zip/next zip/next zip/next)]
    (zip/node at-5) => 5
    (zip/node (edit-with {:from :left :put :L :item 99} at-5)) => 4
    (zip/node (edit-with {:from :left :put :R :item 99} at-5)) => 4))


(fact ":left tuples"
  (let [at-5
    (-> test-zipper zip/next zip/next zip/next zip/next zip/next zip/next)]

    (zip/root (edit-with {:from :left :put :L :item 99} at-5)) => 
      '(1 2 3 (99 4 5 (6)))
    (zip/root (edit-with {:from :left :put :R :item 99} at-5)) => 
      '(1 2 3 (4 99 5 (6)))

    (zip/root (edit-with {:from :left :put :L :item 99} empty-zipper)) => 
      '(99)
    (zip/root (edit-with {:from :left :put :R :item 99} empty-zipper)) => 
      '(99)

    (zip/root (edit-with {:from :left :put :L :item 99} simple-zipper)) => 
      '(:foo :bar 99 :baz)
    (zip/root (edit-with {:from :left :put :R :item 99} simple-zipper)) => 
      '(:foo :bar :baz 99)

    (zip/root (edit-with {:from :left :put :L :item 99} stubby-zipper)) => 
      '(() () ((99)))
    (zip/root (edit-with {:from :left :put :L :item 99}
      (zip/prev stubby-zipper))) => '(() () (99 ()))
    (zip/root (edit-with {:from :left :put :L :item 99}
      (-> stubby-zipper zip/prev zip/prev))) => '(() 99 () (()))
    (zip/root (edit-with {:from :left :put :R :item 99}
      (rewind stubby-zipper))) => '(() () (()) 99)
    ))


(fact ":left nil tuples"
  (let [at-5
    (-> test-zipper zip/next zip/next zip/next zip/next zip/next zip/next)]

    (zip/root (edit-with {:from :left :put :L :item nil} at-5)) => 
      '(1 2 3 (4 5 (6)))
    (zip/root (edit-with {:from :left :put :R} at-5)) => 
      '(1 2 3 (4 5 (6)))
    (zip/node (edit-with {:from :left :put :R} at-5)) => 4

    (zip/root (edit-with {:from :left :put :L :item nil} empty-zipper)) => 
      '()
    (zip/root (edit-with {:from :left :put :R} empty-zipper)) => 
      '()
    (zip/node (edit-with {:from :left :put :R} empty-zipper)) => '()  

    (zip/root (edit-with {:from :left :put :L :item nil} simple-zipper)) => 
      '(:foo :bar :baz)
    (zip/root (edit-with {:from :left :put :R} simple-zipper)) => 
      '(:foo :bar :baz)
    (zip/node (edit-with {:from :left :put :R} simple-zipper)) => 
      :baz))


;; right moves


(fact "a tuple with :right as its move leaves the cursor in the right place"
  (let [at-5
    (-> test-zipper zip/next zip/next zip/next zip/next zip/next zip/next)]
    (zip/node at-5) => 5
    (zip/node (edit-with {:from :right :put :L :item 99} at-5)) => '(6)
    (zip/node (edit-with {:from :right :put :R :item 99} at-5)) => '(6)))


(fact ":right tuples"
  (let [at-5
    (-> test-zipper zip/next zip/next zip/next zip/next zip/next zip/next)]

    (zip/root (edit-with {:from :right :put :L :item 99} at-5)) => 
      '(1 2 3 (4 5 99 (6)))
    (zip/root (edit-with {:from :right :put :R :item 99} at-5)) => 
      '(1 2 3 (4 5 (6) 99))

    (zip/root (edit-with {:from :right :put :L :item 99} empty-zipper)) => 
      '(99)
    (zip/root (edit-with {:from :right :put :R :item 99} empty-zipper)) => 
      '(99)

    (zip/root (edit-with {:from :right :put :L :item 99} simple-zipper)) => 
      '(:foo 99 :bar :baz)
    (zip/root (edit-with {:from :right :put :R :item 99} simple-zipper)) => 
      '(:foo :bar 99 :baz)
    (zip/root (edit-with {:from :right :put :L :item 99}
      (fast-forward simple-zipper))) =>  '(99 :foo :bar :baz)
    (zip/root (edit-with {:from :right :put :R :item 99}
      (fast-forward simple-zipper))) =>  '(:foo 99 :bar :baz)

    (zip/root (edit-with {:from :right :put :L :item 99} stubby-zipper)) => 
      '(() () ((99)))
    (zip/root (edit-with {:from :right :put :L :item 99}
      (zip/prev stubby-zipper))) => '(() () (99 ()))
    (zip/root (edit-with {:from :right :put :L :item 99}
      (-> stubby-zipper zip/prev zip/prev))) => '(99 () () (()))
    (zip/root (edit-with {:from :right :put :R :item 99}
      (rewind stubby-zipper))) => '(() () 99 (()))
    ))


(fact ":right nil tuples"
  (let [at-5
    (-> test-zipper zip/next zip/next zip/next zip/next zip/next zip/next)]

    (zip/root (edit-with {:from :right :put :L :item nil} at-5)) => 
      '(1 2 3 (4 5 (6)))
    (zip/node (edit-with {:from :right :put :R} at-5)) => '(6)

    (zip/root (edit-with {:from :right :put :L :item nil} empty-zipper)) => 
      '()
    (zip/node (edit-with {:from :right :put :R} empty-zipper)) => '()  

    (zip/root (edit-with {:from :right :put :L :item nil} simple-zipper)) => 
      '(:foo :bar :baz)
    (zip/node (edit-with {:from :right :put :R} simple-zipper)) => 
      :bar))


;; prev moves


(fact "a tuple with :prev as its move leaves the cursor in the right place"
  (let [at-4
    (-> test-zipper zip/next zip/next zip/next zip/next zip/next)]
    (zip/node at-4) => 4
    (zip/node (edit-with {:from :prev :put :L :item 99} at-4)) => '(4 5 (6))
    ))


(fact ":prev tuples"
  (let [at-4
    (-> test-zipper zip/next zip/next zip/next zip/next zip/next)]

    (zip/root (edit-with {:from :prev :put :L :item 99} at-4)) => 
      '(1 2 3 99 (4 5 (6)))
    (zip/root (edit-with {:from :prev :put :R :item 99} at-4)) => 
      '(1 2 3 (4 5 (6)) 99)

    (zip/root (edit-with {:from :prev :put :L :item 99} empty-zipper)) => 
      '(99)
    (zip/root (edit-with {:from :prev :put :R :item 99} empty-zipper)) => 
      '(99)

    (zip/root (edit-with {:from :prev :put :L :item 99} simple-zipper)) => 
      '(:foo :bar 99 :baz)
    (zip/root (edit-with {:from :prev :put :R :item 99} simple-zipper)) => 
      '(:foo :bar :baz 99)
    (zip/root (edit-with {:from :prev :put :L :item 99}
      (fast-forward simple-zipper))) =>  '(:foo 99 :bar :baz)
    (zip/root (edit-with {:from :prev :put :R :item 99}
      (fast-forward simple-zipper))) =>  '(:foo :bar 99 :baz)

    (zip/root (edit-with {:from :prev :put :L :item 99} stubby-zipper)) => 
      '(() () (99 ()))
    (zip/root (edit-with {:from :prev :put :R :item 99} stubby-zipper)) => 
      '(() () (() 99))
    (zip/root (edit-with {:from :prev :put :L :item 99}
      (zip/prev stubby-zipper))) => '(() () 99 (()))
    (zip/root (edit-with {:from :prev :put :L :item 99}
      (-> stubby-zipper zip/prev zip/prev))) => '(() (99) (()))
    (zip/root (edit-with {:from :prev :put :R :item 99}
      (-> stubby-zipper zip/prev zip/prev))) => '(() (99) (()))
    (zip/root (edit-with {:from :prev :put :R :item 99}
      (rewind stubby-zipper))) => '(() () ((99)))
    ))


;; next moves


(fact "a tuple with :next as its move leaves the cursor in the right place"
  (let [at-4
    (-> test-zipper zip/next zip/next zip/next zip/next zip/next)]
    (zip/node at-4) => 4
    (zip/node (edit-with {:from :next :put :L :item 99} at-4)) => 5
    (zip/node (edit-with {:from :next :put :L :item 99}
      (fast-forward test-zipper))) => 1
    ))


(fact ":next tuples"
  (let [at-4
    (-> test-zipper zip/next zip/next zip/next zip/next zip/next)]

    (zip/root (edit-with {:from :next :put :L :item 99} at-4)) => 
      '(1 2 3 (4 99 5 (6)))
    (zip/root (edit-with {:from :next :put :R :item 99} at-4)) => 
      '(1 2 3 (4 5 99 (6)))

    (zip/root (edit-with {:from :next :put :L :item 99} empty-zipper)) => 
      '(99)
    (zip/root (edit-with {:from :next :put :R :item 99} empty-zipper)) => 
      '(99)

    (zip/root (edit-with {:from :next :put :L :item 99} simple-zipper)) => 
      '(:foo 99 :bar :baz)
    (zip/root (edit-with {:from :next :put :R :item 99} simple-zipper)) => 
      '(:foo :bar 99 :baz)
    (zip/root (edit-with {:from :next :put :L :item 99}
      (fast-forward simple-zipper))) =>  '(99 :foo :bar :baz)
    (zip/root (edit-with {:from :next :put :R :item 99}
      (fast-forward simple-zipper))) =>  '(:foo 99 :bar :baz)

    (zip/root (edit-with {:from :next :put :L :item 99} stubby-zipper)) => 
      '(99 () () (()))
    (zip/root (edit-with {:from :next :put :R :item 99} stubby-zipper)) => 
      '(() 99 () (()))
    (zip/root (edit-with {:from :next :put :L :item 99}
      (zip/prev stubby-zipper))) => '(() () ((99)))
    (zip/root (edit-with {:from :next :put :L :item 99}
      (-> stubby-zipper zip/prev zip/prev))) => '(() () (99 ()))
    (zip/root (edit-with {:from :next :put :R :item 99}
      (-> stubby-zipper zip/prev zip/prev))) => '(() () (() 99))
    (zip/root (edit-with {:from :next :put :R :item 99}
      (rewind stubby-zipper))) => '((99) () (()))
    (zip/root (edit-with {:from :next :put :R :item 99}
      (zip/next (rewind stubby-zipper)))) => '(() () 99 (()))
    ))


;; translating genomes



(fact "an empty genome produces an empty program"
  (zip->push [])=> [])

