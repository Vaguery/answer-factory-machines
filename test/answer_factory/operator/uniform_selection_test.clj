(ns answer-factory.operator.uniform-selection-test
  (:use midje.sweet)
  (:use answer-factory.operator.select))

;
;; uniform-selection

(fact "uniform selection returns a vector of answers"
  (uniform-selection []) => []
  (uniform-selection [:foo :bar :baz]) => [99]
    (provided (rand-nth [:foo :bar :baz])=> 99)
  )

(fact "uniform-selection throws an exception if `answers` isn't a vector"
  (uniform-selection 99) => (throws #"expects a vector of answers")
  (uniform-selection '(1 2 3)) => (throws #"expects a vector of answers")
  )

(fact "uniform-selection can accept (but ignores) a :scores collection"
  (uniform-selection [] :scores [1 2 3]) => []
  )


;; uniform-cull


(fact "uniform cull returns a vector of answers"
  (uniform-cull []) => []
  (uniform-cull [:foo :bar :baz]) => [:foo :baz]
    (provided (rand-int 3) => 1)
  (uniform-cull [:foo :bar :baz]) => [:bar :baz]
    (provided (rand-int 3) => 0)
    )

(fact "uniform-cull throws an exception if `answers` isn't a vector"
  (uniform-cull 99) => (throws #"expects a vector of answers")
  (uniform-cull '(1 2 3)) => (throws #"expects a vector of answers")
  )

(fact "uniform-cull can accept (but ignores) a :scores collection"
  (uniform-cull [] :scores [1 2 3]) => []
  )
