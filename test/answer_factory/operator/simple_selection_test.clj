(ns answer-factory.operator.simple-selection-test
  (:use midje.sweet)
  (:require [answer-factory.util.selection-fixtures :as fixtures])
  (:require [clj-uuid :as uuid])
  (:require [answer-factory.rubric.push :as rubric])
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
  (count (scores-for-answer fixtures/random-scores one-answer)) => 3))


(fact "scores-for-rubric"
  (let [one-rubric (first fixtures/some-rubrics)]
  (count (scores-for-rubric fixtures/random-scores one-rubric)) => 5))


(fact "numeric only throws an exception if the result is empty"
  (numeric-only fixtures/random-scores (rubric/error-rubric)) =>
    (throws #"No valid scores for rubric :id") )


;;; simple-selection



(fact "simple selection returns a vector of answers"
  (let [one-rubric (first fixtures/some-rubrics)]
    (type (simple-selection fixtures/some-guys fixtures/random-scores one-rubric)) =>
      clojure.lang.PersistentVector
    (type (first (simple-selection fixtures/some-guys fixtures/random-scores one-rubric))) =>
      answer_factory.answer.push.PushAnswer))


(fact "simple selection returns the items with the lowest score indicated by the rubric argument"
  (let [one-rubric (first fixtures/some-rubrics)
        winners (simple-selection fixtures/some-guys fixtures/random-scores one-rubric)]
    (count winners) => 1
    fixtures/some-guys => (contains winners)

    (:answer-id
      (first (sort-by :score (scores-for-rubric fixtures/random-scores one-rubric)))) =>
      (:id (first winners))))


(fact "simple-selection ignores unevaluated scores"
  (let [one-rubric (first fixtures/some-rubrics)
        missing-scores (fixtures/make-score-table [:missing 1 2
                                                   :missing 3 4
                                                   :missing 5 6
                                                   :missing 7 8
                                                   :missing 9 0])]
    (simple-selection fixtures/some-guys missing-scores one-rubric) => 
      (throws #"No valid scores for rubric :id") ;; every score with that rubric removed
    (simple-selection
      fixtures/some-guys
      (assoc-in missing-scores [0 :score] 999)
      one-rubric) => (vector (first fixtures/some-guys))
      ))


;;; simple-cull


(fact "simple-cull all Answers with the worst scores removed on the specified Rubric"
  (let [scores  (fixtures/make-score-table [0 1 9
                                            0 2 9
                                            0 3 1
                                            1 4 9
                                            1 5 9])]
  (simple-cull
    fixtures/some-guys
    scores
    (first fixtures/some-rubrics)) => (take 3 fixtures/some-guys)
  (simple-cull
    fixtures/some-guys
    scores
    (second fixtures/some-rubrics)) => (take 4 fixtures/some-guys)
  (simple-cull
    fixtures/some-guys
    scores
    (last fixtures/some-rubrics)) => [(nth fixtures/some-guys 2)]))



(fact "simple-cull does not remove non-numeric scoring items"
  (let [scores  (fixtures/make-score-table [0 1 9
                                            0 2 :whaaa?
                                            0 3 1
                                          nil 4 9
                                            8 5 9])]
  (simple-cull
    fixtures/some-guys
    scores
    (first fixtures/some-rubrics)) => (take 4 fixtures/some-guys)
  (simple-cull
    fixtures/some-guys
    scores
    (last fixtures/some-rubrics)) => [(nth fixtures/some-guys 1) (nth fixtures/some-guys 2)]))
