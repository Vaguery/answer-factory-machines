(ns answer-factory.rubric.rubric-test
  (:use midje.sweet)
  (:use answer-factory.rubric.push)
  (:require [push.core :as push]))




(fact "I can create a TestCase and it won't be an annoying experience, with sensible defaults"
  (keys (test-case)) => '(:id :note :context-map :results-fn)

  (:note (test-case)) => "unnamed test case"
  (:note (test-case :note "foo")) => "foo"

  (get-in (test-case) [:context-map :interpreter :program]) => []
  (get-in (test-case :interpreter (push/interpreter :program [1 2 3]))
    [:context-map :interpreter :program]) => [1 2 3]

  (get-in (test-case) [:context-map :interpreter :config :step-limit]) => 0
  (get-in (test-case) [:context-map :interpreter :config :lenient?]) => false

  (get-in (test-case :config {:lenient? true :step-limit 999})
    [:context-map :interpreter :config :step-limit]) => 999
  (get-in (test-case :config {:lenient? true :step-limit 999})
    [:context-map :interpreter :config :lenient?]) => true

  (get-in (test-case) [:context-map :inputs]) => {}
  (get-in (test-case :inputs {:foo 7.7}) [:context-map :inputs]) => {:foo 7.7}

  ((:results-fn (test-case)) (push/interpreter)) => {} ;; apply default results-fn to an interpreter
  (let [steppy (test-case :results-fn (fn [i] {:steps (:counter i)}))]
    ((:results-fn steppy)  (push/interpreter)) => {:steps 0}))



; context-map items
; - language
; - interpreter config
; - input assignments
;
; (run the program in that context)
;
; results-fn
; - access interpreter state
; - returns a map of results
; - map should not contain a score!


