(ns design-spikes.deterministic-epsilon-lexicase
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


(defn get-scores
  [answers idx]
  (map #(get-score % idx) answers))


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




(def population                           ;;  winner:
  [
    {:genome 1 :scores [1 2 3 4 5 6 7 8]} ;;  1 0 1 3/7 0 0 0 0 
    {:genome 2 :scores [2 3 4 5 6 7 9 1]} ;;  0 0 0  0  0 0 0 b 
    {:genome 3 :scores [2 5 4 5 6 7 9 1]} ;;  0 0 0  0  0 0 0 c 
    {:genome 4 :scores [7 1 7 8 9 1 1 4]} ;;  0 1 0  0  0 1 1 0 
    {:genome 5 :scores [4 4 4 4 4 4 4 4]} ;;  0 0 0 4/7 1 0 0 0 
    ])                                    ;;  1 1 1  1  1 1 1 1    

;; a/d
;;  {:genome 1 :scores [1 2 3 4 5 6 7 8]} ;;  1 1 1  a  0 0 0 0   a = 3/7
;;  {:genome 5 :scores [4 4 4 4 4 4 4 4]} ;;  0 0 0  d  1 1 1 1   d = 4/7
                                          ;;  1 1 1  -  1 1 1 1


;;  {:genome 2 :scores [2 3 4 5 6 7 9 1]} ;;  1 1 1  1  1 1 1 b
;;  {:genome 3 :scores [2 5 4 5 6 7 9 1]} ;;  1 0 1  1  1 1 1 c 
                                          ;;  1 1 1  1  1 1 1
                                          ;;  



(fact "I can use delta-simple-selection to count winners at one level"
  (map #(delta-simple-selection population % 0) (range 8)) =>
      '(({:genome 1, :scores [1 2 3 4 5 6 7 8]})
        ({:genome 4, :scores [7 1 7 8 9 1 1 4]})
        ({:genome 1, :scores [1 2 3 4 5 6 7 8]})
        ({:genome 1, :scores [1 2 3 4 5 6 7 8]} {:genome 5, :scores [4 4 4 4 4 4 4 4]}) 
        ({:genome 5, :scores [4 4 4 4 4 4 4 4]}) 
        ({:genome 4, :scores [7 1 7 8 9 1 1 4]}) 
        ({:genome 4, :scores [7 1 7 8 9 1 1 4]}) 
        ({:genome 2, :scores [2 3 4 5 6 7 9 1]} {:genome 3, :scores [2 5 4 5 6 7 9 1]}))

  (map #(delta-simple-selection 
          '({:genome 1, :scores [1 2 3 4 5 6 7 8]}
            {:genome 5, :scores [4 4 4 4 4 4 4 4]}) % 0) [0 1 2 4 5 6 7]) =>

      '(({:genome 1, :scores [1 2 3 4 5 6 7 8]})
        ({:genome 1, :scores [1 2 3 4 5 6 7 8]})
        ({:genome 1, :scores [1 2 3 4 5 6 7 8]})
        ({:genome 5, :scores [4 4 4 4 4 4 4 4]})
        ({:genome 5, :scores [4 4 4 4 4 4 4 4]})
        ({:genome 5, :scores [4 4 4 4 4 4 4 4]})
        ({:genome 5, :scores [4 4 4 4 4 4 4 4]}))

  (map #(delta-simple-selection 
          '({:genome 2, :scores [2 3 4 5 6 7 9 1]}
            {:genome 3, :scores [2 5 4 5 6 7 9 1]}) % 0) [0 1 2 4 5 6 7]) => 

      '(({:genome 2, :scores [2 3 4 5 6 7 9 1]} {:genome 3, :scores [2 5 4 5 6 7 9 1]})
        ({:genome 2, :scores [2 3 4 5 6 7 9 1]})
        ({:genome 2, :scores [2 3 4 5 6 7 9 1]} {:genome 3, :scores [2 5 4 5 6 7 9 1]})
        ({:genome 2, :scores [2 3 4 5 6 7 9 1]} {:genome 3, :scores [2 5 4 5 6 7 9 1]})
        ({:genome 2, :scores [2 3 4 5 6 7 9 1]} {:genome 3, :scores [2 5 4 5 6 7 9 1]})
        ({:genome 2, :scores [2 3 4 5 6 7 9 1]} {:genome 3, :scores [2 5 4 5 6 7 9 1]})
        ({:genome 2, :scores [2 3 4 5 6 7 9 1]} {:genome 3, :scores [2 5 4 5 6 7 9 1]}))

      )


(defn count-scores
  [answers score-id]
  (let [values (get-scores answers score-id)]
    (count (distinct values))))


(defn remove-ties
  "removes all score indices from score-list for which all answers have the same score"
  [answers score-list]
  (filter #(> (count-scores answers %) 1) score-list))


(fact "remove-ties"
  (remove-ties
    '({:genome 2, :scores [2 3 4 5 6 7 9 1]}
      {:genome 3, :scores [2 5 4 5 6 7 9 1]}) [0 1 2 3 4 5 6 7]) => [1]

  (remove-ties
    '({:genome 2, :scores [2 3 4 5 6 7 9 1]}
      {:genome 3, :scores [2 5 4 3 6 7 9 1]}) [0 1 2 3 4 5 6 7]) => [1 3]

  (remove-ties
    '({:genome 2, :scores [2 3 4 5 6 7 99 11]}
      {:genome 3, :scores [2 5 4 3 6 7 33 99]}) [0 1 2 3 4 5]) => [1 3]
      )

;; if there is one answer, there is one probability: [1]
;; if for every score of S scores there is a single winner, then prob for each is #wins/N
;; if for any score there are multiple winners, recurse
;; if there are multiple answers that fall through all scores, 1/m for each