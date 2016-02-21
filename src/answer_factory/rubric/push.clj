(ns answer-factory.rubric.push
    (:use midje.sweet)

  (:require [push.core :as push])
  (:require [clj-uuid :as uuid])
  (:require [push.interpreter.core :as i])
  (:use answer-factory.answer.push)
  )



(defrecord TestCase [id note context inputs expected])


(defn nil-all-the-things
  "replaces every value in a hash-map with nil"
  [m]
  (into {} (map (fn [[k v]] [k nil]) m)))



(defn test-case
  "Creates a new TestCase record. The `note` argument should be a human-readable string; `interpreter` should be an instance; `config` is a map of interpreter configuration that will be merged with that in the `interpreter` arg before running; `inputs` is a map (not vector) of keyword-named input bindings; `expected` is a map of keyword-named output bindings, with the values desired for each named binding."
  [& {:keys [note interpreter config inputs expected]
      :or {note        "unnamed test case"
           interpreter (push/interpreter :program [])
           config      {}
           inputs      {}
           expected    {}
           }}] 
  (->TestCase
    (uuid/v1)
    note
    (-> interpreter
        (assoc :config (merge (:config interpreter) config))
        (i/bind-inputs (merge inputs 
                          (nil-all-the-things expected)))
        (i/reset-interpreter))      ;; note this will simply clear stacks, with no program!
    inputs
    expected
    ))


(defn extract-bindings
  [interpreter keywords]
  (reduce (fn [m k] (merge m {k (get-in interpreter [:bindings k])})) {} keywords))



(defn exercise-test-case
  [t program]
  (-> (assoc (:context t) :program program)
      i/reset-interpreter
      (push/run , program 1000)
      (extract-bindings , (keys (:expected t)))))



(defrecord ErrorRubric [id testcase score-fn])


(defn error-rubric
  "Creates a new ErrorRubric record. Both keyworded arguments are necessary (:testcase and :score-fn)"
  [& {:keys [testcase score-fn]}]
  (->ErrorRubric
    (uuid/v1)
    testcase
    score-fn))


