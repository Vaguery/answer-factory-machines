(ns design-spikes.deterministic-epsilon-lexicase
  (:require [com.climate.claypoole :as cp])
  (:use midje.sweet))


;;; some fixtures

(def population
  [ {:genome 0 :scores [1 2 3 4 5 6 7 8]}
    {:genome 1 :scores [2 3 4 5 6 7 9 1]}
    {:genome 2 :scores [2 5 4 5 6 7 9 1]}
    {:genome 3 :scores [7 1 7 8 9 1 1 4]}
    {:genome 4 :scores [4 4 4 4 4 4 4 4]}
    ])


(def boring
  [ {:genome 1 :scores [2 2 3 4 5 6 7 8]}
    {:genome 2 :scores [3 2 3 4 5 6 7 8]}
    {:genome 3 :scores [1 2 3 4 5 6 7 8]}
    {:genome 4 :scores [1 3 3 4 5 6 7 8]}
    {:genome 5 :scores [1 2 4 4 5 6 7 8]}
    ])


(def simple
  [ {:genome 1 :scores [1 2 3]}
    {:genome 2 :scores [2 3 1]}
    {:genome 3 :scores [3 1 2]}
    ])

(def identical
  [ {:genome 1 :scores [1 1 1]}
    {:genome 2 :scores [1 1 1]}
    {:genome 3 :scores [1 1 1]}
    ])

;;; functions

