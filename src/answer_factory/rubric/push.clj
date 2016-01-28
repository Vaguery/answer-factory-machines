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


(defrecord ErrorRubric [id testcase score-fn])


(defn error-rubric
  [id testcase score-func]
  (->ErrorRubric id testcase score-func))


