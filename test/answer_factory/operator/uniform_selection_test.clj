(ns answer-factory.operator.uniform-selection-test
  (:use midje.sweet)
  (:require [answer-factory.util.selection-fixtures :as fixtures])
  (:use answer-factory.operator.select))


(fact "uniform selection returns a vector of answers"
  (type (uniform-selection fixtures/some-guys fixtures/random-scores)) =>
    clojure.lang.PersistentVector
  (type (first (uniform-selection fixtures/some-guys fixtures/random-scores))) =>
    answer_factory.answer.push.PushAnswer)


(fact "uniform selection returns one item sampled at random"
  (count (uniform-selection fixtures/some-guys fixtures/random-scores)) => 1
  fixtures/some-guys => (contains (uniform-selection fixtures/some-guys fixtures/random-scores)))

