(ns answer-factory.rubric.rubric-test
  (:use midje.sweet)
  (:use answer-factory.rubric.push)
  (:require [push.core :as push])
  (:require [push.interpreter.core :as i]))




(fact "I can create a TestCase"
  (keys (test-case)) => '(:id :note :context :inputs :expected)

  (:note (test-case)) => "unnamed test case"
  (:note (test-case :note "foo=2")) => "foo=2"

  (get-in (test-case) [:context :program]) => []
  (get-in (test-case :interpreter (push/interpreter :program [1 2 3]))
    [:context :program]) => [1 2 3]

  (get-in (test-case) [:context :config :step-limit]) => 0
  (get-in (test-case) [:context :config :lenient?]) => false

  (get-in (test-case :config {:lenient? true :step-limit 999})
    [:context :config :step-limit]) => 999
  (get-in (test-case :config {:lenient? true :step-limit 999})
    [:context :config :lenient?]) => true

  (get-in (test-case) [:context :bindings]) => {}
  (get-in (test-case :inputs {:foo 7.7}) [:context :bindings]) => {:foo '(7.7)}
  (get-in (test-case :expected {:bar 1.1}) [:context :bindings]) => {:bar '()}
)


(fact "test-case can include expected values for stacks"
  (get-in (test-case :expected {:foo 1.1}) [:context :bindings]) => {:foo '()}
  (get-in (test-case :expected {:float 1.1}) [:context :bindings]) => {}
  (get-in (test-case :expected {:float 1.1}) [:expected]) => {:float 1.1}
  )


(fact "extract-bindings"
  (extract-bindings (push/interpreter :bindings {:a 3 :b 4}) '(:b)) => {:b '(4)}
  (extract-bindings (push/interpreter :bindings {:a 3 :b 4}) [:a :b]) => {:a '(3) :b '(4)}
  (extract-bindings (push/interpreter :bindings {:a 3 :b 4}) '()) => {})


(fact "extract-stacks"
  (extract-stacks (push/interpreter :bindings {:a 3 :b 4}) '(:integer)) => {:integer '()}
  (extract-stacks (push/interpreter :bindings {:a 3 :b 4}) '(:boolean :foo)) => 
    {:boolean '(), :foo nil}
  (extract-stacks (push/interpreter :bindings {:a 3 :b 4}) '()) => {})


(fact "extract-results"
  (extract-results (push/interpreter :bindings {:a 3 :b 4}) '(:integer :b)) =>
    {:integer '() :b '(4)}
  (extract-results (push/interpreter :bindings {:a 3 :b 4}) '(:foo :b)) =>
    {:b '(4)}
  (extract-results (push/interpreter :bindings {:a 3 :b nil}) '(:float :b)) =>
    {:b '() :float '()})


(fact "exercise-test-case works for output bindings"
  (let [t1 (test-case :inputs {:x 8} 
                      :expected {:y 7} 
                      :config {:step-limit 1000})]
    (exercise-test-case t1 [:x 12 :push-quoterefs :y :integer-add :integer-save]) => {:y '(20)}
    (exercise-test-case t1 [:x -2 :push-quoterefs :y :integer-add :integer-save]) => {:y '(6)}
    (exercise-test-case t1 [:x -2.1 :push-quoterefs :y :integer-add :integer-save]) => {:y '(8)}
    (exercise-test-case t1 [-2.1 :push-quoterefs :y :integer-add :integer-save]) => {:y '()}
    (exercise-test-case t1 [-2.1 :push-quoterefs :y :float-save :x]) => {:y '(-2.1)}
    ))


(fact "exercise-test-case works for stack names"
  (let [t1 (test-case :inputs {:x 8}
                      :expected {:integer 7} 
                      :config {:step-limit 100})]
    (exercise-test-case t1 [:x 12 :integer-add]) => {:integer '(20)}
    (exercise-test-case t1 [:x 12 :integer-add :integer-dup]) => {:integer '(20 20)}
    ))


(fact "exercise-test-case works for mixed keywords"
  (let [t1 (test-case :inputs {:x 8} 
                      :expected {:integer 7 :v 88.2} 
                      :config {:step-limit 1000})]
    (exercise-test-case t1
      [3.3 :x 12 :integer-add :integer-dup :push-quoterefs 7.1 :exec-y '(:v :float-save)]) =>
        {:integer '(20 20), :v '(3.3 7.1)}
    ))


(fact "L1-distance-from-top-result"
  (L1-distance-from-top-result {:y 8} {:y '(1 7 121)} :missing) => 7.0
  (L1-distance-from-top-result {:y 8} {:y '(13 7 121)} :missing) => 5.0
  (L1-distance-from-top-result {:y 8} {:y '(8 7 121)} :missing) => 0.0
  (L1-distance-from-top-result {:y 8} {:y '()} :missing) => :missing
  (L1-distance-from-top-result {:y 8} {:x '(2)} :missing) => :missing
  
  (L1-distance-from-top-result {:y false} {:y '(false)} :missing) => 0.0
  (L1-distance-from-top-result {:y false} {:y '(true)} :missing) => 1.0
  (L1-distance-from-top-result {:y false} {:y '()} :missing) => :missing
  (L1-distance-from-top-result {:y false} {:x '(3)} :missing) => :missing
  
  (L1-distance-from-top-result {:y :foo} {:y '(:foo)} :missing) => 0.0
  (L1-distance-from-top-result {:y :foo} {:y '(:bar)} :missing) => 1.0
  (L1-distance-from-top-result {:y :foo} {:y '()} :missing) => :missing
  (L1-distance-from-top-result {:y :foo} {:x '(:foo)} :missing) => :missing
  )


;; 
;; score an Answer using a single ErrorRubric:
;;   1. extract TestCase and answer
;;   2. exercise-test-case
;;   3. gather results (headless)
;;   4. apply error measure => score (error measure may be soft or harsh)
;;





;; make ErrorRubric headless!

;; 
;; score an Answer using a single StructuralRubric:
;;   1. extract TestCase and answer
;;   2. exercise-test-case
;;   3. gather results (headless)
;;   4. apply error measure => score (error measure may be soft or harsh)
;;


;; 
;; score an Answer using a single BehaviorRubric:
;;   1. extract TestCase and answer
;;   2. exercise-test-case
;;   3. gather results (each an entire history)
;;   4. apply error measure => score (error measure may be soft or harsh)
;;


;; 
;; score an Answer using a single InteractiveRubric:
;;   1. extract TestCase and two or more Answers
;;   2. exercise-test-case
;;   3. gather results
;;   4. apply error measure => scores (error measure may be soft or harsh), for all Answers
;;
