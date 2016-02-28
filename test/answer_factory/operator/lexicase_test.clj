(ns answer-factory.operator.lexicase-test
  (:use midje.sweet)
  (:use answer-factory.answer.push)
  (:require [answer-factory.util.selection-fixtures :as fixtures])
  (:use answer-factory.operator.select)
  (:use answer-factory.util.test))



(fact "lexicase-selection will return the one answer if only one was passed in"
  (count (lexicase-selection
    (list (first fixtures/some-guys))
    fixtures/random-scores
    fixtures/some-rubrics)) => 1)


(fact "lexicase-selection will do simple-selection if only one rubric is used"
  (lexicase-selection
    fixtures/some-guys
    fixtures/random-scores
    (list (first fixtures/some-rubrics))) =>
  (simple-selection fixtures/some-guys fixtures/random-scores (first fixtures/some-rubrics)))


(fact "lexicase-selection does no filtering if no rubrics are specified"
  (lexicase-selection
    fixtures/some-guys
    fixtures/random-scores
    (list)) => fixtures/some-guys)


; (fact "lexicase-selection will return the answer with the best score in a single objective"
;   (let [d1 (dude-with-scores {:x 1})
;         d9 (dude-with-scores {:x 9})]
;     (lexicase-selection [d1 d9] [:x]) => d1))


; ;; remove nil-scored answers


; (fact "purge-nils discards any entry which has a nil in any listed score"
;   (let [d0 (dude-with-scores {:x 1 :y 1 :z nil})
;         d1 (dude-with-scores {:x 1 :y 1 :z 2})
;         d2 (dude-with-scores {:x 2 :y 2 :z 1})]
;     (purge-nils [d0 d1 d2] [:x :y :z]) => (contains [d1 d2] :in-any-order)))


; (fact "purge-nils discards any entry which has a nil score"
;   (let [d0 (dude-with-scores {:x 1 :y 1 :z nil})
;         d1 (dude-with-scores {:x 1 :y 1 :z 2})
;         d2 (dude-with-scores {:x 2 :y 2 :z 1})]
;     (purge-nils [d0 d1 d2] [:x :y :z :foo]) => []))


; ;; filter-by-rubric


; (fact "filter-by-rubric should return all the answers with the lowest score of the indicated rubric"
;   (let [d1a (dude-with-scores {:x 1})
;         d1b (dude-with-scores {:x 1})
;         d9 (dude-with-scores {:x 9})]
;     (filter-by-rubric [d1a d1b d9] :x) => (contains d1a d1b)))



; (fact "filter-by-rubric does not return any answers where the score is nil for only the indicated rubric"
;   (let [d1a (dude-with-scores {:x 1 :y nil})
;         d1b (dude-with-scores {:x nil})
;         d9 (dude-with-scores {:x 9})]
;     (filter-by-rubric [d1a d1b d9] :x) => (contains d1a)))


; ;; lexicase selection


; (fact "lexicase-selection returns the best choice when there is exactly one"
;   (let [d9 (dude-with-scores {:x 9 :y 11})
;         d1 (dude-with-scores {:x 1 :y  9})]
;     (lexicase-selection [d1 d9] [:x :y]) => d1
;     (lexicase-selection [d1 d9] [:y :x]) => d1))


; (fact "lexicase-selection returns the best choice even when there are several paths to it"
;   (let [d0 (dude-with-scores {:x 1 :y 1 :z 1})
;         dz (dude-with-scores {:x 1 :y 1 :z 2})
;         dx (dude-with-scores {:x 2 :y 1 :z 1})]
;     (lexicase-selection [d0 dz dx] [:x :y :z]) => d0))


; (fact "lexicase selection randomly samples after exhausting rubrics"
;   (let [d0 (dude-with-scores {:x 1 :y 1 :z 1})
;         d1 (dude-with-scores {:x 1 :y 1 :z 1})
;         d2 (dude-with-scores {:x 1 :y 1 :z 1})]
;     (lexicase-selection [d0 d1 d2] [:x :y :z]) => :proxy-result-of-rand-nth
;     (provided
;       (rand-nth [d0 d1 d2]) => :proxy-result-of-rand-nth))

;   (let [d0 (dude-with-scores {:x 1 :y 1 :z 1})
;         d1 (dude-with-scores {:x 1 :y 1 :z 1})
;         d2 (dude-with-scores {:x 2 :y 1 :z 1})]
;     (lexicase-selection [d0 d1 d2] [:x :y :z]) => :proxy-result-of-rand-nth
;     (provided
;       (rand-nth [d0 d1]) => :proxy-result-of-rand-nth)))


; (fact "lexicase-selection discards any entry which has a nil score"
;   (let [d0 (dude-with-scores {:x 1 :y 1 :z nil})
;         d1 (dude-with-scores {:x 1 :y 1 :z 1})
;         d2 (dude-with-scores {:x 2 :y 2 :z 1})]
;     (lexicase-selection [d0 d1 d2] [:x :y :z]) => d1))

