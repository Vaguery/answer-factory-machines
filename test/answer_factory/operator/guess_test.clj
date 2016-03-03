(ns answer-factory.operator.guess-test
  (:use midje.sweet)
  (:use answer-factory.answer.push)
  (:require [answer-factory.util.selection-fixtures :as fixtures])
  (:use answer-factory.operator.guess)
  (:require [push.core :as push]))


(fact "boolean-guess"
  (boolean-guess) => false
    (provided (rand) => 0.1)
  (boolean-guess) => true
    (provided (rand) => 0.9))


(fact "char-guess"
  (char-guess :ascii) => \Q
    (provided (rand-int anything) => 81)
  (char-guess :ascii-chars) => \q
    (provided (rand-int anything) => 81)
  (char-guess :unicode) => \ō
    (provided (rand-int anything) => 333)
  (char-guess 881) => (throws #"unexpected character guess range"))


(fact "integer-guess"
  (integer-guess 0) => 0
  (integer-guess 10) => -5
    (provided (rand-int anything) => 5))


(fact "float-guess"
  (float-guess 0) => 0.0
  (float-guess 10) => -3.5
    (provided (rand anything) => 6.5))


(fact "tidy-float-guess"
  (tidy-float-guess 0 8) => 0.0
  (tidy-float-guess 100 8) => 12.625
    (provided (rand-int anything) => 901))


(fact "instruction-guess"
  (instruction-guess (push/interpreter)) => :foo
    (provided (rand-nth anything) => :foo)
  (keys (:instructions (push/interpreter))) =>
    (contains (instruction-guess (push/interpreter))))


(fact "instruction-guess when none are registered"
  (instruction-guess (assoc (push/interpreter) :instructions {})) => nil)


(fact "ref-guess"
  (ref-guess (push/interpreter)) => :foo
    (provided (rand-nth anything) => :foo)
  (ref-guess (push/interpreter :bindings {:abc 77})) => :abc
  (keys (:bindings (push/interpreter :bindings {:foo 8 :bar 9}))) =>
    (contains (ref-guess (push/interpreter :bindings {:foo 8 :bar 9}))))


(fact "ref-guess when none are registered"
  (ref-guess (push/interpreter)) => nil)


(fact "string-guess"
  (string-guess :ascii 10) => "XoXoX"
    (provided (rand-int anything) =streams=> [5 88 111 88 111 88 111])
  (string-guess :unicode 10) => "¼їĠҰľ"
    (provided (rand-int anything) =streams=> [5 188 1111 288 1200 318]))


(fact "string-guess when a wrong charset is specified"
  (string-guess :foo 72) => (throws #"unexpected character guess range"))


;;; item-guess


(fact "item-guess wants at least one entry"
  (item-guess ) => (throws #"item-guess requires one or more"))

(fact "item-guess works with a vector"
  (item-guess [99]) => 99)

(fact "item-guess works with a vector"
  [1 2 3 4 5 6] => (contains (item-guess [1 2 3 4 5 6])))

(fact "item-guess works with multiple vectors"
  [1 :a] => (contains (item-guess [1] [:a])))


(fact "item-guess works with functions and vectors"
  (let [foo (fn [] (+ 55 20 2))]
    (item-guess foo ) => 77
    (item-guess (fn [] constantly 77)
                (fn [] (+ 33 44))
                #(* 11 7)
                [77 77 770/10]) => 77))


;;; weighted-item-guess


(fact "weighted-item-guess wants at least one entry"
  (weighted-item-guess {}) => (throws #"weighted-item-guess requires generators"))


(fact "weighted-item-guess throws an exception if any weights are negative"
  (weighted-item-guess {[8] -2}) => (throws #"must all be positive numbers"))


(fact "weighted-item-guess works with one vector item"
  (weighted-item-guess {[99] 812}) => 99)

(fact "weighted-item-guess works with a vector of multiple items"
  [1 2 3 4 5 6] => (contains (weighted-item-guess {[1 2 3 4 5 6] 772})))

(fact "weighted-item-guess works with multiple vectors"
  [1 :a] => (contains (weighted-item-guess {[1] 7 [:a] 99})))


(fact "weighted-item-guess works with functions and vectors"
  (let [foo (fn [] (+ 55 20 2))]

    (weighted-item-guess {foo 8}) => 77
    (weighted-item-guess {(fn [] constantly 77)   20
                          (fn [] (+ 33 44))       30
                          #(* 11 7)               40
                          [77 77 770/10]          10}) => 77))



;;; plush genes


(fact "plush-guess produces a Plush gene"

  (plush-guess {[1] 9} [9] 0.0) => {:close 0, :item 1, :silent false}

  (plush-guess {[1] 12} [9] 1.0) => {:close 0, :item 1, :silent true}

  (plush-guess {[1] 8} [8 4 2 2] 0.0) => {:close 3, :item 1, :silent false}
    (provided (rand) => 0.9)

  (plush-guess {(fn [] (- 5 4))   10
                (fn [] (* -1 -1)) 10
                [1 3/3]    10}
               [8]
               0.0) => {:close 0, :item 1, :silent false})


;;; constructing random plush genes from scratch


(fact "it all fits together without breaking"
  (let [target (push/interpreter :bindings {:x1 8 :x2 8 :x3 8})]
    (plush-guess
      { #(boolean-guess)                   20
        #(integer-guess 100)               10
        #(tidy-float-guess 100 8)          10
        #(char-guess :ascii-chars)               1
        #(string-guess :ascii-chars 40)    1
        #(ref-guess target)                30
        #(instruction-guess target)        80
      }
      [88 44 13 2]
      0.05
      ) =not=> (throws)      ;; examine this to see what we're getting
  ))


;;; bb8 genes


(fact "bb8-guess produces a bb8 gene"
  (:item (bb8-guess {[1] 99})) => 1
  [:L :R] => (contains (:put (bb8-guess {[1] 99})))
  [:head :tail :subhead :append :left :right :prev :next :up :down] =>
    (contains (:from (bb8-guess {[1] 99}))))



;;; constructing random bb8 genes from scratch


(fact "it all fits together without breaking"
  (let [target (push/interpreter :bindings {:x1 8 :x2 8 :x3 8})]
    (bb8-guess
      { #(boolean-guess)                   20
        #(integer-guess 100)               10
        #(tidy-float-guess 100 8)          10
        #(char-guess :ascii-chars)         10.5
        #(string-guess :ascii-chars 40)    10
        #(ref-guess target)                30
        #(instruction-guess target)        30
      })  =not=> (throws)      ;; examine this to see what we're getting
  ))
