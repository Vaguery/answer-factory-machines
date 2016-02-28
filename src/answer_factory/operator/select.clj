(ns answer-factory.operator.select
  (:require [answer-factory.answer.push :as answer]))


;; Notes on the fundamental structure of the data store and how it affects function calls.
;;
;; The Answers table contains information about genomes and programs only.
;; The Rubrics table contain information about running and evaluating programs only.
;; The Scores table contain all info about scores obtained when applying a Rubric to an Answer
;;
;; Thus, for consistency all selection operators are built to take TWO arguments: a collection of Answer records, and a collection of Score records.


(defn uniform-selection
  "Returns a single element of the `answers` collection passed in, selected at random with uniform probability, disregarding the `scores` argument (which is still required)"
  [answers scores]
  [(rand-nth answers)])



(defn purge-nils
  "purge-nils removes all answers in the collection where any of the explicitly listed scores has a nil value"
  [answers rubrics]
    (filter
      #(not-any? nil? (map (:scores %) (set rubrics)))
      answers))


; (defn filter-by-rubric
;   "takes a collection of answers (which contain a map called :scores), and a keyword which is the name of one of those scores; returns the reduced collection after removing any with nil scores for that rubric only!"
;   [answers rubric]
;   (let [get-rubric #(answer/get-score % rubric)]
;     (->> (purge-nils answers [rubric])
;          (sort-by get-rubric)
;          (partition-by get-rubric)
;          first)))


; (defn lexicase-selection
;   "takes a collection of answers (which each contain a map called :scores), and a collection of keywords which name the particular scores to use in selection; returns one answer"
;   [answers rubrics]
;   (rand-nth
;     (reduce 
;       filter-by-rubric 
;       (purge-nils answers rubrics)
;       (shuffle rubrics))))



;; multiobjective selection


(defn every-rubric
  "returns a set of rubric keywords, which is the union of all the :score keys in every answer in the argument collection"
  [answers]
  (reduce
    #(into %1 (keys (:scores %2)))
    #{}
    answers))


(defn dominated-by?
  "returns true if the second (answer) argument dominates the first; if a collection of rubrics is specified, that is used as the basis of comparison; otherwise, the union of the :scores keys of both answers are used; if any scores in either answer are nil, it returns false"
  [a1 a2 & [rubrics]]
  (let [k     (if rubrics
                (seq rubrics)
                (set (concat (keys (:scores a1)) (keys (:scores a2)))))
        s1    (map (:scores a1) k)
        s2    (map (:scores a2) k)
        delta (map compare s1 s2)]
    (and (not-any? nil? s1)
         (not-any? nil? s2)
         (and
           (boolean (some pos? delta))
           (not-any? neg? delta)))))


(defn remove-dominated
  "takes an answer and a collection of answers, and removes from the latter all answers dominated by the first argument; if an optional rubrics collection is passed in, that is used for the basis of comparison"
  [answer answers & [rubrics]]
  (remove #(dominated-by? % answer rubrics) answers))


(defn nondominated
  "takes a collection of answers; removes any that are dominated by any others; if _any_ answer in the entire collection has an extra score, or lacks a score the others have (or it has a nil value), then _all_ the answers are returned"
  [answers & [rubrics]]
  (let [universe    (every-rubric answers)
        consistent? (every? #(= (set (keys (:scores %))) universe) answers)
        covered?    (every? #(not-any? nil? (vals (:scores %))) answers)]
    (if (and consistent? covered?)
      (reduce
        (fn [keep check] (remove-dominated check keep rubrics))
        answers
        answers)
      answers)))


;; filtering a single VECTOR rubric

