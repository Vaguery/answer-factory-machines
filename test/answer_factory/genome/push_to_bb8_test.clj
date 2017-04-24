(ns answer-factory.genome.push-to-bb8-test
  (:require [clojure.zip :as zip]
            [push.util.numerics :as num]
            [answer-factory.genome.bb8 :as bb8])
  (:use [answer-factory.genome.push-to-bb8])
  (:use midje.sweet)
  )


(fact "tail-written simple programs are simple, and written in left-to-write order"
  (push-to-bb8 '(1)) => [{:from :tail :put :L :item 1}]
  (push-to-bb8 '(1 2 3)) => [{:from :tail :put :L :item 1}
                             {:from :tail :put :L :item 2}
                             {:from :tail :put :L :item 3}]
  (push-to-bb8 '(1 [2 3] 4)) =>
                            [{:from :tail, :item 1, :put :L}
                             {:from :tail, :item [2 3], :put :L}
                             {:from :tail, :item 4, :put :L}]
  )

(fact "tail-writing drops into subtrees as it walks along"
  (push-to-bb8 '(1 (2))) => 99
  )
