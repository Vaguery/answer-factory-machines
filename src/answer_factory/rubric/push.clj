(ns answer-factory.rubric.push
    (:use midje.sweet)

  (:require [push.core :as push])
  (:require [clj-uuid :as uuid])
  (:use answer-factory.answer.push)
  )



(defrecord TestCase [id note context-map results-fn])


(defn test-case
  "Creates a new TestCase record. The `note` argument should be a human-readable string; `interpreter` should be an instance; `config` is a map of interpreter configuration that will be merged with that in the `interpreter` arg before running; `inputs` is a map (not vector) of keyword-named input bindings; `result-fn` should take an interpreter state as its arg and return a keyword-labeled map of results values (any type)"
  [& {:keys [note interpreter config inputs results-fn]
      :or {note        "unnamed test case"
           interpreter (push/interpreter)
           config      {}
           inputs      {}
           results-fn  (fn [i] {})  ;; this default fn returns an empty map!
           }}] 
  (->TestCase
    (uuid/v1)
    note
    { :interpreter (-> interpreter
                       (assoc :config (merge (:config interpreter) config))
                       (assoc :inputs inputs))
      :config      config
      :inputs      inputs}
    results-fn))


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



(defrecord ErrorRubric [id testcase score-fn])


(defn error-rubric
  [id testcase score-func]
  (->ErrorRubric id testcase score-func))


