(ns answer-factory.rubric.rubric-test
  (:use midje.sweet)
  (:use answer-factory.rubric.push)
  (:require [push.core :as push]))



; context-map items
; - language
; - interpreter config
; - input assignments
;
; (run the program in that context)
;
; results-fn
; - access interpreter state
; - returns a map of results
; - map should not contain a score!


