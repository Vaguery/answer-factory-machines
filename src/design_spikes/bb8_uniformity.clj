(ns design-spikes.bb8-uniformity
  (:use midje.sweet)
  (:require [clojure.zip :as zip])
  (:use [answer-factory.genome.bb8])
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

