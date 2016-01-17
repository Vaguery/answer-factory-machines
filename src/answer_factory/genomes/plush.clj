(ns answer-factory.genomes.plush)



(defn plush->push
  "takes a plush genome and an optional branching-map, and returns the translated push program"
  ([genome]
    (plush->push genome {}))
  ([genome branching-map]
    (map :item genome)))