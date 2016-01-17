(ns answer-factory.genomes.plush-test
  (:use midje.sweet)
  (:use answer-factory.genomes.plush)
  (:use clojure.pprint))


;; noop_open_paren
;; noop_delete_prev_paren_pair
;; :parentheses metadata
;; :close values
;; :close-open values
;; :close count
;; :silent metadata

(fact "translating a genome without a branching map produces a flat program"
  (plush->push [{:item 1 :close 0}
                {:item 2 :close 0}
                {:item 3 :close 0}
                {:item 4 :close 0}]) => [1 2 3 4]
  (plush->push []) => []
  )