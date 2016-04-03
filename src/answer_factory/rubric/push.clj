(ns answer-factory.rubric.push
  (:use midje.sweet)

  (:require [push.core :as push])
  (:require [clj-uuid :as uuid])
  (:require [push.interpreter.core :as i])
  (:require [clojure.set :as s])
  (:require [clojure.math.numeric-tower :as math])
  (:use answer-factory.answer.push)
  )



(defrecord TestCase [id note context inputs expected])


(defn nil-all-the-things
  "replaces every value in a hash-map with nil"
  [m]
  (into {} (map (fn [[k v]] [k nil]) m)))


(defn nil-map-from-key-set
  "produces a hash-map where each element of the set of keys has a nil value"
  [s]
  (reduce (fn [m i] (merge m {i nil})) {} s))



(defn test-case
  "Creates a new TestCase record. The `note` argument should be a human-readable string; `interpreter` should be an instance; `config` is a map of interpreter configuration that will be merged with that in the `interpreter` arg before running; `inputs` is a map (not vector) of keyword-named input bindings; `expected` is a map of keyword-named bindings OR stacks (can be mixed), with the target value indicated for each keyword. For example, `{:y 7 :integer 18}` refers to one stack (`:integer`) and one binding (`:y`); any keyword not already present in the interpreter AS A STACK will be assumed to be an output binding."
  [& {:keys [note interpreter config inputs expected]
      :or {note        "unnamed test case"
           interpreter (push/interpreter :program [])
           config      {}
           inputs      {}
           expected    {}
           }}]
  (let [expected-stacks
          (s/intersection (set (keys expected)) (set (keys (:stacks interpreter))))
        output-bindings
          (s/difference (set (keys expected)) (set (keys (:stacks interpreter))))]
  (->TestCase
    (uuid/v4)
    note
    (-> interpreter
        (assoc :config (merge (:config interpreter) config))
        (i/bind-inputs (merge inputs (nil-map-from-key-set output-bindings)))
        (i/reset-interpreter))      ;; note this will simply clear stacks
    inputs
    expected
    )))


(defn extract-bindings
  [interpreter keywords]
  (reduce (fn [m k] (merge m {k (get-in interpreter [:bindings k])})) {} keywords))


(defn extract-stacks
  [interpreter keywords]
  (reduce (fn [m k] (merge m {k (get-in interpreter [:stacks k])})) {} keywords))


(defn extract-results
  "Takes an Interpreter and a collection of keywords. Returns hash-map with those keywords as keys, with values set to the stacks of the interpreter (if the keyword is a stackname), or binding stacks (if a binding name). Unknown keywords are not included at all."
  [interpreter keywords]
  (let [kw-set     (set keywords)
        stack-kw   (s/intersection kw-set (set (keys (:stacks interpreter))))
        binding-kw (s/intersection kw-set (set (keys (:bindings interpreter))))]
    (merge
      (extract-bindings interpreter binding-kw)
      (extract-stacks   interpreter stack-kw))))


(defn exercise-test-case
  [t program]
  (-> (assoc (:context t) :program program)
      i/reset-interpreter
      (push/run , program (i/step-limit (:context t)))
      (extract-results , (keys (:expected t)))))



(defrecord ErrorRubric [id testcase score-fn])





(defn L1-distance-from-top-result
  "Compares the expected result to the top item on the indicated result stack, returning a hash-map with the indicated key and the absolute deviation as its value. If the key (or stack) is missing from the `results-hash`, `missing-value` is returned instead. If the value found in the `expected-hash` is non-numeric, a _match error_ value is returned instead: 0.0 if they are equal, 1.0 otherwise."
  [expected-hash results-hash missing-value]
  (let [k      (first (keys expected-hash))
        target (k expected-hash)
        guess  (first (k results-hash))]
    (if (nil? guess)
      {k missing-value}
      {k (if (number? guess)
            (double (math/abs (-' target guess)))
            (if (= target guess) 0.0 1.0))}
      )))


(defn error-rubric
  "Creates a new ErrorRubric record. Both keyworded arguments are necessary (:testcase and :score-fn). The `score-fn` should accept a hash-map produced by `extract-results`, which should contain all information needed for the scoring, and should return a single numerical value, with lower score values preferred."
  [& {:keys [testcase score-fn]}]
  (->ErrorRubric
    (uuid/v1)
    testcase
    score-fn))


(defn score-answer
  [answer rubric missing-value]
  (let [p      (:program answer)
        tc     (:testcase rubric)
        scorer (:score-fn rubric)]
    (scorer
      (:expected tc)
      (exercise-test-case tc p)
      missing-value)
    ))

