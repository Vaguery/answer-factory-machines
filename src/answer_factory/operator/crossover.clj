(ns answer-factory.operator.crossover
  (:require [answer-factory.util.random :as rand])
  (:require [answer-factory.answer.push :as answer]))

;; genome-agnostic


(defn pad-with-nil
  "Takes a genome and a number, and returns a revised genome with the indicated number of `nil` values padded at the front"
  [genome padding]
  (into [] (concat (repeat padding nil) genome)))



(defn one-point-crossover
  "Takes two genomes (not Answer records). Selects cut-points in each uniformly and independently, and produces _two_ offspring obtained by swapping ends at those points. NOTE: does not check the genomes are the same representation!"
  [mom dad]
  (let [cut-m (rand-int (inc (count mom)))
        cut-d (rand-int (inc (count dad)))]
    [
      (into [] (concat (take cut-m mom) (drop cut-d dad)))
      (into [] (concat (take cut-d dad) (drop cut-m mom)))
    ]))



(defn safe-conj
  "Takes a collection (typically a vector) and an item. If the item is not nil, it conj's the item onto the collection; otherwise the collection is returned unchanged"
  [coll item]
  (if (nil? item)
    coll
    (conj coll item)))


(defn uniform-crossover
  "Takes two genomes (not Answer records). Starting at the front, with probability 1/2 each element is either sent to the left or right 'child', until all elements of both genomes are assorted. NOTE: does not check the genomes are the same representation!"
  [mom dad]
  (loop [mom-bits mom
         dad-bits dad
         swap?    (if (< 0.5 (rand)) true false)
         kid1     []
         kid2     []]
    (if (and (empty? mom-bits) (empty? dad-bits))
      [ kid1 kid2 ]
      (recur (rest mom-bits)
             (rest dad-bits)
             (if (< 0.5 (rand)) true false)
             (safe-conj kid1 (first (if swap? dad-bits mom-bits)))
             (safe-conj kid2 (first (if swap? mom-bits dad-bits)))))))


;; "unaligned" variations:
;;
;; abcde     -5
;;      123
;; abcde     -4
;;     123
;; abcde     -3
;;    123
;; abcde     -2
;;   123
;; abcde     -1
;;  123
;; abcde     0
;; 123
;;  abcde    1
;; 123
;;   abcde   2
;; 123
;;    abcde  3
;; 123


(defn unaligned-one-point-crossover
  "Takes two genomes (not Answer records). First the two genomes are aligned randomly, selecting an alignment offset for `dad` somewhere in the range including `(- (count dad))` and `(count mom)`. For example, if `mom` is `123` and dad `abcde`, the possible alignments range from `abcde123` to `123abcde`, inclusive. After filling missing space with `nil` placeholders, one-point-crossover is applied."
  [mom dad]
  (let [mom-count (count mom)
        dad-count (count dad)
        alignment (- (rand-int (+ mom-count dad-count 1)) dad-count)]
    (into []
      (map (partial remove nil?)
        (if (neg? alignment)
          (one-point-crossover (pad-with-nil mom (- alignment)) dad)
          (one-point-crossover mom (pad-with-nil dad alignment))
      )))))


(defn unaligned-uniform-crossover
  "Takes two genomes (not Answer records). First the two genomes are aligned randomly, selecting an alignment offset for `dad` somewhere in the range including `(- (count dad))` and `(count mom)`. For example, if `mom` is `123` and dad `abcde`, the possible alignments range from `abcde123` to `123abcde`, inclusive. After filling missing space with `nil` placeholders, uniform-crossover is applied."
  [mom dad]
  (let [mom-count (count mom)
        dad-count (count dad)
        alignment (- (rand-int (+ mom-count dad-count 1)) dad-count)]
    (if (neg? alignment)
      (uniform-crossover (pad-with-nil mom (- alignment)) dad)
      (uniform-crossover mom (pad-with-nil dad alignment))
      )))

