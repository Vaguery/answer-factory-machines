(ns answer-factory.operator.multiobjective-test
  (:use midje.sweet)
  (:use answer-factory.answer.push)
  (:require [answer-factory.util.selection-fixtures :as fixtures])
  (:use answer-factory.operator.select)
  (:use answer-factory.util.test))



;; salient-scores



(fact "returns only the specified subset"
  (let [guy1 (first fixtures/some-guys)
        rub1 (first fixtures/some-rubrics)]
  (salient-scores
    guy1
    fixtures/random-scores
    [rub1]) => [(:score (first fixtures/random-scores))]
  (count (salient-scores
    guy1
    fixtures/random-scores
    fixtures/some-rubrics)) => 3
  (salient-scores
    guy1
    fixtures/random-scores        ;; VV checking they're the right ones
    fixtures/some-rubrics) => (map :score (map #(nth fixtures/random-scores %) [0 1 2]))))



(fact "salient-scores throws an Exception if the rubrics aren't in a collection"
  (salient-scores
    (first fixtures/some-guys)
    fixtures/random-scores
    (first fixtures/some-rubrics)) => (throws #"collection of Rubric records"))



(fact "salient-scores returns whatever value is present"
  (let [scores  (fixtures/make-score-table [nil 1 :bad
                                            1   1 1
                                            0   1 1
                                            1   1 1
                                            1   1 1])]
  (salient-scores
    (first fixtures/some-guys)
    scores
    fixtures/some-rubrics) => [nil (:score (nth scores 1)) :bad]))



;; dominated-by?



(fact "dominated-by? returns false if any of the specified rubrics' scores are non-numeric"
  (let [scores  (fixtures/make-score-table [1 1 :bad
                                            1 1 1
                                            0 1 1
                                            1 1 1
                                            1 1 1])]
    (dominated-by?
      (first fixtures/some-guys)
      (second fixtures/some-guys)
      scores
      fixtures/some-rubrics) => false))



(fact "dominated-by? returns false if no rubrics are specified"
  (let [scores  (fixtures/make-score-table [1 1 0
                                            1 1 1
                                            0 1 1
                                            1 1 1
                                            1 1 1])]
    (dominated-by?
      (first fixtures/some-guys)
      (second fixtures/some-guys)
      scores
      (list)) => false))



(fact "dominated-by? acts like simple-selection if only one rubric is specified"
  (let [scores  (fixtures/make-score-table [1 1 0
                                            1 1 1
                                            0 1 1
                                            1 1 1
                                            1 1 1])]
    (dominated-by?
      (nth fixtures/some-guys 1)
      (nth fixtures/some-guys 2)
      scores
      (take 1 fixtures/some-rubrics)) => true
    (dominated-by?
      (nth fixtures/some-guys 1)
      (nth fixtures/some-guys 0)
      scores
      (drop 2 fixtures/some-rubrics)) => true
    (dominated-by?
      (nth fixtures/some-guys 3)
      (nth fixtures/some-guys 4)
      scores                       ;; same score
      [(nth fixtures/some-rubrics 1)]) => false))



(fact "dominated-by? is false when comparing an individual to itself"
  (dominated-by?
    (nth fixtures/some-guys 1)
    (nth fixtures/some-guys 1)
    fixtures/random-scores
    fixtures/some-rubrics) => false)



(fact "dominated-by? does actual multiobjective comparisons"
  (let [scores  (fixtures/make-score-table [1 1 0
                                            1 0 1
                                            0 1 1
                                            0 1 2
                                            0 0 0])]
    (dominated-by?
      (nth fixtures/some-guys 1)
      (nth fixtures/some-guys 2)
      scores
      fixtures/some-rubrics) => false
    (dominated-by?
      (nth fixtures/some-guys 2)
      (nth fixtures/some-guys 1)
      scores
      fixtures/some-rubrics) => false
    (dominated-by?
      (nth fixtures/some-guys 3)
      (nth fixtures/some-guys 2)
      scores
      fixtures/some-rubrics) => true
    (dominated-by?
      (nth fixtures/some-guys 2)
      (nth fixtures/some-guys 3)
      scores
      fixtures/some-rubrics) => false
    (map #(dominated-by?
      %
      (nth fixtures/some-guys 4)
      scores
      fixtures/some-rubrics)
      fixtures/some-guys) => [true true true true false]))



(fact "dominated-by? works with partial lists of rubrics"
  (let [scores  (fixtures/make-score-table [1 1 0
                                            1 0 1
                                            0 1 1
                                            0 1 2
                                            0 0 0])]
    (dominated-by?
      (nth fixtures/some-guys 3)
      (nth fixtures/some-guys 2)
      scores
      (take 2 fixtures/some-rubrics)) => false
    (dominated-by?
      (nth fixtures/some-guys 3)
      (nth fixtures/some-guys 4)
      scores
      (take 1 fixtures/some-rubrics)) => false
    (dominated-by?
      (nth fixtures/some-guys 1)  ;; [1 _ 1]
      (nth fixtures/some-guys 2)  ;; [0 _ 1]
      scores
      [(first fixtures/some-rubrics) (last fixtures/some-rubrics)]) => true))
    


;; filter-out-dominated



(fact "filter-out-dominated removes answers dominated by a specific one"
  (let [scores  (fixtures/make-score-table [1 1 0
                                            1 0 1
                                            0 1 1
                                            0 1 2
                                            0 0 0])]
  (filter-out-dominated
    (first fixtures/some-guys)
    fixtures/some-guys
    scores
    fixtures/some-rubrics) => fixtures/some-guys ;; first one dominates nobody

  (filter-out-dominated
    (nth fixtures/some-guys 4)
    fixtures/some-guys
    scores
    fixtures/some-rubrics) => [(last fixtures/some-guys)] ;; last guy dominates all

  (filter-out-dominated
    (nth fixtures/some-guys 2)
    fixtures/some-guys
    scores
    fixtures/some-rubrics) =not=> (contains (nth fixtures/some-guys 3)) ;; #3 > #4
  (count (filter-out-dominated
    (nth fixtures/some-guys 2)
    fixtures/some-guys
    scores
    fixtures/some-rubrics)) => 4))



(fact "filter-out-dominated does not remove items with missing scores"
  (let [scores  (fixtures/make-score-table [1 1 0
                                            1 0 1
                                            0 1 1
                                            0 1 :missing
                                            0 0 0])]
  (filter-out-dominated
    (nth fixtures/some-guys 2)
    fixtures/some-guys
    scores
    fixtures/some-rubrics) => fixtures/some-guys))



(fact "filter-out-dominated pays attention to the particular Rubrics"
  (let [scores  (fixtures/make-score-table [1 1 0
                                            1 0 1
                                            0 1 1
                                            0 1 2
                                            0 0 0])]
  (filter-out-dominated
    (nth fixtures/some-guys 3)
    fixtures/some-guys
    scores
    fixtures/some-rubrics) => fixtures/some-guys
  (filter-out-dominated
    (nth fixtures/some-guys 3)
    fixtures/some-guys
    scores                              ;; first guy gone V
    (take 2 fixtures/some-rubrics)) => (drop 1 fixtures/some-guys)))



;; nondominated-selection

(fact "nondominated-selection removes all dominated answers"
  (let [scores  (fixtures/make-score-table [1 1 0
                                            1 0 1
                                            0 1 1
                                            0 1 2
                                            0 0 0])]
  (nondominated-selection
    fixtures/some-guys
    scores
    fixtures/some-rubrics) => [(last fixtures/some-guys)]
  (nondominated-selection
    (take 4 fixtures/some-guys)
    scores
    fixtures/some-rubrics) => (take 3 fixtures/some-guys)))



(fact "nondominated-selection obeys rubric lists"
  (let [scores  (fixtures/make-score-table [1 1 0
                                            1 0 1
                                            0 1 1
                                            0 1 2
                                            0 0 0])]
  (nondominated-selection
    fixtures/some-guys
    scores
    (take 1 fixtures/some-rubrics)) => (drop 2 fixtures/some-guys)
  (nondominated-selection
    (take 4 fixtures/some-guys)
    scores
    (drop 2 fixtures/some-rubrics)) => (take 1 fixtures/some-guys)))



(fact "nondominated-selection retains any Answers with missing scores"
  (let [scores  (fixtures/make-score-table [1 1 0
                                            1 0 nil
                                            0 1 1
                                            0 1 2
                                            0 0 0])]
  (nondominated-selection
    fixtures/some-guys
    scores
    fixtures/some-rubrics) => (list (nth fixtures/some-guys 1) (last fixtures/some-guys))
  (nondominated-selection
    fixtures/some-guys
    scores
    (drop 2 fixtures/some-rubrics)) =>
      (list (first fixtures/some-guys) (nth fixtures/some-guys 1) (last fixtures/some-guys))))


(fact "nondominated-selection retains any Answers with keyword scores"
  (let [scores  (fixtures/make-score-table [1 1 0
                                            1 0 :missing
                                            0 1 1
                                            0 1 2
                                            0 0 0])]
  (nondominated-selection
    fixtures/some-guys
    scores
    fixtures/some-rubrics) => (list (nth fixtures/some-guys 1) (last fixtures/some-guys))
  (nondominated-selection
    fixtures/some-guys
    scores
    (drop 2 fixtures/some-rubrics)) =>
      (list (first fixtures/some-guys) (nth fixtures/some-guys 1) (last fixtures/some-guys))))



;; nondomination-sort



(fact "nondomination-sort works as expected"
  (let [scores  (fixtures/make-score-table [1 1 0
                                            1 0 1
                                            0 1 1
                                            0 1 2
                                            0 0 0])]
  (count (nondomination-sort
    fixtures/some-guys
    scores
    fixtures/some-rubrics)) => 3
  (first (nondomination-sort
    fixtures/some-guys
    scores
    fixtures/some-rubrics)) => [(last fixtures/some-guys)]
  (last (nondomination-sort
    fixtures/some-guys
    scores
    fixtures/some-rubrics)) => [(nth fixtures/some-guys 3)]))



(fact "nondomination-sort respects the Rubrics given"
  (let [scores  (fixtures/make-score-table [1 1 0
                                            1 0 1
                                            0 1 1
                                            0 1 2
                                            0 0 0])]
  (count (nondomination-sort
    fixtures/some-guys
    scores
    (drop 2 fixtures/some-rubrics))) => 3
  (map count (nondomination-sort
    fixtures/some-guys
    scores
    (drop 2 fixtures/some-rubrics))) => [2 2 1]
  (last (nondomination-sort
    fixtures/some-guys
    scores
    (drop 2 fixtures/some-rubrics))) => [(nth fixtures/some-guys 3)]))



;;; most-dominated-cull

(fact "most-dominated-cull removes all the 'most' dominated answers"
  (let [scores  (fixtures/make-score-table [9 3 3
                                            1 0 1
                                            0 1 1
                                            0 1 2
                                            0 0 0])]
  (most-dominated-cull
    fixtures/some-guys
    scores
    fixtures/some-rubrics) => (drop 1 fixtures/some-guys)
  (most-dominated-cull
    (drop 1 fixtures/some-guys)
    scores
    fixtures/some-rubrics) => (map #(nth fixtures/some-guys %) [1 2 4])
  ))



(fact "most-dominated-cull will not eliminate everybody"
  (let [scores  (fixtures/make-score-table [1 2 3
                                            1 2 3
                                            1 2 3
                                            1 2 3
                                            1 2 3])]
  (most-dominated-cull
    fixtures/some-guys
    scores
    fixtures/some-rubrics) => fixtures/some-guys))


(fact "most-dominated-cull does not remove answers with non-numeric scores"
  (let [scores  (fixtures/make-score-table [1 2 :bad
                                            1 2 3
                                          nil 2 3
                                            1 2 3
                                            1 2 3])]
  (most-dominated-cull
    fixtures/some-guys
    scores
    fixtures/some-rubrics) => fixtures/some-guys))