(defn close-enough?
  "Takes two numbers and a `delta`, and returns true if the numbers are within a range `delta` of one another."
  [arg1 arg2 delta]
  (<= (Math/abs (-' arg1 arg2)) delta))


(defn get-score
  "Takes an answer and an index, and returns the score of that answer on the indexed training case"
  [answer which-score]
  (get-in answer [:scores which-score]))


(fact "get-score returns the value from `:scores` at the index"
  :prototype
  (get-score (first population) 7) => 8
  (get-score (first population) 2) => 3
  (get-score (last population) 2) => 4
  (get-score (last population) 7) => 4)


(defn winning-answers
  "Takes a collection of answers (each of which has scores), an index of a score to use, and a single delta value. Returns the answers which will be selected by the indexed score; that is, 'bests'."
  [answers score-idx delta]
  (let [scores (map #(get-score % score-idx) answers)
        best   (apply min scores)]
    (filter
      #(close-enough? (get-score % score-idx) best delta)
      answers)))


(fact "single results from winning-answers"
  :prototype
  (map :genome (winning-answers population 0 0)) => [0]
  (map :genome (winning-answers population 1 0)) => [3]
  (map :genome (winning-answers population 2 0)) => [0]
  (map :genome (winning-answers population 3 0)) => [0 4]
  (map :genome (winning-answers population 4 0)) => [4]
  (map :genome (winning-answers population 5 0)) => [3]
  (map :genome (winning-answers population 6 0)) => [3]
  (map :genome (winning-answers population 7 0)) => [1 2])


(fact "winning-answers works with various delta values"
  :prototype
  (map #(map :genome (winning-answers population % 0)) (range 8)) =>
    '((0)     (3)     (0)       (0 4)     (4)       (3) (3) (1 2))
  (map #(map :genome (winning-answers population % 1)) (range 8)) =>
    '((0 1 2) (0 3)   (0 1 2 4) (0 1 2 4) (0 4)     (3) (3) (1 2))
  (map #(map :genome (winning-answers population % 2)) (range 8)) =>
    '((0 1 2) (0 1 3) (0 1 2 4) (0 1 2 4) (0 1 2 4) (3) (3) (1 2)))


(fact "winning-answers works with a tie"
  :prototype
  (winning-answers identical 0 0) => identical)


(defn next-winning-answers
  [answers deltas answer-indices score-indices]
  (let [which-answers (map #(nth answers %) answer-indices)]
    (map
      #(winning-answers
        which-answers
        %
        (nth deltas %)) score-indices)))


(defn subset
  [answers indices]
  (map #(nth answers %) indices))


(fact "next-winning-answers does the same as winning-answers mapped"
  :prototype
  (next-winning-answers population (repeat 0) (range 5) (range 8)) =>
     [ [(nth population 0)],
       [(nth population 3)],
       [(nth population 0)],
       [(nth population 0) (nth population 4)],
       [(nth population 4)],
       [(nth population 3)],
       [(nth population 3)],
       [(nth population 1) (nth population 2)] ]
  (next-winning-answers population (repeat 1) (range 5) (range 8)) =>
     [ (subset population [0 1 2]),
       (subset population [0 3]),
       (subset population [0 1 2 4]),
       (subset population [0 1 2 4]),
       (subset population [0 4]),
       (subset population [3]),
       (subset population [3]),
       (subset population [1 2]) ]
  )


(fact "next-winning-answers can handle complex score index lists"
  :prototype
  (next-winning-answers population (repeat 0) (range 5) [0]) =>
    [ (subset population [0]) ]
  (next-winning-answers population (repeat 1) (range 5) [0 2 4 5]) =>
    [ (subset population [0 1 2]),
      (subset population [0 1 2 4]),
      (subset population [0 4]),
      (subset population [3]) ]
    )

(fact "next-winning-answers handles an odd edge case"
  :prototype
  (next-winning-answers simple [2 2 2] (range 3) (range 3)) => [simple simple simple]
  (next-winning-answers simple [1 1 1] (range 3) (range 3)) =>
    [ (subset simple [0 1]),
      (subset simple [0 2]),
      (subset simple [1 2])]
  )


(defn get-scores
  [answers score-index]
  (map #(get-score % score-index) answers))


(defn all-nearly-best?
  "Takes a collection of numbers, and returns `true` if all the values are within `delta` of the smallest value"
  [numbers delta]
  (let [best (apply min numbers)]
    (every? #(close-enough? best % delta) numbers)))


(fact "all-nearly-best?"
  :prototype
  (all-nearly-best? [0 1 2 3] 0) => false
  (all-nearly-best? [0 1 2 3] 3) => true)


(defn purge-ties
  "removes indices of score columns which are constant; accepts an optional `deltas` collection, which is used to specify the acceptable range for each score, positionally, which counts as 'tied'"
  [answers score-indices & {:keys [deltas]
                            :or {deltas (repeat 0)}}]
  (remove
    #(all-nearly-best?
      (get-scores answers %)
      (nth deltas %))
    score-indices))


(fact
  :prototype
  (purge-ties boring [0 1 2 3 4 5 6 7]) => [0 1 2]
  (purge-ties simple [0 1 2]) => [0 1 2]
  (purge-ties population [0 1 2 3 4 5 6 7]) => [0 1 2 3 4 5 6 7]
  (purge-ties (take 3 boring) [0 1 2 3 4 5 6 7]) => [0]
  )


(fact "purge-ties works when none are left"
  :prototype
  (purge-ties identical [0 1 2]) => []
  )


(fact "purge-ties does not loop forever (bug fix)"
  :prototype
  (purge-ties simple [1 2] :deltas [1 1 1]) => [1 2]
  (purge-ties simple [2] :deltas [1 1 1]) => [2]
  (purge-ties simple [0 1 2] :deltas [2 2 2]) => []
  (purge-ties simple [0 1 2] :deltas [1 1 1]) => [0 1 2]
  )


(fact "I can couple next-winning-answers and purge-ties"
  :prototype
  (next-winning-answers
    boring
    (repeat 0)
    (range 5)
    (range 8)) =>
      [ (subset boring [2 3 4]),
        (subset boring [0 1 2 4]),
        (subset boring [0 1 2 3]),
        (subset boring [0 1 2 3 4]),
        (subset boring [0 1 2 3 4]),
        (subset boring [0 1 2 3 4]),
        (subset boring [0 1 2 3 4]),
        (subset boring [0 1 2 3 4]) ]

  (next-winning-answers
    boring
    (repeat 0)
    (range 5)
    (purge-ties boring (range 8))) =>
      [ (subset boring [2 3 4]),
        (subset boring [0 1 2 4]),
        (subset boring [0 1 2 3]) ]
        )


(defn collapse-probabilities
  "Takes a hash with (nominally) probabilities in the values; merges this into another similar hash by adding the probabilities in, if they exist"
  [parent new-probs]
    (merge-with + parent new-probs))


(fact
  :prototype
  (collapse-probabilities {} {1 1/2 2 2/3}) => {1 1/2, 2 2/3}
  (collapse-probabilities {1 1/2 2 2/3} {1 1/12 2 2/13}) => {1 7/12, 2 32/39}
  (collapse-probabilities {:a 1/2 :b 2/3} {:a 1/12 :b 2/13}) => {:a 7/12, :b 32/39}
  )


(defn probs
  "Calculate the absolute probabilities of selecting each individual from a collection, where it is assumed each individual contains a `vector` (as such!) called `:scores` which contains all the values measured. A collection of `deltas` can be passed in, one for each `:score` value, matching the scores positionally to indicate the range of numerical values _for that score_ which are to be treated as 'good enough'. A `total` value can also be passed in, representing the total probability being shared among the answers (useful for recursion, or testing). A collection of indices of scores can be passed in as `criteria`, which specifies (by index) which score columns to use while ignoring others. It is assumed that every individual's `:scores` vector has the same number of items."
  [survivors &
    {:keys [deltas
            total
            criteria]
     :or   {deltas   (repeat 0)
            total    1
            criteria (range (count (:scores (first survivors))))}}]
    (let [useful-indices (purge-ties survivors criteria)
          breakdown      (next-winning-answers
                            survivors
                            deltas
                            (range (count survivors))
                            useful-indices)
          active-criteria (count useful-indices)
          nobody (zipmap survivors (repeat 0))]

      (if (empty? useful-indices)
        (zipmap survivors (repeat (/ total (count survivors))))
        (reduce
          collapse-probabilities
          (pmap
            (fn [idx winners]
              (collapse-probabilities
                nobody
                (if (< active-criteria 2)
                  (zipmap winners (repeat (/ total (count winners))))
                  (let [drop-one (nth useful-indices idx)]
                    (probs winners
                           :deltas deltas
                           :total (/ total active-criteria)
                           :criteria (remove #{drop-one} useful-indices)
                           )))))
            (iterate inc 0)
            breakdown
            )))))


(fact "probabilities of selection can be calculated which match hand-calculation, and which include all genomes, and which sum to 1N"
  :prototype
  (probs simple) =>
    '{{:genome 1, :scores [1 2 3]} 1/3,
      {:genome 2, :scores [2 3 1]} 1/3,
      {:genome 3, :scores [3 1 2]} 1/3}
  (apply + (vals (probs simple))) => 1

  (probs simple :criteria [0]) =>
    '{{:genome 1, :scores [1 2 3]} 1,
      {:genome 2, :scores [2 3 1]} 0,
      {:genome 3, :scores [3 1 2]} 0}
  (apply + (vals (probs simple :criteria [0]))) => 1


  (probs population) =>
    '{{:genome 0, :scores [1 2 3 4 5 6 7 8]} 17/56,
      {:genome 1, :scores [2 3 4 5 6 7 9 1]} 1/8,
      {:genome 2, :scores [2 5 4 5 6 7 9 1]} 0,
      {:genome 3, :scores [7 1 7 8 9 1 1 4]} 3/8,
      {:genome 4, :scores [4 4 4 4 4 4 4 4]} 11/56}

  (apply + (vals (probs population))) => 1


  (probs boring) =>
    '{{:genome 1, :scores [2 2 3 4 5 6 7 8]} 0,
      {:genome 2, :scores [3 2 3 4 5 6 7 8]} 0,
      {:genome 3, :scores [1 2 3 4 5 6 7 8]} 1,
      {:genome 4, :scores [1 3 3 4 5 6 7 8]} 0,
      {:genome 5, :scores [1 2 4 4 5 6 7 8]} 0}

  (apply + (vals (probs boring))) => 1


  (probs identical) =>
    '{{:genome 1, :scores [1 1 1]} 1/3,
      {:genome 2, :scores [1 1 1]} 1/3,
      {:genome 3, :scores [1 1 1]} 1/3}

  (apply + (vals (probs identical))) => 1)




(fact "probabilities of selection take into account `deltas`"
  :prototype
  (probs simple :deltas [0 0 1]) =>
    '{{:genome 1, :scores [1 2 3]} 1/3,
      {:genome 2, :scores [2 3 1]} 1/6,
      {:genome 3, :scores [3 1 2]} 1/2}

  (apply + (vals (probs simple :deltas [0 0 1]))) => 1


  (probs simple :deltas [1 0 1]) =>
    '{{:genome 1, :scores [1 2 3]} 1/6,
      {:genome 2, :scores [2 3 1]} 1/6,
      {:genome 3, :scores [3 1 2]} 2/3}

  (apply + (vals (probs simple :deltas [1 0 1]))) => 1


  (probs simple :deltas [0 0 0]) =>
    '{{:genome 1, :scores [1 2 3]} 1/3,
      {:genome 2, :scores [2 3 1]} 1/3,
      {:genome 3, :scores [3 1 2]} 1/3}

  (apply + (vals (probs simple :deltas [1 0 1]))) => 1


  (def sloppy  ;; like simple with deltas = [1 1 1]
    [ {:genome 1 :scores [1 1 3]}
      {:genome 2 :scores [1 3 1]}
      {:genome 3 :scores [3 1 1]}
      ])


  (probs sloppy :deltas [0 0 0]) =>
    '{{:genome 1, :scores [1 1 3]} 1/3,
      {:genome 2, :scores [1 3 1]} 1/3,
      {:genome 3, :scores [3 1 1]} 1/3}

  (apply + (vals (probs sloppy :deltas [0 0 0]))) => 1


  (probs simple :deltas [1 1 1]) =>
    '{{:genome 1, :scores [1 2 3]} 1/3,
      {:genome 2, :scores [2 3 1]} 1/3,
      {:genome 3, :scores [3 1 2]} 1/3}

  (apply + (vals (probs simple :deltas [1 1 1]))) => 1


  (probs simple :deltas [2 2 2]) =>
    '{{:genome 1, :scores [1 2 3]} 1/3,
      {:genome 2, :scores [2 3 1]} 1/3,
      {:genome 3, :scores [3 1 2]} 1/3}

  (apply + (vals (probs simple :deltas [1 1 1]))) => 1
)



(fact "I can pass in a bigdec probability and expect it to work, at least in a with-precision block"
  :prototype
  (with-precision 40 (probs identical :total 1M)) =>
    '{{:genome 1, :scores [1 1 1]} 0.3333333333333333333333333333333333333333M,
      {:genome 2, :scores [1 1 1]} 0.3333333333333333333333333333333333333333M,
      {:genome 3, :scores [1 1 1]} 0.3333333333333333333333333333333333333333M}
  )


;; exploring a bit

(defn random-fake-answer
  [genome numscores]
  {:genome genome :scores (into [] (take numscores (repeatedly #(rand-int 10))))})


; (fact "random-fake-answer"
;   (count (:scores (random-fake-answer 99 100))) => 100)


(def big-scores-100
  (map #(random-fake-answer % 10) (range 10)))


(future-fact "probs works for large populations without breaking; activate this test (change 'future-fact' to 'fact') to see it work VERY VERY SLOWLY WITH THESE SETTINGS"
  :prototype
  (let [results (time (probs big-scores-100))]
    (println (frequencies (vals results)) "\n\n")
    (println (last (sort-by val results)))

    (sort (vals results)) => 99
    (count (remove #{0} (vals results))) => 99
  ))


;; 0 0 3 3 3 3 3
;; 0 1 9 9 9 9 9
;; 1 0 9 9 9 9 9
;; 4 4 4 4 4 4 4

;; timing on my awful old laptop
;; big-scores-100
;;  map:    13403.839978 msecs
;;          15005.687512 msecs
;;          11222.424646 msecs
;;          12155.441968 msecs
;;          10785.327794 msecs
;; pmap:    7363.094657 msecs
;;          7672.071258 msecs
;;          7155.843618 msecs
;;          12185.153107 msecs
;;          7224.689491 msecs
;; cp/pmap: 7867.190574 msecs
;;          7775.838008 msecs
;;          6930.66503 msecs
;;          10120.213949 msecs
;;          6112.976086 msecs
;;
;; big-scores-1000
;; cp/pmap: 79262.746531 msecs (2)
;;          85473.563576 msecs (4)
;;          85528.799731 msecs
;;          80168.188144 msecs
;;          90123.921085 msecs
