(ns answer-factory.selection.core
  (:require [answer-factory.answer.push :as answer]))



(defn uniform-selection
  "return a single element of the collection passed in, selected at random with uniform probability"
  [things]
  (rand-nth things))



(defn purge-nils
  "purge-nils removes all answers in the collection where any of the explicitly listed scores has a nil value"
  [answers rubrics]
    (filter
      #(not-any? nil? (map (:scores %) (set rubrics)))
      answers))


(defn filter-by-rubric
  "takes a collection of answers (which contain a map called :scores), and a keyword which is the name of one of those scores; returns the reduced collection after removing any with nil scores for that rubric only!"
  [answers rubric]
  (let [get-rubric #(answer/get-score % rubric)]
    (->> (purge-nils answers [rubric])
         (sort-by get-rubric)
         (partition-by get-rubric)
         first)))


(defn lexicase-selection
  "takes a collection of answers (which each contain a map called :scores), and a collection of keywords which name the particular scores to use in selection; returns one answer"
  [answers rubrics]
  (rand-nth
    (reduce 
      filter-by-rubric 
      (purge-nils answers rubrics)
      (shuffle rubrics))))


;; multiobjective selection


(defn dominates?
  "returns true if the second (answer) argument dominates the first"
  [a1 a2]
  (let [k     (keys (:scores a1))
        s1    (map (:scores a1) k)
        s2    (map (:scores a2) k)
        delta (mapv compare s1 s2)]
    (and (not-any? neg? delta) (boolean (some pos? delta)))))

