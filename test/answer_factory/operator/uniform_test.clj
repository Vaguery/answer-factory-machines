(ns answer-factory.operator.uniform-test
  (:use midje.sweet)
  (:use answer-factory.answer.push)
  (:use answer-factory.operator.select))


;; some fixtures

(defn set-scores
  [answer new-fitness-map]
  (assoc answer :scores new-fitness-map))


(def dude1
  (set-scores (make-pushanswer []) {:f1 8 :f2 22}))


(fact "dude1 has some scores"
  (get-score dude1 :f1) => 8)


(fact "uniform selection returns the one guy we pass in"
  (uniform-selection [dude1]) => dude1)


(fact "uniform selection throws an exception with an empty argument list"
  (uniform-selection []) => (throws))

