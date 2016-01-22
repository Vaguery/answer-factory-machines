(ns answer-factory.selection.multiobjective-test
  (:use midje.sweet)
  (:use answer-factory.answer.push)
  (:use answer-factory.selection.core)
  (:use answer-factory.test.util))


(fact "dominates? returns true when the first answer dominates the second on their matching rubrics (if none are specified)"
  (let [d1 (dude-with-scores {:x 1 :y 1 :z 1})
        d2 (dude-with-scores {:x 1 :y 1 :z 2})
        d3 (dude-with-scores {:x 2 :y 1 :z 1})]
    (dominates? d1 d2) => false
    (dominates? d2 d1) => true
    (dominates? d1 d1) => false
    (dominates? d2 d3) => false
    (dominates? d3 d2) => false
    (dominates? d3 d1) => true))