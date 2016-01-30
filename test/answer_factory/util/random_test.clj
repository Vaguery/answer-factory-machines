(ns answer-factory.util.random-test
  (:use midje.sweet)
  (:use answer-factory.util.random))


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


;; discrete-sample


(fact "discrete-sample returns an item from the collection with a scaled probability"
  (discrete-sample [1 2 3] [8 4 1]) => 1
    (provided (rand) => 0)
  (discrete-sample [1 2 3] [8 4 1]) => 2
    (provided (rand) => 10/13)
  (discrete-sample [1 2 3] [8 4 1]) => 3
    (provided (rand) => 0.99))


(fact "discrete-sample throws an exception if it has the wrong count of weights"
  (discrete-sample [1 2 3] [3]) => (throws #"item & weight")
  (discrete-sample [1 2 3] [3 4 5 6 7]) => (throws #"item & weight"))


(fact "discrete-sample throws an exception if it has any negative weights"
  (discrete-sample [1 2 3] [2 3 -3]) => (throws #"positive numbers")
  (discrete-sample [1 2 3] [2 3 0]) =not=> (throws #"positive numbers")
  )

