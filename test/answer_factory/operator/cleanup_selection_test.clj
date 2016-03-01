(ns answer-factory.operator.cleanup-selection-test
  (:use midje.sweet)
  (:require [answer-factory.util.selection-fixtures :as fixtures])
  (:use answer-factory.operator.select))



(fact "remove-uncooperative filters out answers with bad scores"
  (let [scores  (fixtures/make-score-table [1 1 :bad
                                            1 1 1
                                          nil 1 1
                                            1 1 1
                                            1 1 1])]
    (remove-uncooperative
      fixtures/some-guys
      scores
      fixtures/some-rubrics) => (map #(nth fixtures/some-guys %) [1 3 4])
    (remove-uncooperative
      fixtures/some-guys
      fixtures/random-scores
      fixtures/some-rubrics) => fixtures/some-guys
    ))



(fact "select-on-nil returns answers with nil scores"
  (let [scores  (fixtures/make-score-table [1 1 :bad
                                            1 1 1
                                          nil 1 1
                                            1 1 1
                                            1 1 1])]
    (select-on-nil
      fixtures/some-guys
      scores
      fixtures/some-rubrics) => (map #(nth fixtures/some-guys %) [2])
    (select-on-nil
      fixtures/some-guys
      scores
      (drop 1 fixtures/some-rubrics)) => []
    (select-on-nil
      fixtures/some-guys
      fixtures/random-scores
      fixtures/some-rubrics) => []
    ))
