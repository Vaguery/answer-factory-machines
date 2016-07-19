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


(defn median
  "returns the median of a set of numbers; does not attempt to interpolate for even-sized collections, or check for empty collections"
  [numbers]
  (nth (sort numbers) (/ (count numbers) 2)))


(fact "median works as expected"
  (median [0 1 2 3 11 12 13 14]) => 11
  (median [0 1 2 3 11 12 13]) => 3
  (median (shuffle [0 1 2 3 11 12 13 14 15 16])) => 12
  (median (shuffle [0 1 2 3 11 12 13 14 15])) => 11
  )


(defn median-absolute-deviation
  "Takes a collection of numbers, and returns the Median Absolute Deviation. The median of the values is calculated, and the median of the absolute deviations of the values from that is returned."
  [numbers]
  (let [midpoint   (median numbers)
        deviations (map #(-' % midpoint) numbers)]
    (median (map #(Math/abs %) deviations))
    ))


(fact "median-absolute-deviation returns a value for epsilon"
  (median-absolute-deviation [0 1 2 3 11 12 13 14]) => 8
  (median-absolute-deviation [0 1 2 3 11 12 13]) => 3
  (median-absolute-deviation (shuffle [0 1 2 3 11 12 13 14 15 16])) => 4
  (median-absolute-deviation (shuffle [0 1 2 3 11 12 13 14 15])) => 4
  (median-absolute-deviation (range 10)) => 3
  (median-absolute-deviation (range 100)) => 25
  (median-absolute-deviation (range 1000)) => 250
  (median-absolute-deviation (concat (range 1000) (range 10))) => 253
  (median-absolute-deviation (concat (range 1000) (range 100))) => 275
  (median-absolute-deviation (concat (range 10) (range 10) (range 100))) => 30
  )



(defn sloppy-simple-selection
  "Takes a population of answers, an index of a particular score on which to select, and a `delta` value (an absolute range of scores). Returns all answers whose score on the indexed task is within `delta` of the best."
  [answers which-score delta]
  (let [scores (map #(get-score % which-score) answers)
        best   (apply min scores)]
    (filter
      (fn [a] (close-enough? (get-score a which-score) best delta))
      answers)
    ))


(fact "sloppy-simple-selection returns the best and maybe more"
                                                           ;; 2 3 5 1 4
  (map :genome (sloppy-simple-selection population 1 0)) => '(      4  )
  (map :genome (sloppy-simple-selection population 1 1)) => '(1     4  )
  (map :genome (sloppy-simple-selection population 1 2)) => '(1 2   4  )
  (map :genome (sloppy-simple-selection population 1 3)) => '(1 2   4 5)
  (map :genome (sloppy-simple-selection population 1 4)) => '(1 2 3 4 5)
  )


(defn mad-deltas
  "Takes a collection of answers, extracts their scores, and returns the median-absolute-deviation for each score, measured over the enture population"
  [answers]
  (let [score-count (count (:scores (first answers)))]
    (map (fn [s]
            (median-absolute-deviation 
              (map #(get-score % s) answers)))
         (range score-count))))


(fact "mad-deltas"
  (mad-deltas population)   => '(1 1 0 1 1 1 2 3)
  )



;; Lee points out that when you are doing generational selection, the median-absolute-deviation only needs to be calculated on the entire population, not in the context of each selection event. This example assumes that you have already calculated the vector of `deltas` for all answers, and passed those into the selection algorithm. If those values need more frequent calculation, for instance if you are using a steady-state or asynchronous search process, then simply recalculate the argument more often somewhere else.


(def my-deltas (mad-deltas population))


(defn shuffled-indices [n] (shuffle (range n)))


(defn epsilon-lexicase-selection
  "Takes a population of individuals (`answers`), and a collection of `deltas`, which should be calculated by some external function, and are used as absolute deviations permitted when considering almost-optimality. As a default, the `mad-deltas` function will be applied if no `deltas` collection is explicitly passed in. Returns all answers that pass the filtering rounds."
  ([answers]
    (epsilon-lexicase-selection answers (mad-deltas answers)))
  ([answers deltas]
    (let [score-count (count deltas)]
      (loop [survivors   answers
             criteria    (shuffled-indices score-count)]
        (cond (empty? criteria) survivors
              (= 1 (count survivors)) survivors
              :else
                (let [criterion (first criteria)
                      delta     (nth deltas criterion)]
                  (recur (sloppy-simple-selection survivors criterion delta)
                         (rest criteria))))))))
              



(fact "epsilon-lexicase-selection returns some answers"
  (epsilon-lexicase-selection population my-deltas) => 
      '({:genome 1, :scores [1 2 3 4 5 6 7 8]})
    (provided (shuffled-indices 8) => [0 1 2]) ;; override randomness

  (epsilon-lexicase-selection population my-deltas) => 
      '({:genome 5, :scores [4 4 4 4 4 4 4 4]})
    (provided (shuffled-indices 8) => [3 4 5]) ;; override randomness

  (epsilon-lexicase-selection population my-deltas) => 
      '({:genome 4, :scores [7 1 7 8 9 1 1 4]})
    (provided (shuffled-indices 8) => [6 7 0]) ;; override randomness

  (epsilon-lexicase-selection population my-deltas) => 
      '({:genome 2, :scores [2 3 4 5 6 7 9 1]} 
        {:genome 3, :scores [2 5 4 5 6 7 9 1]})
    (provided (shuffled-indices 8) => [7 2 3 0]) ;; override randomness
  )



(fact "epsilon-lexicase-selection accepts an explicit deltas vector"
  (epsilon-lexicase-selection population [0 0 0 0 0 0 0 0]) => 
      '({:genome 4, :scores [7 1 7 8 9 1 1 4]})
    (provided (shuffled-indices 8) => [1]) ;; override randomness
)



(fact "epsilon-lexicase-selection can be tuned via the deltas vector"
  (epsilon-lexicase-selection population (take 8 (repeat 1000))) => population
)


;;;;;;;;;;;;;;;;; probabilistic

(def population
  [ {:genome 1 :scores [1 2 3 4 5 6 7 8]}
    {:genome 2 :scores [2 3 4 5 6 7 9 1]}
    {:genome 3 :scores [2 5 4 5 6 7 9 1]}
    {:genome 4 :scores [7 1 7 8 9 1 1 4]}
    {:genome 5 :scores [4 4 4 4 4 4 4 4]}
    ])


(defn lexicase-selected?
  "Takes a collection of answers (each of which has scores), an index of an answer to use, an index of a score to use, and a single delta value. Returns `true` if that answer would be selected on that score with that delta (slack)."
  [answers answer-idx score-idx delta]
  (let [scores (map #(get-score % score-idx) answers)
        best   (apply min scores)]
    (close-enough? (nth scores answer-idx) best delta)))


(fact "lexicase-selected?"
  (lexicase-selected? population 0 0 0) => true
  (lexicase-selected? population 1 0 0) => false
  (lexicase-selected? population 2 0 1) => true
  (lexicase-selected? population 3 0 1) => false
  (lexicase-selected? population 4 0 1) => false
  (lexicase-selected? population 4 0 3) => true
  )


(defn winning-answers
  "Takes a collection of answers (each of which has scores), an index of a score to use, and a single delta value. Returns the indices of answers which will be selected by the indexed score; that is, 'bests'."
  [answers score-idx delta]
  (let [scores (map #(get-score % score-idx) answers)
        best   (apply min scores)]
    (filter 
      #(close-enough? (get-score (nth answers %) score-idx) best delta)
      (range (count answers)))))


(fact "single results from winning-answers"
  (winning-answers population 0 0) => [0]
  (winning-answers population 1 0) => [3]
  (winning-answers population 2 0) => [0]
  (winning-answers population 3 0) => [0 4]
  (winning-answers population 4 0) => [4]
  (winning-answers population 5 0) => [3]
  (winning-answers population 6 0) => [3]
  (winning-answers population 7 0) => [1 2]
  )


(fact "winning-answers with various delta values"
  (map #(winning-answers population % 0) (range 8)) => 
    '((0)     (3)     (0)       (0 4)     (4)       (3) (3) (1 2))
  (map #(winning-answers population % 1) (range 8)) => 
    '((0 1 2) (0 3)   (0 1 2 4) (0 1 2 4) (0 4)     (3) (3) (1 2))
  (map #(winning-answers population % 2) (range 8)) => 
    '((0 1 2) (0 1 3) (0 1 2 4) (0 1 2 4) (0 1 2 4) (3) (3) (1 2))
)


(defn next-winning-answers
  [answers deltas answer-indices score-indices]
  (zipmap
    score-indices
    (map
      #(winning-answers 
        (map (fn [i] (nth answers i)) answer-indices)
        %
        (nth deltas %)) score-indices)))


(def zero-deltas (take 8 (repeat 0)))
(def one-deltas (take 8 (repeat 1)))

(fact "next-winning-answers does the same as winning-answers mapped"
  (next-winning-answers population zero-deltas (range 5) (range 8)) => 
    '{0 (0), 1 (3), 2 (0), 3 (0 4), 4 (4), 5 (3), 6 (3), 7 (1 2)}
  (next-winning-answers population one-deltas (range 5) (range 8)) => 
    '{0 (0 1 2), 1 (0 3), 2 (0 1 2 4), 3 (0 1 2 4), 4 (0 4), 5 (3), 6 (3), 7 (1 2)}
  )


(fact "next-winning-answers can handle complex score index lists"
  (next-winning-answers population zero-deltas (range 5) [0]) => 
    '{0 (0)}
  (next-winning-answers population one-deltas (range 5) [0 2 4 5]) => 
    '{0 (0 1 2), 2 (0 1 2 4), 4 (0 4), 5 (3)}
    )


;; if there is only one winner, you're done. Return a hash with the winner, and prob 1
;; if there are multiple winners, but you're out of criteria, you're done. Return a hash with 1/c probabilities for each answer.
;; if there are multiple winners, but there are still criteria unexplored, dive down by removing all non-winners from the answers, and the next criterion on the list; when a hash of probabilities comes back, merge it in


(def population
  [ {:genome 1 :scores [1 2 3 4 5 6 7 8]}
    {:genome 2 :scores [2 3 4 5 6 7 9 1]}
    {:genome 3 :scores [2 5 4 5 6 7 9 1]}
    {:genome 4 :scores [7 1 7 8 9 1 1 4]}
    {:genome 5 :scores [4 4 4 4 4 4 4 4]}
    ])


;; '{0 (0), 1 (3), 2 (0), 3 (0 4), 4 (4), 5 (3), 6 (3), 7 (1 2)}

;;        0    1    2    3    4    5    6    7
;;  0    1/8       1/8   *
;;  1                                        *
;;  2                                        *
;;  3         1/8                 1/8  1/8
;;  4                    *   1/8

;; 3:           only previous-tied answers included in run-off
;;        0    1    2    3    4    5    6    7
;;  0    1/7  1/7  1/7   *
;;  4                    *   1/7  1/7  1/7  1/7


;;        0    1    2    3    4    5    6    7
;;  0    1/8       1/8  3/56
;;  1                                        *
;;  2                                        *
;;  3         1/8                 1/8  1/8
;;  4                   1/14 1/8



;; 7:             ubiquitous ties removed from score indices initially
;;        0    1    2    3    4    5    6    7
;;  1     X   1/2   X    X    X    X    X    *
;;  2     X   1/2   X    X    X    X    X    *
;;            ^^^ only no criteria remain, so even split


;;        0    1    2    3    4    5    6    7
;;  0    1/8       1/8  3/56
;;  1                                       1/16
;;  2                                       1/16
;;  3         1/8                 1/8  1/8
;;  4                   1/14 1/8


;;        0    1    2    3    4    5    6    7
;;  0    1/8       1/8  3/56                       = 17/56
;;  1                                       1/16   =  1/16
;;  2                                       1/16   =  1/16
;;  3         1/8                 1/8  1/8         =  3/8
;;  4                   1/14 1/8                   = 11/56


(def boring
  [ {:genome 1 :scores [2 2 3 4 5 6 7 8]}
    {:genome 2 :scores [3 2 3 4 5 6 7 8]}
    {:genome 3 :scores [1 2 3 4 5 6 7 8]}
    {:genome 4 :scores [1 3 3 4 5 6 7 8]}
    {:genome 5 :scores [1 2 4 4 5 6 7 8]}
    ])


(defn get-scores
  [answers score-index]
  (map #(get-score % score-index) answers))


(defn purge-constants
  "removes indexes of score columns which are constant"
  [answers score-indices]
  (remove
    #(= 1 (count (distinct (get-scores answers %)))) score-indices))


(fact 
  (purge-constants boring [0 1 2 3 4 5 6 7]) => [0 1 2]
  (purge-constants population [0 1 2 3 4 5 6 7]) => [0 1 2 3 4 5 6 7]
  (purge-constants (take 3 boring) [0 1 2 3 4 5 6 7]) => [0]
  )


(fact "I can couple next-winning-answers and purge-constants"
  (next-winning-answers
    boring 
    zero-deltas
    (range 5)
    (range 8)) => '{0 (2 3 4), 1 (0 1 2 4), 2 (0 1 2 3), 3 (0 1 2 3 4), 4 (0 1 2 3 4), 5 (0 1 2 3 4), 6 (0 1 2 3 4), 7 (0 1 2 3 4)}


  (next-winning-answers
    boring 
    zero-deltas
    (range 5)
    (purge-constants boring (range 8))) => '{0 (2 3 4), 1 (0 1 2 4), 2 (0 1 2 3)}
  )


(fact 
  (purge-constants
    (map #(nth population %) [0 4])
    '(0 1 2)) => '(0 1 2))


(defn probs
  [answers deltas answer-indices score-indices total]
  (loop [survivors answer-indices
         criteria score-indices]
      (let [competitors (map #(nth answers %) survivors)
          breakdown   (next-winning-answers
                        answers
                        deltas
                        survivors
                        (purge-constants competitors criteria))]


      (if (= 1 (count criteria))
        (zipmap survivors (repeat (* total (/ 1 (count survivors)))))
      (reduce-kv
        (fn [m k v]
          (assoc m k
            (cond (= 1 (count v))
                    (* total (/ 1 (count criteria)))
                :else
                  (probs answers deltas v (remove #{k} criteria) (/ 1 (count survivors)))
            )))
        {}
        breakdown)))
  ))

;; DON'T FORGET TO CHANGE purge-constants to take deltas into account


(fact
  (probs population zero-deltas (range 5) (range 8) 1) => 
      '{0 1/8, 1 1/8, 2 1/8, 3 (0 4), 4 1/8, 5 1/8, 6 1/8, 7 (1 2)}


)