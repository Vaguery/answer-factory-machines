(ns answer-factory.operator.erc-mutate-test
  (:use midje.sweet)
  (:use answer-factory.answer.push)
  (:use answer-factory.operator.mutate))


(fact "mutate-gene-item changes the :item field"
  (mutate-gene-item {:item 22 :close 0} 1.0 [99]) => {:item 99 :close 0}
  (mutate-gene-item {:item 22 :close 0} 1.0 [false]) => {:item false :close 0}
  (mutate-gene-item {:item 22 :close 0} 0.0 [false]) => {:item 22 :close 0})


(fact "mutate-gene-item does nothing if the list is empty"
  (mutate-gene-item {:item 22 :close 0} 1.0 []) => {:item 22 :close 0})


(fact "mutate-gene-item actually samples the list"
  (mutate-gene-item {:item 22 :close 0} 1.0 [1 2 3]) => {:item 8888 :close 0}
    (provided (rand-nth [1 2 3]) => 8888))


;; mutate-plush-close

(fact "mutate-plush-close replaces the :close field"
  (mutate-plush-close {:item 22 :close 0} 1.0 1.0 10) => {:item 22 :close 11}
  (mutate-plush-close {:item 22 :close 1} 1.0 0.0 55) => {:item 22 :close 0})


;; mutate-plush-silence

(fact "mutate-plush-silence sets or resets the :silent field"
  (mutate-plush-silence {:item 22 :close 0} 1.0 1.0) => {:item 22 :close 0 :silent true}
  (mutate-plush-silence {:item 22 :close 0} 1.0 0.0) => {:item 22 :close 0 :silent false}
  (mutate-plush-silence {:item 22 :close 0} 1.0 0.4) => {:item 22 :close 0 :silent false}
    (provided (rand) => 0.7)
  (mutate-plush-silence {:item 22 :close 1} 0.0 0.0) => {:item 22 :close 1})
