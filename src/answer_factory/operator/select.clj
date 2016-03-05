(ns answer-factory.operator.select
  (:require [answer-factory.answer.push :as answer]))

;; Notes on the fundamental structure of the data store and how it affects function calls.
;;
;; The Answers table contains information about genomes and programs only.
;; The Rubrics table contain information about running and evaluating programs only.
;; The Scores table contain all info about scores obtained when applying a Rubric to an Answer
;;
;; In many of these functions, what is passed in is either a collection of records
;; or a single record from one or more of these tables.


(defn uniform-selection
  "Returns a single element of the `answers` collection passed in, selected at random with uniform probability, disregarding the `scores` argument (which is still required)"
  [answers scores]
  (if (empty? answers)
    (throw (Exception. "uniform-selection attempted on an empty collection"))
    [(rand-nth answers)]))



(defn uniform-cull
  "Returns the `answers` collection passed in with a randomly selected item removed, disregarding the `scores` argument (which is still required)"
  [answers scores]
  (if (empty? answers)
    (throw (Exception. "uniform-cull attempted on an empty collection"))
    (let [which (rand-int (count answers))]
      (into [] (concat (take which answers) (drop (inc which) answers))))))



(defn scores-for-answer
  "Takes a collection of Score hashmaps, and a single Answer record, and returns the subset of the scores which refer to that Answer by :id"
  [scores answer]
  (let [which (:id answer)]
    (filter #(= (:answer-id %) which) scores)))



(defn scores-for-rubric
  "Takes a collection of Score hashmaps, and a single Rubric record, and returns the subset of the scores which refer to that Rubric by :id"
  [scores rubric]
  (let [which (:id rubric)]
    (filter #(= (:rubric-id %) which) scores)))



(defn appears-on-list-of-ids?
  "Takes an answer and a collection of :id values, and returns true if the answer's :id appears on the list"
  [answer list-of-ids]
  (boolean (some #{(:id answer)} list-of-ids)))



(defn numeric-only
  "Takes a table of score records, and a single Rubric record; removes all records from the collection of records where the indicated Rubric does not have numeric value. Throws an exception if the resulting collection would be empty"
  [scores rubric]
  (let [result
    (remove #( (complement number?) (:score %)) (scores-for-rubric scores rubric))]
    (if (empty? result)
      (throw
        (Exception. (str "No valid scores for rubric :id " (:id rubric))))
      result)))



(defn simple-selection
  "Takes a collection of Answer records, a collection of Scores, and a single Rubric record. Returns ALL Answers which have the lowest score on the indicated rubric."
  [answers scores rubric]
  (let [useful-scores (numeric-only scores rubric)
        min-score     (apply min (map :score useful-scores))
        best-scores   (filter #(= (:score %) min-score) useful-scores)
        winning-ids   (map :answer-id best-scores)]
    (into [] (filter #(appears-on-list-of-ids? % winning-ids) answers))))



(defn simple-cull
  "Takes a collection of Answer records, a collection of Scores, and a single Rubric record. Removes ALL Answers which have the largest score on the indicated rubric. Can produce an empty result?"
  [answers scores rubric]
  (let [useful-scores (numeric-only scores rubric)
        max-score     (apply max (map :score useful-scores))
        dead-scores   (filter #(= (:score %) max-score) useful-scores)
        losing-ids    (map :answer-id dead-scores)]
    (into [] (remove #(appears-on-list-of-ids? % losing-ids) answers))))




(defn lexicase-selection
  "Takes a collection of Answer records, a collection of Scores, and a collection of Rubric records. NOTE: returns all answers which filter through; does not sample at the end!"
  [answers scores rubrics]
  (loop [survivors answers
         criteria (shuffle rubrics)]
    (cond (empty? criteria) survivors
          (= 1 (count survivors)) survivors
          :else
            (let [criterion (first criteria)]
              (recur (simple-selection survivors scores criterion)
                     (rest criteria))))))


(defn negate-score
  "Takes a Score record, and negates the score if it's numerical. Leaves it alone otherwise."
  [s]
  (if (number? (:score s))
    (assoc s :score (- (:score s)))
    s))



(defn lexicase-cull
  "Takes a collection of Answer records, a collection of Scores, and a collection of Rubric records. Performs lexicase-selection on the _inverted_ score table: that is, it negates all numerical score values and then does lexicase-selection on that. Returns the population _lacking_ the worst-scoring individual(s). NOTES: Answers with non-numeric scores will be retained, not culled. Also, if all scores are equivalent, lexicase-cull can produce an empty result!"
  [answers scores rubrics]
  (let [backwards-scores (map negate-score scores)
        loser-ids        (map :id (lexicase-selection answers backwards-scores rubrics))]
    (remove #(some #{(:id %)} loser-ids) answers)))



(defn salient-scores
  "takes one Answer record, a collection of Scores, and a collection of Rubric records, and returns a vector of the scores associated with those Rubrics, or nil if missing."
  [answer scores rubrics]
  (let [a        (:id answer)
        which    (map :id rubrics)
        salient  (filter #(= (:answer-id %) a) scores)]
    (if (record? rubrics) ;; it's not in a collection
      (throw (Exception. "salient-scores argument is not a collection of Rubric records"))
      (reduce
        (fn [v r] (conj v (:score (first (filter #(= r (:rubric-id %)) salient)))))
        []
        which))))



(defn dominated-by?
  "Takes two Answer records, a Scores table, and a collection of Rubric record. Returns `true` if the second Answer (strictly) dominates the first on the scores specified by the rubrics. If any of the specified scores of either Answer is non-numeric (`nil` or `keyword`), it returns `false`."
  [a1 a2 scores rubrics]
  (let [scores1 (salient-scores a1 scores rubrics)
        scores2 (salient-scores a2 scores rubrics)]
    (cond
      (not-every? number? scores1) false
      (not-every? number? scores2) false
      :else (let [delta (map compare scores1 scores2)]
              (and (boolean (some pos? delta))
                   (not-any? neg? delta))))))



(defn filter-out-dominated
  "Takes a single Answer, a collection of Answer records, a Scores table, and a collection of Rubric records. Removes any Answer from the collection which is dominated by the first, based on the specified Rubrics"
  [answer answers scores rubrics]
  (remove #(dominated-by? % answer scores rubrics) answers))



(defn nondominated-selection
  "Takes a collection of answers, scores and rubrics, and removes any that are dominated by any others on the specified rubrics. NOTE: Answers with non-numeric scores (including `nil`) are NEVER removed! This can have unexpected consequences."
  [answers scores rubrics]
  (reduce
    (fn [survivors dude] (filter-out-dominated dude survivors scores rubrics))
    answers
    answers))



(defn nondomination-sort
  "Takes a collection of Answers, scores and Rubrics. Returns a vector of collections of Answers, with the first collection being the non-dominated ones, the next being the nondominated remainder, and so on until all Answers have been sorted."
  [answers scores rubrics]
  (loop [remainder answers
         layers    [] ]
    (if (empty? remainder)
      layers
      (let [best      (nondominated-selection remainder scores rubrics)
            best-ids  (map :id best)
            less-best (remove #(some #{= (:id %)} best-ids) remainder)]
        (recur less-best
               (conj layers best))))))



(defn most-dominated-cull
  "Takes collections of answers, scores, and rubrics, and removes the _most dominated_ answers in the collection, according to the specified rubrics. If no answer is dominated, they are all returned. NOTE: The algorithm applies `nondomination-sort` and removes the last subset found, unless there is only one."
  [answers scores rubrics]
  (let [sorted (nondomination-sort answers scores rubrics)]
    (if (= 1 (count sorted))
      answers
      (let [loser-ids (map :id (last sorted))]
        (remove #(some #{(:id %)} loser-ids) answers)))))



(defn remove-uncooperative
  "Takes collections of answers, scores and rubrics, and removes any answer for which any score is non-numeric for any of the specified rubrics."
  [answers scores rubrics]
  (remove 
    #(some (complement number?) (salient-scores % scores rubrics))
    answers))


(defn select-on-nil
  "Takes collections of answers, scores and rubrics, and returns only thoe answers where some score is `nil`."
  [answers scores rubrics]
  (filter 
    #(some nil? (salient-scores % scores rubrics))
    answers))

