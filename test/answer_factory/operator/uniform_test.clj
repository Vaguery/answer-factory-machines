(ns answer-factory.operator.uniform-test
  (:use midje.sweet)
  (:require [answer-factory.util.selection-fixtures :as fixtures])
  (:use answer-factory.operator.select))





;; OK now we can test

(fact "uniform selection returns one item sampled at random"
  (uniform-selection fixtures/some-guys fixtures/some-scores) => :fake-answer
  (provided (rand-nth fixtures/some-guys) => :fake-answer))