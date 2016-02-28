(ns answer-factory.operator.simple-selection-test
  (:use midje.sweet)
  (:require [answer-factory.util.selection-fixtures :as fixtures])
  (:require [clj-uuid :as uuid])
  (:use answer-factory.operator.select))


(fact "appears-on-list-of-ids?"
  (appears-on-list-of-ids?
    (first fixtures/some-guys)
    (map :id fixtures/some-guys)) => true
  (appears-on-list-of-ids?
    (first fixtures/some-guys)
    (take 5 (repeatedly uuid/v4))) => false
  (appears-on-list-of-ids?
    (first fixtures/some-guys)
    '()) => false)


(fact "scores-for-answer"
  (let [one-answer (first fixtures/some-guys)]
  (count (scores-for-answer fixtures/some-scores one-answer)) => 3))


(fact "scores-for-rubric"
  (let [one-rubric (first fixtures/some-rubrics)]
  (count (scores-for-rubric fixtures/some-scores one-rubric)) => 5))


(fact "simple selection returns a vector of answers"
  (let [one-rubric (first fixtures/some-rubrics)]
    (type (simple-selection fixtures/some-guys fixtures/some-scores one-rubric)) =>
      clojure.lang.PersistentVector
    (type (first (simple-selection fixtures/some-guys fixtures/some-scores one-rubric))) =>
      answer_factory.answer.push.PushAnswer))


(fact "simple selection returns the items with the lowest score indicated by the rubric argument"
  (let [one-rubric (first fixtures/some-rubrics)
        winners (simple-selection fixtures/some-guys fixtures/some-scores one-rubric)]
    (count winners) => 1
    fixtures/some-guys => (contains winners)

    (:answer-id
      (first (sort-by :score (scores-for-rubric fixtures/some-scores one-rubric)))) =>
      (:id (first winners))))


(future-fact "simple-selection ignores unevaluated scores"
  (let [one-rubric (first fixtures/some-rubrics)
        missing-scores (assoc-in fixtures/some-scores [0 :score] :missing)
        winners (simple-selection fixtures/some-guys missing-scores one-rubric)]
    (first winners) => 88
    ))
