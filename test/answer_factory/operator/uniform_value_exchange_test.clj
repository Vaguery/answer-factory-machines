(ns answer-factory.operator.uniform-value-exchange-test
  (:use midje.sweet)
  (:use answer-factory.answer.push)
  (:use answer-factory.operator.crossover)
  (:use answer-factory.util.crossover-fixtures))


;; uniform-value-exchange


(fact "uniform-value-exchange does nothing if both genomes are empty"
  (uniform-value-exchange [] [] :foo) => [[] []])



(fact "uniform-value-exchange works if one genome is empty"
  (first (uniform-value-exchange plush-1 [] :item)) => plush-1
  (provided (rand) => 0.1)

  (first (uniform-value-exchange plush-1 [] :item)) => []
  (provided (rand) => 0.9)

  (uniform-value-exchange plush-1 [] :item) =>
    [ [(nth plush-1 0) (nth plush-1 2) (nth plush-1 4)]
      [(nth plush-1 1) (nth plush-1 3)] ]
  (provided (rand) =streams=> (cycle [0 1])))



(fact "uniform-value-exchange works as expected otherwise"
  (map :item (first (uniform-value-exchange plush-1 plush-2 :item))) => [:a :b :c :d 5]
  (provided (rand) => 0.1)

  (map :item (first (uniform-value-exchange plush-1 plush-2 :item))) => [1 2 3 4]
  (provided (rand) => 0.9)

  (map :item (first (uniform-value-exchange plush-1 plush-2 :item))) => [:a 2 :c 4 5]
  (provided (rand) =streams=> (cycle [0 1]))

  (map :item (second (uniform-value-exchange plush-1 plush-2 :item))) => [1 :b 3 :d]
  (provided (rand) =streams=> (cycle [0 1]))

  (map :item (first (uniform-value-exchange bb8-1 bb8-2 :item))) => [:a 2 :c 4 :e]
  (provided (rand) =streams=> (cycle [0 1]))

  (map :item (second (uniform-value-exchange bb8-1 bb8-2 :item))) => [1 :b 3 :d 5 6]
  (provided (rand) =streams=> (cycle [0 1])))



(fact "uniform-value-exchange works on any keyword"
  (map :from (first (uniform-value-exchange bb8-1 bb8-2 :from))) =>
    [:down :up :down :up :down]
  (provided 
    (rand) =streams=> (cycle [0 1])) 

  (map :from (second (uniform-value-exchange bb8-1 bb8-2 :from))) =>
    [:up :down :up :down :up :up]
  (provided 
    (rand) =streams=> (cycle [0 1]))  
  )


;; unaligned-uniform-value-exchange


(fact "unaligned-uniform-value-exchange works as expected"
  (map :item (first (unaligned-uniform-value-exchange plush-1 plush-2 :item))) => [:b :c 2 3 5]
  (provided 
    (rand-int 10) => 2                 ;; --12345 
    (rand) =streams=> (cycle [0 1]))   ;; abcd---

  (map :item (first (unaligned-uniform-value-exchange plush-1 plush-2 :item))) => [:b :d 1 3 5]
  (provided 
    (rand-int 10) => 0                 ;; ----12345
    (rand) =streams=> (cycle [0 1]))   ;; abcd-----

  (map :item (first (unaligned-uniform-value-exchange plush-1 plush-2 :item))) => [1 :a 4 :c :d]
  (provided 
    (rand-int 10) => 6                 ;; 12345
    (rand) =streams=> (cycle [0 1]))   ;; --abcd

  (map :item (first (unaligned-uniform-value-exchange bb8-1 bb8-2 :item))) => [:b :c 2 :e 5]
  (provided 
    (rand-int 12) => 3                 ;; 123456
    (rand) =streams=> (cycle [0 1]))   ;; --abcde

  (map :item (second (unaligned-uniform-value-exchange bb8-1 bb8-2 :item))) => [:a 1 :d 3 4 6]
  (provided 
    (rand-int 12) => 3                 ;; 123456
    (rand) =streams=> (cycle [0 1]))   ;; --abcde 
  )




(fact "unaligned-uniform-value-exchange works on any keyword"
  (map :put (first (unaligned-uniform-value-exchange bb8-1 bb8-2 :put))) =>
    [:L :R :L :R :L]
  (provided 
    (rand-int 12) => 5               
    (rand) =streams=> (cycle [0 1])) 

  (map :put (second (unaligned-uniform-value-exchange bb8-1 bb8-2 :put))) =>
    [:R :L :R :L :L]
  (provided 
    (rand-int 12) => 8              
    (rand) =streams=> (cycle [0 1]))  
  )