(ns design-spikes.bb8-hillclimb
  (:use midje.sweet)
  (:require [clojure.zip :as zip])
  (:require [push.core :as p])
  (:require [push.interpreter.core :as i])
  (:use [answer-factory.genome.bb8])
  (:use clojure.pprint)
  (:require [clojure.edn :as edn])
  (:require [ragtime.jdbc :as jdbc])
  (:require [ragtime.repl :as repl])
  (:require [clojure.java.jdbc :as j])
  (:require [clj-time.local :as t])
  )


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


(defn random-binding
  [interpreter]
  (p/binding-names interpreter))


(fact
  (p/binding-names (p/interpreter :bindings {:a 8 :b 9})) => [:a :b] )


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
                        (p/binding-names interpreter))))))


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


(def dude-x (random-genome (p/interpreter :bindings {:a 8 :b 11}) 0.1 10 ))
(def dude-y (random-genome (p/interpreter :bindings {:a 8 :b 11}) 0.1 20 ))


(defn point-crossover
  [mom dad]
  (into [] (concat

      (take (max (rand-int (count mom)) 1) mom)
      (drop (min (rand-int (count dad)) (dec (count dad))) dad))))


(fact "point-crossover does a thing"
  (first (point-crossover dude-x dude-y)) => (first dude-x)
  (last (point-crossover dude-x dude-y)) => (last dude-y)
  (> (+ (count dude-x) (count dude-y))
    (count (point-crossover dude-x dude-y))) => true
  )


(defn uniform-crossover
  [mom dad]
  (map
    #(if (< (rand) 0.5)
      (nth mom %)
      (nth dad %))
    (range 0 (count mom))))


(fact "uniform-crossover does a thing"
  (count (uniform-crossover dude-x dude-y)) => (count dude-x)
  (concat dude-x dude-y) => (contains (uniform-crossover dude-x dude-y) :gaps-ok :in-any-order))



(defn item-mutate
  [genome interpreter prob]
  (assoc-in
    genome 
    [(rand-int (count genome)) :item]
    (random-item interpreter prob)))


(fact "mutation does a thing"
  (map :item (item-mutate dude-x (p/interpreter :bindings {:a 8 :b 9}) 0.1)) =not=>
    (map :item dude-x))


(defn gene-mutate
  [genome interpreter prob]
  (assoc
    genome 
    (rand-int (count genome))
    (random-gene interpreter prob)))


(fact "mutation does a thing"
  (bb8->push (gene-mutate dude-x (p/interpreter :bindings {:a 8 :b 9}) 0.1)) =not=> 
  (bb8->push dude-x))



(def x-runner (p/interpreter :bindings {:a 8 :b 9}))


(defn run-over-input-range
  [genome]
  (into []
    (map #(p/get-stack
                (p/run 
                  x-runner
                  (bb8->push genome)
                  300 :bindings {:a % :b (* % -8.7)})
                :integer) (range -10 10))))


(println (run-over-input-range dude-x))


;;
;; some db stuff
;;

(def db-migrate-config
  {:datastore 
    (jdbc/sql-database 
      {:connection-uri "jdbc:sqlite:resources/db/test.db"})
   :migrations 
    (jdbc/load-resources "migrations")})


(repl/rollback db-migrate-config 5) ;; kill everything
(repl/migrate db-migrate-config)    ;; start afresh


;;
;; jdbc
;;

(def population-db { :subprotocol "sqlite"
                     :subname "resources/db/test.db"})


(defn genome->sql
  [genome]
  {:genome (pr-str genome)
   :program (pr-str (bb8->push genome))
   :timestamp (pr-str (t/local-now))})



(j/insert! population-db :answers
  (genome->sql dude-x)
  (genome->sql dude-y))


(println
  (j/query population-db
    ["select * from answers LIMIT 1"]))


(defn f [thing] (+ thing 88))
(println (clojure.repl/source f))

