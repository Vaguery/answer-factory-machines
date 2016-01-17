(ns answer-factory.answer.push
  (:require [clj-uuid :as uuid]))


(defrecord PushAnswer [id genome fitness])

(defn make-pushanswer
  "Builds a PushAnswer record from a given genome; creates a random uuid (using v1, from which a timestamp of creation can be derived if needed), and leaves the fitness map empty."
  [g]
  (->PushAnswer (uuid/v1) g {}))