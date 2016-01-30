(ns answer-factory.util.test
  (:use answer-factory.answer.push))

(defn set-scores
  [answer new-fitness-map]
  (assoc answer :scores new-fitness-map))


(defn dude-with-scores
  [scores]
  (set-scores (make-pushanswer []) scores))

