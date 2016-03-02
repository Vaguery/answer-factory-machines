(ns answer-factory.operator.uniform-crossover-test
  (:use midje.sweet)
  (:use answer-factory.answer.push)
  (:use answer-factory.operator.crossover)
  (:use answer-factory.util.crossover-fixtures))


;; uniform-crossover

(fact "uniform-crossover does nothing if both genomes are empty"
  (uniform-crossover [] []) => [[] []])



(fact "uniform-crossover works if one genome is empty"
  (first (uniform-crossover plush-1 [])) => plush-1
  (provided (rand) => 0.1 :times 6)

  (first (uniform-crossover plush-1 [])) => []
  (provided (rand) => 0.9 :times 6)

  (uniform-crossover plush-1 []) =>
    [ [(nth plush-1 0) (nth plush-1 2) (nth plush-1 4)]
      [(nth plush-1 1) (nth plush-1 3)] ]
  (provided (rand) =streams=> (cycle [0 1])))



(fact "uniform-crossover works as expected otherwise"
  (first (uniform-crossover plush-1 plush-2)) => plush-1
  (provided (rand) => 0.1 :times 6)

  (first (uniform-crossover plush-1 plush-2)) => plush-2
  (provided (rand) => 0.9 :times 6)

  (map :item (first (uniform-crossover plush-1 plush-2))) =>
    [1 :b 3 :d 5]
  (provided (rand) =streams=> (cycle [0 1]))

  (map :item (second (uniform-crossover plush-1 plush-2))) =>
    [:a 2 :c 4]
  (provided (rand) =streams=> (cycle [0 1])))



;; pad-with-nil


(fact "pad-with-nil works as expected"
  (pad-with-nil [1 2 3] 3) => [nil nil nil 1 2 3]
  (pad-with-nil [1 2 3] 0) => [1 2 3])


;; unaligned-uniform-crossover


(fact "unaligned-uniform-crossover works as expected"
  (map :item (first (unaligned-uniform-crossover plush-1 plush-2))) => [:b 1 :d 3 5]
  (provided 
    (rand-int 10) => 2                 ;; --12345
    (rand) =streams=> (cycle [0 1]))   ;; abcd---

  (map :item (first (unaligned-uniform-crossover plush-1 plush-2))) => [:b :d 1 3 5]
  (provided 
    (rand-int 10) => 0                 ;; ----12345
    (rand) =streams=> (cycle [0 1]))   ;; abcd-----

  (map :item (first (unaligned-uniform-crossover plush-1 plush-2))) => [1 3 :b 5 :d]
  (provided 
    (rand-int 10) => 6                 ;; 12345
    (rand) =streams=> (cycle [0 1]))   ;; --abcd

  (map :item (first (unaligned-uniform-crossover plush-1 plush-2))) => [1 3 5 :a :c]
  (provided 
    (rand-int 10) => 9                 ;; 12345
    (rand) =streams=> (cycle [0 1]))   ;; -----abcd

  )