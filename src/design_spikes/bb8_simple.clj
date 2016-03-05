(ns design-spikes.bb8-simple
  (:use midje.sweet)
  (:require [clojure.zip :as zip])
  (:require [clj-uuid :as uuid])
  (:require [push.core :as p])
  (:require [push.interpreter.core :as i])
  (:use [answer-factory.genome.bb8])
  (:use clojure.pprint)
  (:require [clojure.edn :as edn])
  (:require [ragtime.jdbc :as jdbc])
  (:require [ragtime.repl :as repl])
  (:require [clojure.java.jdbc :as j])
  (:require [clj-time.local :as t])
  (:require [clojure.math.numeric-tower :as math])
  (:require [answer-factory.rubric.push :as rubric])
  (:require [answer-factory.operator.guess :as guess])
  )


;; fixtures


(def x-runner (p/interpreter :bindings {:x 12 :y 18}))


;; some random code generators


(defn run-over-input-range
  [genome]
  (into []
    (map #(p/get-stack
                (p/run 
                  x-runner
                  (bb8->push genome)
                  300 :bindings {:a % :b (* % -8.7)})
                :integer) (range -10 10))))



;;
;; some db setup stuff
;;

(defn silent-reporter
  "For ragtime use; prints nothing to STDOUT"
  [& stuff]
  ;; do nothing
  )


(def db-migrate-config
  { :datastore 
      (jdbc/sql-database 
        {:connection-uri "jdbc:sqlite:resources/db/test.db"})
    :migrations 
      (jdbc/load-resources "migrations")
    ; :reporter silent-reporter
    })



;;
;; jdbc
;;

(def population-db { :subprotocol "sqlite"
                     :subname "resources/db/test.db"})


(fact "I can roll back all the migrations, and then re-run them" :spike
  (repl/rollback db-migrate-config 5) ;; kill everything
  (repl/migrate db-migrate-config)    ;; start afresh
  (fact "there is an empty table now"
    (let [r (j/query population-db ["SELECT count(id) from answers"])]
      (first r) => {(keyword "count(id)") 0})))


(defn genome->sql
  [genome]
  {:id (pr-str (uuid/v4))
   :genome (pr-str genome)
   :program (pr-str (bb8->push genome))
   :timestamp (pr-str (t/local-now))})


(def random-pop
  (repeatedly 100
    (fn [] (genome->sql
      (guess/bb8-genome-guess
        10
        { #(guess/boolean-guess)                   20
          #(guess/integer-guess 100)               10
          #(guess/tidy-float-guess 100 8)          10
          #(guess/char-guess :ascii-chars)         10.5
          #(guess/string-guess :ascii-chars 40)    10
          #(guess/ref-guess x-runner)              30
          #(guess/instruction-guess x-runner)      30
        })))))



(fact "I can write answers" :spike
  (apply (partial j/insert! population-db :answers) random-pop)
  (fact "there are answers in the table now"
    (let [r (j/query population-db ["SELECT count(id) from answers"])]
      (first r) => {(keyword "count(id)") 100})))


; ;; rubrics

; ;; let's do y=a+6


(defn simple-training-set
  [interpreter io-pairs]
  (for [[setup expected] io-pairs]
    {:interpreter (i/bind-inputs interpreter setup)
     :inputs setup
     :outputs expected
     :results-fn (fn [interpreter]
        {:y (first (p/get-stack interpreter :integer))})
     }))


(println (map :inputs (simple-training-set
  (p/interpreter :bindings {:x1 1 :x2 11})
  [ [{:x1 8 :x2 12} {:y 200}]
    [{:x1 3 :x2 11} {:y 140}]
    [{:x1 18 :x2 2} {:y 200}] ]
  )))