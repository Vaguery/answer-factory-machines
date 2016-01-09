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


(fact "count-cursorpoints returns the number of steps in a zipper"
  (count-cursorpoints empty-zipper) => 1
  (count-cursorpoints test-zipper) => 8
  (count-cursorpoints simple-zipper) => 3
  (count-cursorpoints stubby-zipper) => 7)



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
  (zip/node (apply-gene {:from :head :put :L :item 99} test-zipper)) => 1
  (zip/node (apply-gene {:from :head :put :L :item 99} simple-zipper)) => :foo)



(fact ":head tuples"
  (zip/root (apply-gene {:from :head :put :L :item 99} test-zipper)) => 
    '(99 1 2 3 (4 5 (6)))
  (zip/root (apply-gene {:from :head :put :R :item 99} test-zipper)) => 
    '(1 99 2 3 (4 5 (6)))

  (zip/root (apply-gene {:from :head :put :L :item 99} empty-zipper)) => 
    '(99)
  (zip/root (apply-gene {:from :head :put :R :item 99} empty-zipper)) => 
    '(99)

  (zip/root (apply-gene {:from :head :put :L :item 99} simple-zipper)) => 
    '(99 :foo :bar :baz)
  (zip/root (apply-gene {:from :head :put :R :item 99} simple-zipper)) => 
    '(:foo 99 :bar :baz)

  (zip/root (apply-gene {:from :head :put :L :item 99} stubby-zipper)) => 
    '(99 () () (())))



(fact ":head nil tuples"
  (zip/root (apply-gene {:from :head :put :L :item nil} test-zipper)) => 
    '(1 2 3 (4 5 (6)))
  (zip/root (apply-gene {:from :head :put :R} test-zipper)) => 
    '(1 2 3 (4 5 (6)))

  (zip/root (apply-gene {:from :head :put :L :item nil} empty-zipper)) => 
    '()
  (zip/root (apply-gene {:from :head :put :R} empty-zipper)) => 
    '()

  (zip/root (apply-gene {:from :head :put :L :item nil} simple-zipper)) => 
    '(:foo :bar :baz)
  (zip/root (apply-gene {:from :head :put :R} simple-zipper)) => 
    '(:foo :bar :baz))



;; tail moves


(fact "a tuple with :tail as its move leaves the cursor at the tail of the zipper"
  (zip/node (apply-gene {:from :tail :put :L :item 99} test-zipper)) => 6
  (zip/node (apply-gene {:from :tail :put :L :item 99} simple-zipper)) => :baz)



(fact ":tail tuples"
  (zip/root (apply-gene {:from :tail :put :L :item 99} test-zipper)) => 
    '(1 2 3 (4 5 (99 6)))
  (zip/root (apply-gene {:from :tail :put :R :item 99} test-zipper)) => 
    '(1 2 3 (4 5 (6 99)))

  (zip/root (apply-gene {:from :tail :put :L :item 99} empty-zipper)) => 
    '(99)
  (zip/root (apply-gene {:from :tail :put :R :item 99} empty-zipper)) => 
    '(99)

  (zip/root (apply-gene {:from :tail :put :L :item 99} simple-zipper)) => 
    '(:foo :bar 99 :baz)
  (zip/root (apply-gene {:from :tail :put :R :item 99} simple-zipper)) => 
    '(:foo :bar :baz 99)

  (zip/root (apply-gene {:from :tail :put :L :item 99} stubby-zipper)) => 
    '(() () ((99)))
  (zip/root (apply-gene {:from :tail :put :L :item 99}
    (-> stubby-zipper zip/prev zip/prev))) => '(() () ((99)))
  )


(fact ":tail nil tuples"
  (zip/root (apply-gene {:from :tail :put :L :item nil} test-zipper)) => 
    '(1 2 3 (4 5 (6)))
  (zip/root (apply-gene {:from :tail :put :R} test-zipper)) => 
    '(1 2 3 (4 5 (6)))
  (zip/node (apply-gene {:from :tail :put :R} test-zipper)) => 6

  (zip/root (apply-gene {:from :tail :put :L :item nil} empty-zipper)) => 
    '()
  (zip/root (apply-gene {:from :tail :put :R} empty-zipper)) => 
    '()
  (zip/node (apply-gene {:from :tail :put :R} empty-zipper)) => '()  

  (zip/root (apply-gene {:from :tail :put :L :item nil} simple-zipper)) => 
    '(:foo :bar :baz)
  (zip/root (apply-gene {:from :tail :put :R} simple-zipper)) => 
    '(:foo :bar :baz)
  (zip/node (apply-gene {:from :tail :put :R} simple-zipper)) => 
    :baz  )


;; subhead moves


(fact "a tuple with :subhead as its move leaves the cursor at the leftmost in its level"
  (let [at-5
    (-> test-zipper zip/next zip/next zip/next zip/next zip/next zip/next)]
    (zip/node at-5) => 5
    (zip/node (apply-gene {:from :subhead :put :L :item 99} at-5)) => 4
    (zip/node (apply-gene {:from :subhead :put :R :item 99} at-5)) => 4))


(fact ":subhead tuples"
  (let [at-5
    (-> test-zipper zip/next zip/next zip/next zip/next zip/next zip/next)]

    (zip/root (apply-gene {:from :subhead :put :L :item 99} at-5)) => 
      '(1 2 3 (99 4 5 (6)))
    (zip/root (apply-gene {:from :subhead :put :R :item 99} at-5)) => 
      '(1 2 3 (4 99 5 (6)))

    (zip/root (apply-gene {:from :subhead :put :L :item 99} empty-zipper)) => 
      '(99)
    (zip/root (apply-gene {:from :subhead :put :R :item 99} empty-zipper)) => 
      '(99)

    (zip/root (apply-gene {:from :subhead :put :L :item 99} simple-zipper)) => 
      '(99 :foo :bar :baz)
    (zip/root (apply-gene {:from :subhead :put :R :item 99} simple-zipper)) => 
      '(:foo 99 :bar :baz)

    (zip/root (apply-gene {:from :subhead :put :L :item 99} stubby-zipper)) => 
      '(() () ((99)))
    (zip/root (apply-gene {:from :subhead :put :L :item 99}
      (zip/prev stubby-zipper))) => '(() () (99 ()))
    (zip/root (apply-gene {:from :subhead :put :L :item 99}
      (-> stubby-zipper zip/prev zip/prev))) => '(99 () () (()))
      ))


(fact ":subhead nil tuples"
  (let [at-5
    (-> test-zipper zip/next zip/next zip/next zip/next zip/next zip/next)]

    (zip/root (apply-gene {:from :subhead :put :L :item nil} at-5)) => 
      '(1 2 3 (4 5 (6)))
    (zip/root (apply-gene {:from :subhead :put :R} at-5)) => 
      '(1 2 3 (4 5 (6)))
    (zip/node (apply-gene {:from :subhead :put :R} at-5)) => 4

    (zip/root (apply-gene {:from :subhead :put :L :item nil} empty-zipper)) => 
      '()
    (zip/root (apply-gene {:from :subhead :put :R} empty-zipper)) => 
      '()
    (zip/node (apply-gene {:from :subhead :put :R} empty-zipper)) => '()  

    (zip/root (apply-gene {:from :subhead :put :L :item nil} simple-zipper)) => 
      '(:foo :bar :baz)
    (zip/root (apply-gene {:from :subhead :put :R} simple-zipper)) => 
      '(:foo :bar :baz)
    (zip/node (apply-gene {:from :subhead :put :R} simple-zipper)) => 
      :foo  ))


;; left moves


(fact "a tuple with :left as its move leaves the cursor in the right place"
  (let [at-5
    (-> test-zipper zip/next zip/next zip/next zip/next zip/next zip/next)]
    (zip/node at-5) => 5
    (zip/node (apply-gene {:from :left :put :L :item 99} at-5)) => 4
    (zip/node (apply-gene {:from :left :put :R :item 99} at-5)) => 4))


(fact ":left tuples"
  (let [at-5
    (-> test-zipper zip/next zip/next zip/next zip/next zip/next zip/next)]

    (zip/root (apply-gene {:from :left :put :L :item 99} at-5)) => 
      '(1 2 3 (99 4 5 (6)))
    (zip/root (apply-gene {:from :left :put :R :item 99} at-5)) => 
      '(1 2 3 (4 99 5 (6)))

    (zip/root (apply-gene {:from :left :put :L :item 99} empty-zipper)) => 
      '(99)
    (zip/root (apply-gene {:from :left :put :R :item 99} empty-zipper)) => 
      '(99)

    (zip/root (apply-gene {:from :left :put :L :item 99} simple-zipper)) => 
      '(:foo :bar 99 :baz)
    (zip/root (apply-gene {:from :left :put :R :item 99} simple-zipper)) => 
      '(:foo :bar :baz 99)

    (zip/root (apply-gene {:from :left :put :L :item 99} stubby-zipper)) => 
      '(() () ((99)))
    (zip/root (apply-gene {:from :left :put :L :item 99}
      (zip/prev stubby-zipper))) => '(() () (99 ()))
    (zip/root (apply-gene {:from :left :put :L :item 99}
      (-> stubby-zipper zip/prev zip/prev))) => '(() 99 () (()))
    (zip/root (apply-gene {:from :left :put :R :item 99}
      (rewind stubby-zipper))) => '(() () (()) 99)
    ))


(fact ":left nil tuples"
  (let [at-5
    (-> test-zipper zip/next zip/next zip/next zip/next zip/next zip/next)]

    (zip/root (apply-gene {:from :left :put :L :item nil} at-5)) => 
      '(1 2 3 (4 5 (6)))
    (zip/root (apply-gene {:from :left :put :R} at-5)) => 
      '(1 2 3 (4 5 (6)))
    (zip/node (apply-gene {:from :left :put :R} at-5)) => 4

    (zip/root (apply-gene {:from :left :put :L :item nil} empty-zipper)) => 
      '()
    (zip/root (apply-gene {:from :left :put :R} empty-zipper)) => 
      '()
    (zip/node (apply-gene {:from :left :put :R} empty-zipper)) => '()  

    (zip/root (apply-gene {:from :left :put :L :item nil} simple-zipper)) => 
      '(:foo :bar :baz)
    (zip/root (apply-gene {:from :left :put :R} simple-zipper)) => 
      '(:foo :bar :baz)
    (zip/node (apply-gene {:from :left :put :R} simple-zipper)) => 
      :baz))


;; right moves


(fact "a tuple with :right as its move leaves the cursor in the right place"
  (let [at-5
    (-> test-zipper zip/next zip/next zip/next zip/next zip/next zip/next)]
    (zip/node at-5) => 5
    (zip/node (apply-gene {:from :right :put :L :item 99} at-5)) => '(6)
    (zip/node (apply-gene {:from :right :put :R :item 99} at-5)) => '(6)))


(fact ":right tuples"
  (let [at-5
    (-> test-zipper zip/next zip/next zip/next zip/next zip/next zip/next)]

    (zip/root (apply-gene {:from :right :put :L :item 99} at-5)) => 
      '(1 2 3 (4 5 99 (6)))
    (zip/root (apply-gene {:from :right :put :R :item 99} at-5)) => 
      '(1 2 3 (4 5 (6) 99))

    (zip/root (apply-gene {:from :right :put :L :item 99} empty-zipper)) => 
      '(99)
    (zip/root (apply-gene {:from :right :put :R :item 99} empty-zipper)) => 
      '(99)

    (zip/root (apply-gene {:from :right :put :L :item 99} simple-zipper)) => 
      '(:foo 99 :bar :baz)
    (zip/root (apply-gene {:from :right :put :R :item 99} simple-zipper)) => 
      '(:foo :bar 99 :baz)
    (zip/root (apply-gene {:from :right :put :L :item 99}
      (fast-forward simple-zipper))) =>  '(99 :foo :bar :baz)
    (zip/root (apply-gene {:from :right :put :R :item 99}
      (fast-forward simple-zipper))) =>  '(:foo 99 :bar :baz)

    (zip/root (apply-gene {:from :right :put :L :item 99} stubby-zipper)) => 
      '(() () ((99)))
    (zip/root (apply-gene {:from :right :put :L :item 99}
      (zip/prev stubby-zipper))) => '(() () (99 ()))
    (zip/root (apply-gene {:from :right :put :L :item 99}
      (-> stubby-zipper zip/prev zip/prev))) => '(99 () () (()))
    (zip/root (apply-gene {:from :right :put :R :item 99}
      (rewind stubby-zipper))) => '(() () 99 (()))
    ))


(fact ":right nil tuples"
  (let [at-5
    (-> test-zipper zip/next zip/next zip/next zip/next zip/next zip/next)]

    (zip/root (apply-gene {:from :right :put :L :item nil} at-5)) => 
      '(1 2 3 (4 5 (6)))
    (zip/node (apply-gene {:from :right :put :R} at-5)) => '(6)

    (zip/root (apply-gene {:from :right :put :L :item nil} empty-zipper)) => 
      '()
    (zip/node (apply-gene {:from :right :put :R} empty-zipper)) => '()  

    (zip/root (apply-gene {:from :right :put :L :item nil} simple-zipper)) => 
      '(:foo :bar :baz)
    (zip/node (apply-gene {:from :right :put :R} simple-zipper)) => 
      :bar))


;; append moves


(fact "a tuple with :append as its move shifts the cursor to the append position of the subtree in which it was originally located"
  (let [at-4
    (-> test-zipper zip/next zip/next zip/next zip/next zip/next)]
    (zip/node at-4) => 4

    (zip/node (apply-gene {:from :append :put :L :item 99} at-4)) => '(6)
    (zip/node (apply-gene {:from :append :put :R :item 99}
      (-> at-4 zip/prev zip/prev))) => '(4 5 (6))))


(fact ":append tuples"
  (let [at-4
    (-> test-zipper zip/next zip/next zip/next zip/next zip/next)]

    (zip/root (apply-gene {:from :append :put :L :item 99} at-4)) => 
      '(1 2 3 (4 5 99 (6)))
    (zip/root (apply-gene {:from :append :put :R :item 99} at-4)) => 
      '(1 2 3 (4 5 (6) 99))

    (zip/root (apply-gene {:from :append :put :L :item 99} empty-zipper)) => 
      '(99)
    (zip/root (apply-gene {:from :append :put :R :item 99} empty-zipper)) => 
      '(99)

    (zip/root (apply-gene {:from :append :put :L :item 99} simple-zipper)) => 
      '(:foo :bar 99 :baz)
    (zip/root (apply-gene {:from :append :put :R :item 99} simple-zipper)) => 
      '(:foo :bar :baz 99)
    (zip/root (apply-gene {:from :append :put :L :item 99}
      (fast-forward simple-zipper))) =>  '(:foo :bar 99 :baz)
    (zip/root (apply-gene {:from :append :put :R :item 99}
      (fast-forward simple-zipper))) =>  '(:foo :bar :baz 99)

    (zip/root (apply-gene {:from :append :put :L :item 99} stubby-zipper)) => 
      '(() () ((99)))
    (zip/root (apply-gene {:from :append :put :R :item 99} stubby-zipper)) => 
      '(() () ((99)))
    (zip/root (apply-gene {:from :append :put :L :item 99}
      (zip/prev stubby-zipper))) => '(() () (99 ()))
    (zip/root (apply-gene {:from :append :put :L :item 99}
      (-> stubby-zipper zip/prev zip/prev))) => '(() () 99 (()))
    (zip/root (apply-gene {:from :append :put :R :item 99}
      (-> stubby-zipper zip/prev zip/prev))) => '(() () (()) 99)
    (zip/root (apply-gene {:from :append :put :R :item 99}
      (rewind stubby-zipper))) => '(() () (()) 99)
    ))



;; prev moves


(fact "a tuple with :prev as its move leaves the cursor in the right place"
  (let [at-4
    (-> test-zipper zip/next zip/next zip/next zip/next zip/next)]
    (zip/node at-4) => 4
    (zip/node (apply-gene {:from :prev :put :L :item 99} at-4)) => '(4 5 (6))
    ))


(fact ":prev tuples"
  (let [at-4
    (-> test-zipper zip/next zip/next zip/next zip/next zip/next)]

    (zip/root (apply-gene {:from :prev :put :L :item 99} at-4)) => 
      '(1 2 3 99 (4 5 (6)))
    (zip/root (apply-gene {:from :prev :put :R :item 99} at-4)) => 
      '(1 2 3 (4 5 (6)) 99)

    (zip/root (apply-gene {:from :prev :put :L :item 99} empty-zipper)) => 
      '(99)
    (zip/root (apply-gene {:from :prev :put :R :item 99} empty-zipper)) => 
      '(99)

    (zip/root (apply-gene {:from :prev :put :L :item 99} simple-zipper)) => 
      '(:foo :bar 99 :baz)
    (zip/root (apply-gene {:from :prev :put :R :item 99} simple-zipper)) => 
      '(:foo :bar :baz 99)
    (zip/root (apply-gene {:from :prev :put :L :item 99}
      (fast-forward simple-zipper))) =>  '(:foo 99 :bar :baz)
    (zip/root (apply-gene {:from :prev :put :R :item 99}
      (fast-forward simple-zipper))) =>  '(:foo :bar 99 :baz)

    (zip/root (apply-gene {:from :prev :put :L :item 99} stubby-zipper)) => 
      '(() () (99 ()))
    (zip/root (apply-gene {:from :prev :put :R :item 99} stubby-zipper)) => 
      '(() () (() 99))
    (zip/root (apply-gene {:from :prev :put :L :item 99}
      (zip/prev stubby-zipper))) => '(() () 99 (()))
    (zip/root (apply-gene {:from :prev :put :L :item 99}
      (-> stubby-zipper zip/prev zip/prev))) => '(() (99) (()))
    (zip/root (apply-gene {:from :prev :put :R :item 99}
      (-> stubby-zipper zip/prev zip/prev))) => '(() (99) (()))
    (zip/root (apply-gene {:from :prev :put :R :item 99}
      (rewind stubby-zipper))) => '(() () ((99)))
    ))


;; next moves


(fact "a tuple with :next as its move leaves the cursor in the right place"
  (let [at-4
    (-> test-zipper zip/next zip/next zip/next zip/next zip/next)]
    (zip/node at-4) => 4
    (zip/node (apply-gene {:from :next :put :L :item 99} at-4)) => 5
    (zip/node (apply-gene {:from :next :put :L :item 99}
      (fast-forward test-zipper))) => 1
    ))


(fact ":next tuples"
  (let [at-4
    (-> test-zipper zip/next zip/next zip/next zip/next zip/next)]

    (zip/root (apply-gene {:from :next :put :L :item 99} at-4)) => 
      '(1 2 3 (4 99 5 (6)))
    (zip/root (apply-gene {:from :next :put :R :item 99} at-4)) => 
      '(1 2 3 (4 5 99 (6)))

    (zip/root (apply-gene {:from :next :put :L :item 99} empty-zipper)) => 
      '(99)
    (zip/root (apply-gene {:from :next :put :R :item 99} empty-zipper)) => 
      '(99)

    (zip/root (apply-gene {:from :next :put :L :item 99} simple-zipper)) => 
      '(:foo 99 :bar :baz)
    (zip/root (apply-gene {:from :next :put :R :item 99} simple-zipper)) => 
      '(:foo :bar 99 :baz)
    (zip/root (apply-gene {:from :next :put :L :item 99}
      (fast-forward simple-zipper))) =>  '(99 :foo :bar :baz)
    (zip/root (apply-gene {:from :next :put :R :item 99}
      (fast-forward simple-zipper))) =>  '(:foo 99 :bar :baz)

    (zip/root (apply-gene {:from :next :put :L :item 99} stubby-zipper)) => 
      '(99 () () (()))
    (zip/root (apply-gene {:from :next :put :R :item 99} stubby-zipper)) => 
      '(() 99 () (()))
    (zip/root (apply-gene {:from :next :put :L :item 99}
      (zip/prev stubby-zipper))) => '(() () ((99)))
    (zip/root (apply-gene {:from :next :put :L :item 99}
      (-> stubby-zipper zip/prev zip/prev))) => '(() () (99 ()))
    (zip/root (apply-gene {:from :next :put :R :item 99}
      (-> stubby-zipper zip/prev zip/prev))) => '(() () (() 99))
    (zip/root (apply-gene {:from :next :put :R :item 99}
      (rewind stubby-zipper))) => '((99) () (()))
    (zip/root (apply-gene {:from :next :put :R :item 99}
      (zip/next (rewind stubby-zipper)))) => '(() () 99 (()))
    ))


;; up moves


(fact "a tuple with :up as its move leaves the cursor in the right place"
  (let [at-4
    (-> test-zipper zip/next zip/next zip/next zip/next zip/next)]
    (zip/node at-4) => 4
    (zip/node (apply-gene {:from :up :put :L :item 99} at-4)) => '(4 5 (6))
    (zip/node (apply-gene {:from :up :put :L :item 99}
      (fast-forward test-zipper))) => '(6)
    ))


(fact ":up tuples"
  (let [at-4
    (-> test-zipper zip/next zip/next zip/next zip/next zip/next)]

    (zip/root (apply-gene {:from :up :put :L :item 99} at-4)) => 
      '(1 2 3 99 (4 5 (6)))
    (zip/root (apply-gene {:from :up :put :R :item 99} at-4)) => 
      '(1 2 3 (4 5 (6)) 99)

    (zip/root (apply-gene {:from :up :put :L :item 99} empty-zipper)) => 
      '(99)
    (zip/root (apply-gene {:from :up :put :R :item 99} empty-zipper)) => 
      '(99)

    (zip/root (apply-gene {:from :up :put :L :item 99} simple-zipper)) => 
      '(99 :foo :bar :baz)
    (zip/root (apply-gene {:from :up :put :R :item 99} simple-zipper)) => 
      '(:foo 99 :bar :baz)
    (zip/root (apply-gene {:from :up :put :L :item 99}
      (fast-forward simple-zipper))) =>  '(99 :foo :bar :baz)
    (zip/root (apply-gene {:from :up :put :R :item 99}
      (fast-forward simple-zipper))) =>  '(:foo 99 :bar :baz)

    (zip/root (apply-gene {:from :up :put :L :item 99} stubby-zipper)) => 
      '(() () (99 ()))
    (zip/root (apply-gene {:from :up :put :R :item 99} stubby-zipper)) => 
      '(() () (() 99))
    (zip/root (apply-gene {:from :up :put :L :item 99}
      (zip/prev stubby-zipper))) => '(() () 99 (()))
    (zip/root (apply-gene {:from :up :put :L :item 99}
      (-> stubby-zipper zip/prev zip/prev))) => '(99 () () (()))
    (zip/root (apply-gene {:from :up :put :R :item 99}
      (-> stubby-zipper zip/prev zip/prev))) => '(() 99 () (()))
    (zip/root (apply-gene {:from :up :put :R :item 99}
      (rewind stubby-zipper))) => '(() 99 () (()))
    (zip/root (apply-gene {:from :up :put :R :item 99}
      (zip/next (rewind stubby-zipper)))) => '(() 99 () (()))
    ))


;; down moves


(fact "a tuple with :down as its move leaves the cursor in the right place"
  (let [at-4
    (-> test-zipper zip/next zip/next zip/next zip/next zip/next)]
    (zip/node at-4) => 4
    (zip/node (apply-gene {:from :down :put :L :item 99} at-4)) => 4
    (zip/node (apply-gene {:from :down :put :L :item 99}
      (zip/prev at-4))) => 4
    ))


(fact ":down tuples"
  (let [at-4
    (-> test-zipper zip/next zip/next zip/next zip/next zip/next)]

    (zip/root (apply-gene {:from :down :put :L :item 99} at-4)) => 
      '(1 2 3 (99 4 5 (6)))
    (zip/root (apply-gene {:from :down :put :R :item 99} at-4)) => 
      '(1 2 3 (4 99 5 (6)))

    (zip/root (apply-gene {:from :down :put :L :item 99} empty-zipper)) => 
      '(99)
    (zip/root (apply-gene {:from :down :put :R :item 99} empty-zipper)) => 
      '(99)

    (zip/root (apply-gene {:from :down :put :L :item 99} simple-zipper)) => 
      '(99 :foo :bar :baz)
    (zip/root (apply-gene {:from :down :put :R :item 99} simple-zipper)) => 
      '(:foo 99 :bar :baz)
    (zip/root (apply-gene {:from :down :put :L :item 99}
      (fast-forward simple-zipper))) =>  '(:foo :bar 99 :baz)
    (zip/root (apply-gene {:from :down :put :R :item 99}
      (fast-forward simple-zipper))) =>  '(:foo :bar :baz 99)

    (zip/root (apply-gene {:from :down :put :L :item 99} stubby-zipper)) => 
      '(() () ((99)))
    (zip/root (apply-gene {:from :down :put :R :item 99} stubby-zipper)) => 
      '(() () ((99)))
    (zip/root (apply-gene {:from :down :put :L :item 99}
      (zip/prev stubby-zipper))) => '(() () ((99)))
    (zip/root (apply-gene {:from :down :put :L :item 99}
      (-> stubby-zipper zip/prev zip/prev))) => '(() () (99 ()))
    (zip/root (apply-gene {:from :down :put :R :item 99}
      (-> stubby-zipper zip/prev zip/prev))) => '(() () (() 99))
    (zip/root (apply-gene {:from :down :put :R :item 99}
      (rewind stubby-zipper))) => '((99) () (()))
    (zip/root (apply-gene {:from :down :put :R :item 99}
      (zip/next (fast-forward stubby-zipper)))) => '(() 99 () (()))
    ))


;; goto moves


(fact "scroll-to-index works for numbers in range, then bottoms out"
  (zip/node (scroll-to-index test-zipper 0)) => 1
  (zip/node (scroll-to-index test-zipper 1)) => 2
  (zip/node (scroll-to-index test-zipper 2)) => 3
  (zip/node (scroll-to-index test-zipper 3)) => '(4 5 (6))
  (zip/node (scroll-to-index test-zipper 4)) => 4
  (zip/node (scroll-to-index test-zipper 5)) => 5
  (zip/node (scroll-to-index test-zipper 6)) => '(6)
  (zip/node (scroll-to-index test-zipper 7)) => 6
  (zip/node (scroll-to-index test-zipper 8)) => (zip/node test-zipper)
  (zip/node (scroll-to-index test-zipper 9)) => (zip/node test-zipper)
  (zip/node (scroll-to-index test-zipper -3)) => (throws)
  )


(fact "jump-to works for any number, modulo count-cursorpoints"
  (zip/node (jump-to test-zipper 0)) => 1
  (zip/node (jump-to test-zipper 1)) => 2
  (zip/node (jump-to test-zipper 11)) => '(4 5 (6))
  (zip/node (jump-to test-zipper -1)) => 6
  (zip/node (jump-to test-zipper -2)) => '(6)
  (zip/node (jump-to test-zipper -5)) => '(4 5 (6))
  (zip/node (jump-to test-zipper 100212)) => 4

  (zip/node (jump-to empty-zipper 100212)) => nil

  (zip/node (jump-to simple-zipper 8812)) => :bar
  (zip/node (jump-to simple-zipper -8812)) => :baz

  (zip/node (jump-to stubby-zipper 0)) => '()
  (zip/node (jump-to stubby-zipper 1)) => nil
  (zip/node (jump-to stubby-zipper 11)) => '(())
  (zip/node (jump-to stubby-zipper -2)) => '()
  )


(fact "a tuple with an integer as its move leaves the cursor in the right place"
  (zip/node (apply-gene {:from 3 :put :L :item 99} test-zipper)) => '(4 5 (6)))


(fact "jump-to tuples"
  (zip/root (apply-gene {:from 0 :put :L :item 99} test-zipper)) => 
    '(99 1 2 3 (4 5 (6)))
  (zip/root (apply-gene {:from -1 :put :R :item 99} test-zipper)) => 
    '(1 2 3 (4 5 (6 99)))
  (zip/root (apply-gene {:from -2 :put :R :item 99} test-zipper)) => 
    '(1 2 3 (4 5 (6) 99))

  (zip/root (apply-gene {:from 33 :put :L :item 99} empty-zipper)) => 
    '(99)
  (zip/root (apply-gene {:from 33 :put :R :item 99} empty-zipper)) => 
    '(99)

  (zip/root (apply-gene {:from 31 :put :L :item 99} simple-zipper)) => 
    '(:foo 99 :bar :baz)
  (zip/root (apply-gene {:from 31 :put :R :item 99} simple-zipper)) => 
    '(:foo :bar 99 :baz)
  (zip/root (apply-gene {:from -1 :put :L :item 99}
    (fast-forward simple-zipper))) =>  '(:foo :bar 99 :baz)
  (zip/root (apply-gene {:from -1 :put :R :item 99}
    (fast-forward simple-zipper))) =>  '(:foo :bar :baz 99)

  (zip/root (apply-gene {:from 1 :put :L :item 99} stubby-zipper)) => 
    '((99) () (()))
  (zip/root (apply-gene {:from 2 :put :R :item 99} stubby-zipper)) => 
    '(() () 99 (()))
  (zip/root (apply-gene {:from -2 :put :L :item 99}
    (zip/prev stubby-zipper))) => '(() () (99 ()))
  (zip/root (apply-gene {:from -3 :put :L :item 99}
    (-> stubby-zipper zip/prev zip/prev))) => '(() () 99 (()))
  )



;; translating genomes



(fact "an empty genome produces an empty program"
  (zip->push [])=> [])

