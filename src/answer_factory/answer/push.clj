(ns answer-factory.answer.push
  (:require [clj-uuid :as uuid]))


(defrecord PushAnswer [id genome scores])

(defn make-pushanswer
  "Builds a PushAnswer record from a given genome; creates a random uuid (using v1, from which a timestamp of creation can be derived if needed), and leaves the scores empty."
  [g]
  (->PushAnswer (uuid/v1) g {}))


(defn get-score
  [answer rubric]
  (get-in answer [:scores rubric]))