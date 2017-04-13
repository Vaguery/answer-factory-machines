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
