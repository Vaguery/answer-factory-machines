(ns answer-factory.genomes.bb8-uniformity-test
  (:use midje.sweet)
  (:require [clojure.zip :as zip])
  (:use [answer-factory.genomes])
  (:use clojure.pprint))



;; fixtures

(def all-moves
  [ :head :tail :subhead :append :left :right
    :prev :next :up :down #(rand-int 1000)])


(def all-puts [:L :R])


(defn some-items
  "returns a collection of the counting numbers and empty lists as indicated"
  [numbers branches]
  (concat (repeat branches '()) (range 1 (+ 1 numbers)) ))


(defn random-gene
  []
  { :from (let [f (rand-nth all-moves)] (if (fn? f) (f) f))
    :put (rand-nth all-puts)})


(defn random-tree
  [numbers branches]
  (map #(assoc (random-gene) :item %) (shuffle (some-items numbers branches))))


; (dotimes [n 100]
;   (let [t (random-tree 20 10)]
;       (try
;         (println (zip->push t))
;       (catch Exception e (println (str (pprint t) "Exception: " (.getMessage e)))))))
  



