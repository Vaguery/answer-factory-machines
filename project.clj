(defproject answer-factory "0.0.1-SNAPSHOT"
  :description "A platform for generative programming."
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [danlentz/clj-uuid "0.1.6"]
                 [klapaucius "0.1.10-SNAPSHOT"]
                 [clj-time "0.11.0"]
                 [org.clojure/math.numeric-tower "0.0.4"]]
  :profiles {:dev {:dependencies [[midje "1.8.2"]]}})
  
