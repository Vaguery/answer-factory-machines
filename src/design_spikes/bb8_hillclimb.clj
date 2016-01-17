(ns design-spikes.bb8-hillclimb
  (:use midje.sweet)
  (:require [clojure.zip :as zip])
  (:require [push.core :as p])
  (:require [push.interpreter.core :as i])
  (:use [answer-factory.genomes.bb8])
  (:use clojure.pprint)
  (:use clojure.data))


;; fixtures


(def all-moves
  [ :head :tail :subhead :append :left :right
    :prev :next #(rand-int 1000)])


(def all-puts [:L :R])


;; some random code generators

(defn random-instruction
  [interpreter]
  (p/known-instructions interpreter))


(fact
  (p/known-instructions (p/interpreter)) => (contains :integer-subtract))


(defn random-input
  [interpreter]
  (p/input-names interpreter))


(fact
  (p/input-names (p/interpreter :inputs {:a 8 :b 9})) => [:a :b] )


(defn random-literal
  []
  (condp = (rand-int 10)
    0 true 
    1 (* 10.0 (rand))
    3 false
    (rand-int 10)
    ))


(defn random-item
  [interpreter branch-prob]
  (if (< (rand) branch-prob)
    '()
    (if (< (rand) 0.01)
      (random-literal)
      (rand-nth (concat (p/known-instructions interpreter)
                        (p/input-names interpreter))))))


(defn any-move
  []
  (let [f (rand-nth all-moves)] (if (fn? f) (f) f)))


(defn random-gene
  [interpreter prob]
  { :from (any-move)
    :put (rand-nth all-puts)
    :item (random-item interpreter prob)})


(defn random-genome
  [interpreter prob size]
  (into [] (repeatedly size #(random-gene interpreter prob))))


(def x (random-genome (p/interpreter :inputs {:a 8 :b 11}) 0.1 10 ))
(def y (random-genome (p/interpreter :inputs {:a 8 :b 11}) 0.1 10 ))


(defn point-crossover
  [mom dad]
  (into [] (concat

      (take (max (rand-int (count mom)) 1) mom)
      (drop (min (rand-int (count dad)) (dec (count dad))) dad))))


(fact "point-crossover does a thing"
  (first (point-crossover x y)) => (first x)
  (last (point-crossover x y)) => (last y))


(defn uniform-crossover
  [mom dad]
  (map
    #(if (< (rand) 0.5)
      (nth mom %)
      (nth dad %))
    (range 0 (count mom))))


(fact "uniform-crossover does a thing"
  (count (uniform-crossover x y)) => (count x)
  (concat x y) => (contains (uniform-crossover x y) :gaps-ok :in-any-order))




(defn item-mutate
  [genome interpreter prob]
  (assoc-in
    genome 
    [(rand-int (count genome)) :item]
    (random-item interpreter prob)))


(fact "mutation does a thing"
  (map :item (item-mutate x (p/interpreter :inputs {:a 8 :b 9}) 0.1)) =not=>
    (map :item x))


(defn gene-mutate
  [genome interpreter prob]
  (assoc
    genome 
    (rand-int (count genome))
    (random-gene interpreter prob)))


(fact "mutation does a thing"
  (bb8->push (gene-mutate x (p/interpreter :inputs {:a 8 :b 9}) 0.1)) =not=> 
  (bb8->push x))



(def x-runner (p/interpreter :inputs {:a 8 :b 9}))


(defn run-over-input-range
  [genome]
  (into []
    (map #(p/get-stack
                (p/run 
                  x-runner
                  (bb8->push genome)
                  300 :inputs {:a % :b (* % -8.7)})
                :boolean) (range -10 10))))


; (loop [collection {}
;        g (random-genome x-runner 0.2 20)
;        feature-vec (run-over-input-range g)
;        counter 0]
;   (if (> (count collection) 50)
;     (do
;       (println (sort-by first (for [[k v] collection] [(count v) (into [] k)])))
;       collection)
;     (do
;       (println (str counter " " (count collection)))
;       (recur  (assoc
;               collection
;               feature-vec
;               (conj (get collection feature-vec #{}) g))
;             (gene-mutate g x-runner 0.2) ;; g
;             (run-over-input-range g) ;; feature-vec
;             (inc counter)
;        ))))



