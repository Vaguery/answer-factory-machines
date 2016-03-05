(ns answer-factory.operator.guess
  (:require [answer-factory.answer.push :as answer])
  (:require [answer-factory.util.random :as random]))


;; Note: the literal (ERC) `guess` functions are here in case the interpreter library doesn't
;; have the necessary `random` code for each type.


(defn boolean-guess
  "Returns `true` or `false` with equal probability."
  []
  (< 0.5 (rand)))



(defn char-guess
  "Takes as an argument either `:ascii` or `:unicode`, and returns a uniformly sampled character from the range (0-127) or (0-64k), respectively."
  [charset]
  (condp = charset
    :ascii (char (rand-int 128))     ;; ASCII, including [0,32]
    :ascii-chars (char (+ 32 (rand-int 96)))  ;; ASCII visible chars only
    :unicode (char (rand-int 65535)) ;; most of these are undefined!
    (throw (Exception. "unexpected character guess range"))))



(defn float-guess
  "Takes a single half-range argument, and returns an float uniformly sampled from `[-half-range, half-range)`."
  [half-range]
  (- (rand (* 2.0 half-range)) half-range))



(defn tidy-float-guess
  "Takes a single half-range (integer) argument, and returns an float uniformly sampled from `[-half-range, half-range)`, sampling with the indicated binary resolution."
  [half-range resolution]
  (double 
    (/ (- (rand-int (* 2 half-range resolution)) (* half-range resolution))
      resolution)))



(defn instruction-guess
  "Takes an Interpreter instance, and returns one of the registered instruction keywords, sampled uniformly. If there are none, nil is returned."
  [interpreter]
  (rand-nth (keys (:instructions interpreter))))



(defn integer-guess
  "Takes a single half-range argument, and returns an integer uniformly sampled from `[-half-range, half-range)`."
  [half-range]
  (- (rand-int (* 2 half-range)) half-range))



(defn ref-guess
  "Takes an Interpreter instance, and returns one of the registered binding keywords, sampled uniformly. If there are none, nil is returned."
  [interpreter]
  (rand-nth (keys (:bindings interpreter))))



(defn string-guess
  "Takes a character range specifier (:ascii or :unicode), and a max length. Returns a string composed of characters from the specified range, of length uniformly chosen between 0 and the max"
  [charset maxlength]
  (apply str (take (rand-int maxlength) (repeatedly #(char-guess charset)))))



;; aggregator



(defn item-guess
  "Takes one or more self-contained _generator_ ('guess') functions, and any number of vectors of literal values. Samples each root argument uniformly: if that item is a function call, it is called and its result is returned; if a vector of literals, one element is sampled uniformly. Nested vector arguments are not sampled more deeply than that."
  [& options]
  (if (empty? options)
    (throw (Exception. "item-guess requires one or more generators or vectors of items"))
    (let [which (rand-nth (into [] options))]
      (if (vector? which)
        (rand-nth which)
        (which) ))))


(defn weighted-item-guess
  "Takes a hash-map in which the keys are _generator_ items (which must be arity-0 functions or vectors of literals) and the associated values are non-negative numeric constants. The numeric values are taken to be the relative weights for the random selection of the key items. When selected, a key that is a function is called, and a key that is a vector of literals is sampled uniformly."
  [generator-map]
  (if (empty? generator-map)
    (throw (Exception. "weighted-item-guess requires generators"))
    (let [things    (keys generator-map)
          how-often (vals generator-map)
          choice    (random/discrete-sample
                      things
                      how-often)]
      (if (vector? choice)
        (rand-nth choice)
        (choice) ))))


;; plush gene



(defn plush-guess
  "Takes a hashmap with one or more `item-guess` generators (vectors or functions) as keys, and non-negative numbers as values, a vector of non-negative relative weights for the first n `:close` gene values, and a :silent probabilty. Returns a single Plush gene by invoking `weighted-item-guess` on the hash-map."
  [item-hash close-weights silent-probability]
  {:item   (weighted-item-guess item-hash)
   :close  (random/discrete-sample (range (count close-weights)) close-weights)
   :silent (< (rand) silent-probability)})



;; bb8 gene



(defn bb8-guess
  "Takes a hashmap with one or more `item-guess` generators (vectors or functions) as keys, and non-negative numbers as values. Returns a single bb8 gene in which the `:from` is selected from the (non-integer) directives, and where the `:put` is selected from `[:L :R]` uniformly."
  [item-hash]
  {:item   (weighted-item-guess item-hash)
   :from   (if (< (rand) 1/20)
              (rand-int 1000)
              (rand-nth [:head :tail :subhead :append :left :right :prev :next :up :down]))    
   :put    (rand-nth [:L :R])})



(defn bb8-indexed-guess
  "Takes a hashmap with one or more `item-guess` generators (vectors or functions) as keys, and non-negative numbers as values. Returns a single bb8 gene in which the `:from` is selected from the (non-integer) directives, and where the `:put` is a random integer from 1-1000."
  [item-hash]
  {:item   (weighted-item-guess item-hash)
   :from   (rand-int 1000)    
   :put    (rand-nth [:L :R])})



(defn bb8-genome-guess
  "Takes a number of elements and an item hash. Returns a genome of exactly the specified number of bb8 genes."
  [size item-hash]
  (into [] (repeatedly size #(bb8-guess item-hash))))



