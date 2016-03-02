(ns answer-factory.operator.one-point-crossover-test
  (:use midje.sweet)
  (:use answer-factory.answer.push)
  (:use answer-factory.operator.crossover)
  (:use answer-factory.util.crossover-fixtures))


;; one-point-crossover


(fact "one-point-crossover does nothing if both genomes are empty"
  (one-point-crossover [] []) => [[] []])


(fact "one-point-crossover works if one genome is empty"
  (one-point-crossover plush-1 []) =>
    [ (into [] (take 2 plush-1))
      (into [] (drop 2 plush-1))]
  (provided (rand-int 6) => 2
            (rand-int 1) => 0))


(fact "one-point-crossover works as one might expect otherwise"
  (first (one-point-crossover plush-1 plush-2)) =>
     [ {:item 1 :close 0}
       {:item 2 :close 0}
       {:item 3 :close 0}
       {:item :b :close 0}
       {:item :c :close 0}
       {:item :d :close 0}]
  (provided (rand-int 6) => 3
            (rand-int 5) => 1))


(fact "one-point-crossover can 'pick' end points"
  (first (one-point-crossover plush-1 plush-2)) =>
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


(fact "one-point-crossover consumes all genes"
  (first (one-point-crossover plush-1 plush-2)) =>
     []
  (provided (rand-int 6) => 0
            (rand-int 5) => 4))



;; unaligned-one-point-crossover


(fact "unaligned-one-point-crossover works as expected"
  (map :item (first (unaligned-one-point-crossover plush-1 plush-2))) => [:a :b :c :d]
  (provided 
    (rand-int anything) =streams=> [0 , 0 0])
      ;; X----12345
      ;; Xabcd-----

  (map (partial map :item) (unaligned-one-point-crossover plush-1 plush-2)) =>
    [[         :d]
     [:a :b :c    1 2 3 4 5] ]
  (provided 
    (rand-int anything) =streams=> [0 , 3 3])
      ;; X----12345
      ;; Xabcd-----

  (map (partial map :item) (unaligned-one-point-crossover plush-1 plush-2)) =>
    [[1 2 3 :b :c :d]
     [:a 4 5] ]
  (provided 
    (rand-int anything) =streams=> [6 , 3 3])
      ;; 123 x 45
      ;;   a x bcd

  (map (partial map :item) (unaligned-one-point-crossover plush-1 plush-2)) =>
    [[1 2 3 :a :b :c :d]
     [4 5] ]
  (provided 
    (rand-int anything) =streams=> [10 , 3 3])
      ;; 123 x 45
      ;;     x   abcd

  (map (partial map :item) (unaligned-one-point-crossover plush-1 plush-2)) =>
    [[1 :b :c :d]
     [:a 2 3 4 5] ]
  (provided 
    (rand-int anything) =streams=> [7 , 1 4])
      ;;    1 x 2345
      ;;    a x bcd

  (map (partial map :item) (unaligned-one-point-crossover plush-1 plush-2)) =>
    [[:b :c :d]
     [:a 1 2 3 4 5] ]
  (provided 
    (rand-int anything) =streams=> [0 , 3 1])
      ;;       x  12345
      ;;     a x bcd 

  (map (partial map :item) (unaligned-one-point-crossover [] [])) =>
    [[] [] ]
  (provided 
    (rand-int anything) =streams=> [0 0 0]))
