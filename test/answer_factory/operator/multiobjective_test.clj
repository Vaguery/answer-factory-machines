(ns answer-factory.operator.multiobjective-test
  (:use midje.sweet)
  (:use answer-factory.answer.push)
  (:use answer-factory.operator.select)
  (:use answer-factory.util.test))


(fact "every-rubric returns a set that has every rubric with a key in every answer"
  (every-rubric [(dude-with-scores {:x 1 :y 1 :z 1})]) => #{:x :y :z}
  (every-rubric [(dude-with-scores {:x 1 :y 1 :z 1})
                 (dude-with-scores {:a 1 :b 1 :z 1})]) => #{:a :b :x :y :z}
  (every-rubric [(dude-with-scores {})
                 (dude-with-scores {})]) => #{})


(fact "dominated-by? returns true when the second answer dominates the first on their matching rubrics (if none are specified)"
  (let [d1 (dude-with-scores {:x 1 :y 1 :z 1})
        d2 (dude-with-scores {:x 1 :y 1 :z 2})
        d3 (dude-with-scores {:x 2 :y 1 :z 1})]
    (dominated-by? d1 d2) => false
    (dominated-by? d2 d1) => true
    (dominated-by? d1 d1) => false
    (dominated-by? d2 d3) => false
    (dominated-by? d3 d2) => false
    (dominated-by? d3 d1) => true))


(fact "dominated-by? uses the specified rubrics"
  (let [d1 (dude-with-scores {:x 1 :y 1 :z 1})
        d2 (dude-with-scores {:x 1 :y 1 :z 2})
        d3 (dude-with-scores {:x 2 :y 1 :z 1})]
    (dominated-by? d2 d1 [:x :y]) => false
    (dominated-by? d2 d1 [:x :y :z]) => true
    (dominated-by? d2 d1 [:z]) => true

    (dominated-by? d2 d3) => false
    (dominated-by? d2 d3 [:x]) => false
    (dominated-by? d2 d3 [:z]) => true))


(fact "dominated-by? returns false if either answer has different rubrics (or any are nil)"
  (let [d1 (dude-with-scores {:x 1 :y 1 :z 1})
        d2 (dude-with-scores {:x 1 :y 1 :z 2 :Q 99})
        d3 (dude-with-scores {:x 1 :y 1 :z nil})]
    (dominated-by? d2 d1) => false
    (dominated-by? d1 d2) => false
    (dominated-by? d1 d3) => false
    (dominated-by? d3 d1) => false
    ))

;; nondominated


(fact "remove-dominated returns the subset of the answers passed in that are not dominated by the first arg"
  (let [d1 (dude-with-scores {:x 1 :y 1 :z 1})
        d2 (dude-with-scores {:x 1 :y 1 :z 2})
        d3 (dude-with-scores {:x 2 :y 1 :z 1})]
    (remove-dominated d1 [d1 d2 d3]) => [d1]
    (remove-dominated d2 [d1 d2 d3]) => [d1 d2 d3]))


(fact "remove-dominated passes along the optional rubrics argument if used"
  (let [d1 (dude-with-scores {:x 1 :y 1 :z 1})
        d2 (dude-with-scores {:x 1 :y 1 :z 2})
        d3 (dude-with-scores {:x 2 :y 1 :z 1})]
    (remove-dominated d1 [d1 d2 d3] [:x]) => [d1 d2]
    (remove-dominated d2 [d1 d2 d3] [:y]) => [d1 d2 d3]
    (remove-dominated d1 [d1 d2 d3] [:z]) => [d1 d3]))


(fact "remove-dominated does not remove items with rubrics the comparing answer lacks"
  (let [d1 (dude-with-scores {:x 1 :y 1 :z 1})
        d2 (dude-with-scores {:x 1 :y 1 :z 2})
        d3 (dude-with-scores {:x 2 :y 1 :z nil})]
    (remove-dominated d1 [d1 d2 d3]) => [d1 d3]
    (remove-dominated d2 [d1 d2 d3]) => [d1 d2 d3]
    (remove-dominated d1 [d1 d2 d3] [:z]) => [d1 d3]))



(fact "remove-dominated does not remove items lacking rubrics the comparing answer has"
  (let [d1 (dude-with-scores {:x 1 :y 1 :z 1})
        d2 (dude-with-scores {:x 1 :y 1 :z 2})
        d3 (dude-with-scores {:x 2 :y 1 :z 1 :Q 99})]
    (remove-dominated d1 [d1 d2 d3]) => [d1 d3]
    (remove-dominated d2 [d1 d2 d3]) => [d1 d2 d3]
    (remove-dominated d3 [d1 d2 d3] [:Q]) => [d1 d2 d3]))



(fact "nondominated returns the subset of a collection of answers that are mutually nondominated"
  (let [d1 (dude-with-scores {:x 1 :y 1 :z 1})
        d2 (dude-with-scores {:x 1 :y 1 :z 2})
        d3 (dude-with-scores {:x 2 :y 1 :z 1})
        d4 (dude-with-scores {:x 2 :y 2 :z 1})]
    (nondominated [d1 d2 d3 d4]) => [d1]
    (nondominated [d2 d3]) => [d2 d3]
    (nondominated [d2 d3 d4]) => [d2 d3]
    (nondominated [d4]) => [d4]
    (nondominated [d2 d4]) => [d2 d4]))


(fact "nondominated respects the optional rubrics argument"
  (let [d1 (dude-with-scores {:x 1 :y 1 :z 1})
        d2 (dude-with-scores {:x 1 :y 1 :z 2})
        d3 (dude-with-scores {:x 2 :y 1 :z 1})
        d4 (dude-with-scores {:x 2 :y 2 :z 1})]
    (nondominated [d1 d2 d3 d4] [:x]) => [d1 d2]
    (nondominated [d1 d2 d3 d4] [:x :z]) => [d1]
    (nondominated [d1 d2 d3 d4] [:x :y]) => [d1 d2]
    (nondominated [   d2 d3 d4] [:x :y]) => [d2]
    (nondominated [   d2 d3 d4] [:x :z]) => [d2 d3 d4]))



(fact "if any answer has a score others don't, or lacks one others have, the entire set is returned regarless of specifying rubrics"
  (let [d1 (dude-with-scores {:x 1 :y 1 :z 1})
        d2 (dude-with-scores {:x 1 :y 1 :z 2})
        d3 (dude-with-scores {:x 2 :y 1 :z 1 :Q 1})]
    (nondominated [d1 d2 d3]) => [d1 d2 d3])

  (let [d1 (dude-with-scores {:x 1 :y 1 :z 1})
        d2 (dude-with-scores {:x 1 :y 1 :z 2})
        d3 (dude-with-scores {:x 2 :y 1 :z nil})]
    (nondominated [d1 d2 d3]) => [d1 d2 d3])

  (let [d1 (dude-with-scores {:x 1 :y 1 :z 1})
        d2 (dude-with-scores {:x 1 :y 1 :z 2 :Q 88})
        d3 (dude-with-scores {:x 2 :y 1 :z nil})]
    (nondominated [d1 d2 d3]) => [d1 d2 d3]))


