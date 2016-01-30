(ns answer-factory.util.random)


(defn binomial-sample
  "takes a probability value p, and an optional keyword argument :limit; returns a binomial sample with the probability of 'heads' set to p, with a maximum value of :limit + 1"
  [p & {:keys [limit] :or {limit 20}}]
  (loop [result 0]
    (if (or (> (rand) p) (> result limit))
      result
      (recur (inc result)))))


(defn discrete-sample
  "Takes two collections: one of items being sampled, and one (of equal size) of non-negative numbers which are the 'weights' of each item in the first set; each item is sampled with a probability proportional to its relative weight. Throws exceptions if the two collections are of different sizes, or any weight is negative."
  [items weights]
  (cond
    (not= (count items) (count weights))
      (throw (Exception. "discrete-sample item & weight collections are different sizes"))
    (some neg? weights)
      (throw (Exception. "discrete-sample weights must all be positive numbers"))
    :else
      (let [total   (float (reduce + weights))
            cutoffs (map #(/ % total) (reductions + weights))
            sample  (rand)
            which   (count (filter #(> sample %) cutoffs))]
        (nth items which))))
