(ns answer-factory.genome.push-to-bb8
  (:require [clojure.zip :as zip]
            [push.util.numerics :as num]
            ))

(defn tail-genome
  "produces the most boring flat `bb8` genome possible"
  [items]
  (reduce
    (fn [code item]
      (if (seq? item)
        (concat code (tail-genome item))
        (conj code {:from :tail :put :L :item item})))
    []
    items
    ))


(defn push-to-bb8
  "takes a chunk of Push code and returns a bb8 genome that encodes that code"
  [push]
  (tail-genome push)
  )
