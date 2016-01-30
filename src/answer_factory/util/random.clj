(ns answer-factory.util.random)


(defn binomial-sample
  "takes a probability value p, and an optional keyword argument :limit; returns a binomial sample with the probability of 'heads' set to p, with a maximum value of :limit + 1"
  [p & {:keys [limit] :or {limit 20}}]
  (loop [result 0]
    (if (or (> (rand) p) (> result limit))
      result
      (recur (inc result)))))


(defn discrete-sample
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
