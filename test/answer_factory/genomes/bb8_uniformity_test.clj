(ns answer-factory.genomes.bb8-uniformity-test
  (:use midje.sweet)
  (:require [clojure.zip :as zip])
  (:use [answer-factory.genomes])
  (:use clojure.pprint))


;; fixtures


(def all-moves
  [ :head :tail :subhead :append :left :right
    :prev :next #(rand-int 1000)])


(def all-puts [:L :R])


(defn some-items
  "returns a collection of the counting numbers and empty lists as indicated"
  [numbers branches]
  (concat (repeat branches '()) (range 1 (+ 1 numbers)) ))


(defn any-move
  []
  (let [f (rand-nth all-moves)] (if (fn? f) (f) f)))


(defn random-gene
  []
  { :from (any-move)
    :put (rand-nth all-puts)})


(defn random-tree
  [numbers branches]
  (into
    [] 
    (map 
      #(assoc (random-gene) :item %)
      (some-items numbers branches))))


;; bug-catching

(dotimes [n 100]
  (let [t (random-tree 10 10)]
      (try
        (println (zip->push t))
      (catch Exception e (println (str (pprint t) "Exception: " (.getMessage e)))))))
  


(println "\n\n")


(def starter (random-tree 18 8))

(pprint starter)

(defn mutate
  [g]
  (assoc-in g [(rand-int (count g)) :from] (any-move)))


(def walking
  (take 100
    (iterate #(mutate %) starter)))


(pprint (map zip->push walking))

