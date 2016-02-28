(ns answer-factory.util.selection-fixtures
  (:use answer-factory.answer.push)
  (:use answer-factory.rubric.push))


;; some fixtures


(defn empty-answer
  []
  (make-pushanswer [] :bb8))


(def some-guys (take 5 (repeatedly empty-answer)))


(def some-rubrics [(error-rubric) (error-rubric) (error-rubric)])


(def some-scores
  (for [dude some-guys
        rubr some-rubrics]
        {:answer-id (:id dude)
         :rubric-id (:id rubr)
         :score (rand)}))