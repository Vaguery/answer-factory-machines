(ns answer-factory.operator.lexicase-test
  (:use midje.sweet)
  (:use answer-factory.answer.push)
  (:use answer-factory.operator.select))


;
; (fact "lexicase-selection will return the one answer if only one was passed in"
;   (count (lexicase-selection
;     (list (first fixtures/some-guys))
;     fixtures/random-scores
;     fixtures/some-rubrics)) => 1)
;
;
; (fact "lexicase-selection will do simple-selection if only one rubric is used"
;   (lexicase-selection
;     fixtures/some-guys
;     fixtures/random-scores
;     (list (first fixtures/some-rubrics))) =>
;   (simple-selection fixtures/some-guys fixtures/random-scores (first fixtures/some-rubrics)))
;
;
; (fact "lexicase-selection does no filtering at all if no rubrics are specified"
;   (lexicase-selection
;     fixtures/some-guys
;     fixtures/random-scores
;     (list)) => fixtures/some-guys)
;
;
; (fact "lexicase-selection will return the answer with the best score in a single objective"
;   (let [column1 (fixtures/make-score-table [1 1 1
;                                             1 1 1
;                                             0 1 1
;                                             1 1 1
;                                             1 1 1])]
;     (lexicase-selection
;       fixtures/some-guys
;       column1
;       fixtures/some-rubrics) => (list (nth fixtures/some-guys 2))))
;
;
;
; (fact "lexicase-selection will return several answers if there is no one with best scores"
;   (let [a-and-b (fixtures/make-score-table [0 0 1
;                                             1 1 1
;                                             0 0 1
;                                             1 1 1
;                                             1 1 1])]
;     (lexicase-selection
;       fixtures/some-guys
;       a-and-b
;       fixtures/some-rubrics) =>
;         (contains [(nth fixtures/some-guys 0) (nth fixtures/some-guys 2)])
;     (lexicase-selection
;       fixtures/some-guys
;       a-and-b
;       (drop 2 fixtures/some-rubrics)) => fixtures/some-guys))
;                                           ;; ^^ all of them
;
;
; (fact "lexicase-selection will filter out missing values"
;   (let [a-and-b (fixtures/make-score-table [0 0 :missing
;                                             1 1 1
;                                             0 0 1
;                                             1 1 1
;                                             1 1 1])]
;     (lexicase-selection
;       fixtures/some-guys
;       a-and-b
;       fixtures/some-rubrics) => (list (nth fixtures/some-guys 2))
;     (lexicase-selection
;       fixtures/some-guys
;       a-and-b
;       (drop 2 fixtures/some-rubrics)) => (drop 1 fixtures/some-guys)
;     (lexicase-selection
;       fixtures/some-guys
;       a-and-b
;       (take 2 fixtures/some-rubrics)) => (map #(nth fixtures/some-guys %) '(0 2))))
;
;
;
; ;;; lexicase-cull
;
;
; (fact "lexicase-cull will remove everybody if they are all equivalent!"
;   (let [column1 (fixtures/make-score-table [1 1 1
;                                             1 1 1
;                                             1 1 1
;                                             1 1 1
;                                             1 1 1])]
;     (lexicase-cull
;       fixtures/some-guys
;       column1
;       fixtures/some-rubrics) => (list)
; ))
;
;
;
; (fact "lexicase-cull will remove the worst if it's obvious"
;   (let [column1 (fixtures/make-score-table [1 1 1
;                                             1 1 1
;                                             0 1 1
;                                             0 0 1
;                                             0 0 0])]
;     (lexicase-cull
;       fixtures/some-guys
;       column1
;       fixtures/some-rubrics) => (drop 2 fixtures/some-guys)
; ))
;
;
; (fact "lexicase-cull pays attention to the rubrics"
;   (let [column1 (fixtures/make-score-table [1 1 1
;                                             1 1 1
;                                             0 1 1
;                                             0 0 1
;                                             0 0 0])]
;     (lexicase-cull
;       fixtures/some-guys
;       column1
;       (drop 1 fixtures/some-rubrics)) => (drop 3 fixtures/some-guys)
;     (lexicase-cull
;       fixtures/some-guys
;       column1
;       (drop 2 fixtures/some-rubrics)) => (drop 4 fixtures/some-guys)))
;
;
; (fact "WARNING lexicase-cull retains the dudes with the bad scores"
;   (let [column1 (fixtures/make-score-table [1 1 :bad
;                                           nil 1 1
;                                             1 1 1
;                                             1 1 1
;                                             1 1 1])]
;     (lexicase-cull
;       fixtures/some-guys
;       column1
;       fixtures/some-rubrics) => (take 2 fixtures/some-guys)
; ))
