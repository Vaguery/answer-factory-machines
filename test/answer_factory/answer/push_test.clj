(ns answer-factory.answer.push-test
  (:use midje.sweet)
  (:use answer-factory.answer.push))


(fact "I can make a new PushAnswer"
  (:genome (make-pushanswer [1 2 3])) => [1 2 3]
  (:scores (make-pushanswer [1 2 3])) => {}
  (class (:id (make-pushanswer [1 2 3]))) => java.util.UUID)