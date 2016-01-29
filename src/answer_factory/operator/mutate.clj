(ns answer-factory.operator.mutate
  (:require [answer-factory.answer.push :as answer]))


(defn mutate-plush-item
  "takes a Plush gene map, a probability of change, and a list of replacement items; returns the modified gene with the indicated probability, sampling uniformly from the list"
  [gene prob item-source]
  (if (and (< (rand) prob) (not-empty item-source))
    (let [new-item (rand-nth item-source)]
      (assoc gene :item new-item))
    gene))



(defn binomial-sample
  "takes a probability value p, and an optional keyword argument :limit; returns a binomial sample with the probability of 'heads' set to p, with a maximum value of :limit + 1"
  [p & {:keys [limit] :or {limit 20}}]
  (loop [result 0]
    (if (or (> (rand) p) (> result limit))
      result
      (recur (inc result)))))



(defn mutate-plush-close
  "takes a Plush gene map, a probability of resampling (mutation), a binomial coefficient, and a max cutoff; changes the :close field of the gene to a new binomial sample with the indicated probability"
  [gene prob coefficient cutoff]
  (if (< (rand) prob)
    (let [new-close (binomial-sample coefficient :limit cutoff)]
      (assoc gene :close new-close))
    gene))


(defn mutate-plush-silence
  "takes a Plush gene map, a probability of resampling (mutation); returns the Plush gene with the :silent gene (re)set to true with the given probability"
  [gene prob]
  (if (< (rand) prob)
    (let [new-silence (rand-nth [true false])]
      (assoc gene :silent new-silence))
    gene))

