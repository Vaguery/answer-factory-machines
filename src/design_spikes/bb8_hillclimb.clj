(ns design-spikes.bb8-hillclimb
  (:use midje.sweet)
  (:require [clojure.zip :as zip])
  (:require [push.core :as p])
  (:require [push.interpreter.core :as i])
  (:use [answer-factory.genomes.bb8])
  (:use clojure.pprint))


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
    1 (* 100.0 (rand))
    3 false
    (rand-int 100)
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


(def x (random-genome (p/interpreter :inputs {:a 8 :b 11}) 0.1 100 ))

; (pprint x)
; (println )

(def x-runner (p/interpreter :inputs {:a 8 :b 9}))


(println
  (map #(peek (p/get-stack (p/run
                        x-runner
                        (bb8->push x)
                        1000
                        :inputs {:a % :b (* % 11)}) 
                      :integer))
        (range 0 10)))


