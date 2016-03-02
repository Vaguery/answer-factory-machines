(ns answer-factory.operator.uneven-crossover-test
  (:use midje.sweet)
  (:use answer-factory.answer.push)
  (:use answer-factory.operator.crossover)
  (:use answer-factory.util.crossover-fixtures))


;; one-point-uneven-crossover


(fact "one-point-uneven-crossover does nothing if both genomes are empty"
  (one-point-uneven-crossover [] []) => [[] []])


(fact "one-point-uneven-crossover works if one genome is empty"
  (one-point-uneven-crossover plush-1 []) =>
    [ (into [] (take 2 plush-1))
      (into [] (drop 2 plush-1))]
  (provided (rand-int 6) => 2
            (rand-int 1) => 0))


(fact "one-point-uneven-crossover works as one might expect otherwise"
  (first (one-point-uneven-crossover plush-1 plush-2)) =>
     [ {:item 1 :close 0}
       {:item 2 :close 0}
       {:item 3 :close 0}
       {:item :b :close 0}
       {:item :c :close 0}
       {:item :d :close 0}]
  (provided (rand-int 6) => 3
            (rand-int 5) => 1))


(fact "one-point-uneven-crossover can 'pick' end points"
  (first (one-point-uneven-crossover plush-1 plush-2)) =>
     [ {:item 1 :close 0}
       {:item 2 :close 0}
       {:item 3 :close 0}
       {:item 4 :close 0}
       {:item 5 :close 0}
       {:item :a :close 0}
       {:item :b :close 0}
       {:item :c :close 0}
       {:item :d :close 0}]
  (provided (rand-int 6) => 5
            (rand-int 5) => 0))


(fact "one-point-uneven-crossover consumes all genes"
  (first (one-point-uneven-crossover plush-1 plush-2)) =>
     []
  (provided (rand-int 6) => 0
            (rand-int 5) => 4))



