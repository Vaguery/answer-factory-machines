(ns design-spikes.plush-sampling
  (:use midje.sweet)
  (:use answer-factory.genome.plush)
  (:use answer-factory.operator.guess)
  (:require [push.core :as push])
  )


(def my-interpreter (push/interpreter :bindings {:x1 1 :x2 2 :y false}))


; (fact "the number of instructions which open code blocks is vanishingly small"
;   (count (filter #(pos? (second %)) derived-push-branch-map)) => 38
;   (count (keys (:instructions my-interpreter))) => 741
;   (/ 38 741.0) => (roughly 1/20 0.01))


; (dotimes [n 5000]
;   (let [g (into [] (repeatedly 500
;                       (fn [] (plush-guess
;                         { #(boolean-guess)                    20
;                           #(integer-guess 100)                10
;                           #(tidy-float-guess 100 8)           10
;                           #(char-guess :ascii-chars)          1
;                           #(string-guess :ascii-chars 40)     1
;                           #(ref-guess my-interpreter)         30
;                           [:noop_open_paren]                  10
;                           #(instruction-guess my-interpreter) 80
;                         }
;                         [7] [772 206 21 1]
;                         0.05))))
;         p (plush->push g :branch-map derived-push-branch-map)]
;     ; (println g)
;     ; (println p)
;     (let [ran-it (push/run my-interpreter (plush->push g) 5000)]
;       (println
;         (str
;         (count (filter #(push.util.type-checkers/boolean? (:item %)) g))
;         "\t"
;         (count (filter #(push.util.type-checkers/boolean? (:item %)) (get-in ran-it [:stacks :log])))
;         "\t"
;         (:counter ran-it))))
;   ))

