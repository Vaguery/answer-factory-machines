(ns answer-factory.answer.push
  (:require [clj-uuid :as uuid])
  (:require [answer-factory.genome.bb8 :as bb8])
  (:require [answer-factory.genome.plush :as plush])
  )


(defrecord PushAnswer [id genome dialect program])

(defn make-pushanswer
  "Builds a new PushAnswer record from a given genome: creates a random uuid, and translates the genome into a program using the designated dialect."
  [g d & {:keys [branch-map] :or {branch-map {} }}]
  (->PushAnswer (uuid/v4) g d 
    (cond
      (= d :bb8)   (bb8/bb8->push g)
      (= d :plush) (plush/plush->push g :branch-map branch-map)
      :else (throw (Exception. "unknown genome dialect")))
    ))
