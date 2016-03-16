(ns answer-factory.rubric.rubric-test
  (:use midje.sweet)
  (:use answer-factory.rubric.push)
  (:require  [answer-factory.answer.push :as a])
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
  (L1-distance-from-top-result {:y 8} {:y '(1 7 121)} :missing) => {:y 7.0}
  (L1-distance-from-top-result {:y 8} {:y '(13 7 121)} :missing) => {:y 5.0}
  (L1-distance-from-top-result {:y 8} {:y '(8 7 121)} :missing) => {:y 0.0}
  (L1-distance-from-top-result {:y 8} {:y '()} :missing) => {:y :missing}
  (L1-distance-from-top-result {:y 8} {:y '()} 100000000.0) => {:y 100000000.0}
  (L1-distance-from-top-result {:y 8} {:x '(2)} :missing) => {:y :missing}
  
  (L1-distance-from-top-result {:y false} {:y '(false)} :missing) => {:y 0.0}
  (L1-distance-from-top-result {:y false} {:y '(true)} :missing) => {:y 1.0}
  (L1-distance-from-top-result {:y false} {:y '()} :missing) => {:y :missing}
  (L1-distance-from-top-result {:y false} {:x '(3)} :missing) => {:y :missing}
  
  (L1-distance-from-top-result {:y :foo} {:y '(:foo)} :missing) => {:y 0.0}
  (L1-distance-from-top-result {:y :foo} {:y '(:bar)} :missing) => {:y 1.0}
  (L1-distance-from-top-result {:y :foo} {:y '()} :missing) => {:y :missing}
  (L1-distance-from-top-result {:y :foo} {:x '(:foo)} :missing) => {:y :missing}
  )


(future-fact "L1-distance-from-top-result handling multiple items"

  (L1-distance-from-top-result {:z1 [88 99]} {:z1 '([62 7])} :missing) => {:z1 1.0}

  (L1-distance-from-top-result {:z1 1 :z2 2} {:z1 '(7) :z2 '(8)} :missing) => {:z1 6 :z2 6}

)


;; 
;; score an Answer using a single ErrorRubric:
;;   1. extract TestCase and answer
;;   2. exercise-test-case
;;   3. gather results (headless)
;;   4. apply error measure => score (error measure may be soft or harsh)
;;


;; fixtures

    ; (integer? m)   (jump-to z m)
    ; (= m :head)    (rewind z)
    ; (= m :tail)    (fast-forward z)
    ; (= m :subhead) (goto-leftmost z)
    ; (= m :append)  (goto-rightmost z)
    ; (= m :left)    (wrap-left z)
    ; (= m :right)   (wrap-right z)
    ; (= m :prev)    (wrap-prev z)
    ; (= m :next)    (wrap-next z)
    ; (= m :up)      (step-up z)
    ; (= m :down)    (step-down z)

(def a1 (a/make-pushanswer [{:from :left :put :R :item 1}
                            {:from :up   :put :L :item :x}
                            {:from :left :put :R :item 2}
                            {:from :prev :put :L :item :integer-add}
                            {:from :tail :put :R :item 4}
                            {:from 81    :put :R :item :integer-subtract}
                            {:from :head :put :R :item :integer-dup}] :bb8))


(def a2 (a/make-pushanswer [{:from :left :put :R :item false}] :bb8))


(fact "score-answer returns an answer's score"
  (let [tc (test-case :inputs {:x 8}
                      :expected {:integer 7} 
                      :config {:step-limit 100})
        r  (error-rubric :testcase tc :score-fn L1-distance-from-top-result)]
    
    (:program a1) => [:x :integer-dup 2 :integer-subtract :integer-add 1 4]

    (score-answer a1 r :missing) => {:integer 3.0}
    (score-answer a2 r :missing) => {:integer :missing}))


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
