(ns answer-factory.operator.uniform-selection-test
  (:use midje.sweet)
  (:use answer-factory.operator.select))

;
; ;; uniform-selection
;
; (fact "uniform selection returns a vector of answers"
;   (type (uniform-selection fixtures/some-guys fixtures/random-scores)) =>
;     clojure.lang.PersistentVector
;   (type (first (uniform-selection fixtures/some-guys fixtures/random-scores))) =>
;     answer_factory.answer.push.PushAnswer)
;
;
; (fact "uniform selection returns one item sampled at random"
;   (count (uniform-selection fixtures/some-guys fixtures/random-scores)) => 1
;   fixtures/some-guys => (contains (uniform-selection fixtures/some-guys fixtures/random-scores)))
;
;
; (fact "uniform-selection throws an exception if the collection is empty"
;   (uniform-selection [] fixtures/random-scores) => (throws #"empty collection"))
;
;
; ;; uniform-cull
;
;
; (fact "uniform cull returns a vector of answers"
;   (type (uniform-cull fixtures/some-guys fixtures/random-scores)) =>
;     clojure.lang.PersistentVector
;   (type (first (uniform-selection fixtures/some-guys fixtures/random-scores))) =>
;     answer_factory.answer.push.PushAnswer)
;
;
; (fact "uniform cull removes a random one"
;   (count (uniform-cull fixtures/some-guys fixtures/random-scores)) => 4
;   fixtures/some-guys =>
;     (contains (uniform-cull fixtures/some-guys fixtures/random-scores) :gaps-ok))
;
;
; (fact "uniform-cull throws an exception if the collection is empty"
;   (uniform-cull [] fixtures/random-scores) => (throws #"empty collection"))
