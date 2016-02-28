(ns answer-factory.util.selection-fixtures
  (:use midje.sweet)
  (:use answer-factory.answer.push)
  (:use answer-factory.rubric.push))


;; some fixtures


(defn empty-answer
  []
  (make-pushanswer [] :bb8))


(def some-guys (take 5 (repeatedly empty-answer)))


(def some-rubrics [(error-rubric) (error-rubric) (error-rubric)])


(defn make-score-table
  "creates a fixture using some-guys, some-rubrics, and a seq of 15 score values"
  [scores]
  (let [empty-table (into [] (for [dude some-guys
                                  rubr some-rubrics]
                                  {:answer-id (:id dude)
                                   :rubric-id (:id rubr)
                                   :score nil}))]
    (into [] (for [i (range (count empty-table))]
      (assoc (nth empty-table i) :score (nth scores i))))))


(fact "make-score-table produces a valid table"
  (map :score (make-score-table (repeat 15 6))) => (repeat 15 6)
  (set (map :answer-id (make-score-table (repeat 15 6)))) => (set (map :id some-guys))
  (set (map :rubric-id (make-score-table (repeat 15 6)))) => (set (map :id some-rubrics))
  )


(def random-scores (make-score-table (take 15 (repeatedly rand))))

