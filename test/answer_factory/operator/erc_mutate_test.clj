(ns answer-factory.operator.erc-mutate-test
  (:use midje.sweet)
  (:use answer-factory.answer.push)
  (:use answer-factory.operator.mutate))


(fact "mutate-plush-item changes the :item field"
  (mutate-plush-item {:item 22 :close 0} 1.0 [99]) => {:item 99 :close 0}
  (mutate-plush-item {:item 22 :close 0} 1.0 [false]) => {:item false :close 0}
  (mutate-plush-item {:item 22 :close 0} 0.0 [false]) => {:item 22 :close 0})


(fact "mutate-plush-item does nothing if the list is empty"
  (mutate-plush-item {:item 22 :close 0} 1.0 []) => {:item 22 :close 0})


(fact "mutate-plush-item actually samples the list"
  (mutate-plush-item {:item 22 :close 0} 1.0 [1 2 3]) => {:item 8888 :close 0}
    (provided (rand-nth [1 2 3]) => 8888))


;; binomial-sample

(fact "binomial-sample has a default cutoff"
  (binomial-sample 0) => 0
  (binomial-sample 1) => 21
  (binomial-sample 0.8) => integer?
  (binomial-sample 0.8) => #(< % 22))


(fact "binomial-sample has an optionally settable cutoff"
  (binomial-sample 0 :limit 999) => 0
  (binomial-sample 1 :limit 999) => 1000
  (binomial-sample 0.8 :limit 999) => integer?
  (< (binomial-sample 0.999 :limit 666) 668) => true)


;; mutate-plush-close

(fact "mutate-plush-close replaces the :close field"
  (mutate-plush-close {:item 22 :close 0} 1.0 1.0 10) => {:item 22 :close 11}
  (mutate-plush-close {:item 22 :close 1} 1.0 0.0 55) => {:item 22 :close 0})


;; mutate-plush-silence

(fact "mutate-plush-silence sets or resets the :silent field"
  (mutate-plush-silence {:item 22 :close 0} 1.0) => {:item 22 :close 0 :silent true}
    (provided (rand-nth [true false]) => true)
  (mutate-plush-silence {:item 22 :close 1} 0.0) => {:item 22 :close 1})
