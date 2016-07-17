(ns design-spikes.epsilon-lexicase
  (:use midje.sweet))


(defn close-enough?
  "Takes two numbers and a `delta`, and returns true if the numbers are within a range `delta` of one another."
  [arg1 arg2 delta]
  (<= (Math/abs (-' arg1 arg2)) delta))


(fact "close-enough? detects close numbers"
  (close-enough? 9 9 0)    => true
  (close-enough? 9 9 10)   => true
  (close-enough? 9 10 0)   => false
  (close-enough? 9 10 1)   => true
  (close-enough? 9 10 0.9) => false
  (close-enough? 9 10 1.1) => true
  (close-enough? -9 -10 2) => true)



(def population
  [ {:genome 1 :scores [1 2 3 4 5 6 7 8]}
    {:genome 2 :scores [2 3 4 5 6 7 9 1]}
    {:genome 3 :scores [2 5 4 5 6 7 9 1]}
    {:genome 4 :scores [7 1 7 8 9 1 1 4]}
    {:genome 5 :scores [4 4 4 4 4 4 4 4]}
    ])



(defn get-score
  "Takes an answer and an index, and returns the score of that answer on the indexed training case"
  [answer which-score]
  (get-in answer [:scores which-score])
  )



(fact "get-score returns the value from `:scores` at the index"
  (get-score (first population) 7) => 8
  (get-score (first population) 2) => 3
  (get-score (last population) 2) => 4
  (get-score (last population) 7) => 4
  )


(defn epsilon-to-delta
  "Takes a collection of numbers, and an `epsilon` (a proportion 0 to 1). Returns an equivalent `delta`, which is the absolute size of that percentile of the numbers."
  [numbers epsilon]
  (let [best  (apply min numbers)
        worst (apply max numbers)]
    (* epsilon (Math/abs (- worst best)))))


(fact "epsilon-to-delta returns an absolute range"
  (epsilon-to-delta [0 1 2 3 4 5 6 7 9] 0.5) => 4.5
  (epsilon-to-delta [0 1 2 3 4 5 6 7 9] 0) => 0
  (epsilon-to-delta [0 1 2 3 4 5 6 7 9] 1) => 9
  (epsilon-to-delta [0 0 0 0 1 1 1 1] 0.5) => 0.5
  (epsilon-to-delta [0 0 0 0 1 1 1 1 10] 0.2) => 2.0)



(defn delta-simple-selection
  "Takes a population of answers, an index of a particular score on which to select, and a `delta` value (an absolute range of scores). Returns all answers whose score on the indexed task is within `delta` of the best."
  [answers which-score delta]
  (let [scores (map #(get-score % which-score) answers)
        best   (apply min scores)]
    (filter
      (fn [a] (close-enough? (get-score a which-score) best delta))
      answers)
    ))


(fact "delta-simple-selection returns the best and maybe more"
                                                          ;; 2 3 5 1 4
  (map :genome (delta-simple-selection population 1 0)) => '(      4  )
  (map :genome (delta-simple-selection population 1 1)) => '(1     4  )
  (map :genome (delta-simple-selection population 1 2)) => '(1 2   4  )
  (map :genome (delta-simple-selection population 1 3)) => '(1 2   4 5)
  (map :genome (delta-simple-selection population 1 4)) => '(1 2 3 4 5)
  )



(defn epsilon-lexicase-selection
  "Takes a population of individuals (`answers`), and an `epsilon` value (between 0 and 1). Applies lexicase selection to the population, applying `epsilon-simple-selection` in each cycle. Returns all answers that pass the filtering rounds."
  [answers epsilon]
  (let [score-count (count (:scores (first answers)))]
    (loop [survivors   answers
           criteria    (shuffle (range score-count))]
      (cond (empty? criteria) survivors
            (= 1 (count survivors)) survivors
            :else
              (let [criterion (first criteria)
                    delta (epsilon-to-delta
                            (map #(get-score % criterion) answers)
                            epsilon)]
                (recur (delta-simple-selection survivors criterion delta)
                       (rest criteria)))))))
              

(fact "epsilon-lexicase-selection returns all answers when epsilon = 1"
  (epsilon-lexicase-selection population 1) => population
  )




(fact "epsilon-lexicase-selection returns only the 'best' answers when epsilon = 0"
  (epsilon-lexicase-selection population 0) =>
      '({:genome 2, :scores [2 3 4 5 6 7 9 1]} 
        {:genome 3, :scores [2 5 4 5 6 7 9 1]})
    
    (provided (shuffle (range 8)) => [7]) ;; override lexicase shuffle
)



(fact "epsilon-lexicase-selection returns successively filtered answers"
  (epsilon-lexicase-selection population 0.7) => 
      '({:genome 1, :scores [1 2 3 4 5 6 7 8]}
        {:genome 2, :scores [2 3 4 5 6 7 9 1]} 
        {:genome 4, :scores [7 1 7 8 9 1 1 4]})
    
    (provided (shuffle (range 8)) => [1]) ;; override lexicase shuffle


  (epsilon-lexicase-selection population 0.7) =>
       '({:genome 1, :scores [1 2 3 4 5 6 7 8]}
         {:genome 2, :scores [2 3 4 5 6 7 9 1]})
    (provided (shuffle (range 8)) => [1 0]) ;; override lexicase shuffle


  (epsilon-lexicase-selection population 0.7) =>
       '({:genome 2, :scores [2 3 4 5 6 7 9 1]})
    (provided (shuffle (range 8)) => [1 0 7]) ;; override lexicase shuffle
  )




(def subtle-population
  [ {:genome 1 :scores [1 2 3 4 5 6 7 8]}
    {:genome 2 :scores [2 2 3 4 5 6 7 8]}
    {:genome 3 :scores [2 3 3 4 5 6 7 8]}
    {:genome 4 :scores [2 3 3 5 5 6 7 8]}
    {:genome 5 :scores [2 3 3 5 6 6 7 8]}
    ])




(fact "additional tests just to understand it"
  (epsilon-lexicase-selection subtle-population 0.7) => 
    '({:genome 1, :scores [1 2 3 4 5 6 7 8]}
      {:genome 2, :scores [2 2 3 4 5 6 7 8]}
      {:genome 3, :scores [2 3 3 4 5 6 7 8]})

    (provided (shuffle (range 8)) => [7 6 5 4 3]) ;; override lexicase shuffle


  (epsilon-lexicase-selection subtle-population 0.7) => 
    '({:genome 1, :scores [1 2 3 4 5 6 7 8]})

    (provided (shuffle (range 8)) => [7 6 5 4 3 0]) ;; override lexicase shuffle


  (epsilon-lexicase-selection subtle-population 0.7) => 
    '({:genome 1, :scores [1 2 3 4 5 6 7 8]} 
      {:genome 2, :scores [2 2 3 4 5 6 7 8]} 
      {:genome 3, :scores [2 3 3 4 5 6 7 8]} 
      {:genome 4, :scores [2 3 3 5 5 6 7 8]})

    (provided (shuffle (range 8)) => [5 4]) ;; override lexicase shuffle
  )


(future-fact "repeated applications of epsilon-lexicase-selection provide insight into pseudo-elites"
  (frequencies
    (take 100000 (repeatedly #(epsilon-lexicase-selection population 0)))) => 
      '{({:genome 4, :scores [7 1 7 8 9 1 1 4]}) 37807  
        ({:genome 1, :scores [1 2 3 4 5 6 7 8]}) 30210 
        ({:genome 5, :scores [4 4 4 4 4 4 4 4]}) 19549  
        ({:genome 2, :scores [2 3 4 5 6 7 9 1]}) 12434}

  (frequencies
    (take 100000 (repeatedly #(epsilon-lexicase-selection population 0.25)))) => 
      '{({:genome 2, :scores [2 3 4 5 6 7 9 1]}) 37526 
        ({:genome 4, :scores [7 1 7 8 9 1 1 4]}) 30431
        ({:genome 5, :scores [4 4 4 4 4 4 4 4]}) 18334 
        ({:genome 1, :scores [1 2 3 4 5 6 7 8]}) 13709} 

  (frequencies
    (take 100000 (repeatedly #(epsilon-lexicase-selection population 0.5)))) => 
      '{({:genome 5, :scores [4 4 4 4 4 4 4 4]}) 78524 
        ({:genome 4, :scores [7 1 7 8 9 1 1 4]}) 11976 
        ({:genome 2, :scores [2 3 4 5 6 7 9 1]}) 9500}

  (frequencies
    (take 100000 (repeatedly #(epsilon-lexicase-selection population 0.75)))) => 
      '{
        ({:genome 2, :scores [2 3 4 5 6 7 9 1]}
         {:genome 3, :scores [2 5 4 5 6 7 9 1]}
         {:genome 5, :scores [4 4 4 4 4 4 4 4]}) 56464 
        ({:genome 5, :scores [4 4 4 4 4 4 4 4]}) 22903
        ({:genome 1, :scores [1 2 3 4 5 6 7 8]}
         {:genome 5, :scores [4 4 4 4 4 4 4 4]}) 11225
        ({:genome 2, :scores [2 3 4 5 6 7 9 1]} 
         {:genome 5, :scores [4 4 4 4 4 4 4 4]}) 9408}
        )

