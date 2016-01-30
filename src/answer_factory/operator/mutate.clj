(ns answer-factory.operator.mutate
  (:require [answer-factory.util.random :as rand])
  (:require [answer-factory.answer.push :as answer]))

;; genome-agnostic

(defn mutate-gene-item
  "takes a Plush gene map, a probability of change, and a list of replacement items; returns the modified gene with the indicated probability, sampling uniformly from the list"
  [gene prob item-source]
  (if (and (< (rand) prob) (not-empty item-source))
    (let [new-item (rand-nth item-source)]
      (assoc gene :item new-item))
    gene))


(defn mutate-plush-close
  "takes a Plush gene map, a probability of resampling (mutation), a binomial coefficient, and a max cutoff; changes the :close field of the gene to a new binomial sample with the indicated probability"
  [gene prob coefficient cutoff]
  (if (< (rand) prob)
    (let [new-close (rand/binomial-sample coefficient :limit cutoff)]
      (assoc gene :close new-close))
    gene))


(defn mutate-plush-silence
  "takes a Plush gene map, a probability of resampling, and a probability that the resampled gene value will be `true` vs `false`; returns the Plush gene with the :silent gene (re)set to true with the given probability"
  [gene resample-prob silent-prob]
  (let [unsilent-prob (- 1.0 silent-prob)]
    (if (< (rand) resample-prob)
      (let [new-silence (rand/discrete-sample [true false] [silent-prob unsilent-prob])]
        (assoc gene :silent new-silence))
      gene)))

