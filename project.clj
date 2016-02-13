(defproject answer-factory "0.0.1-SNAPSHOT"
  :description "A platform for generative programming."
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [danlentz/clj-uuid "0.1.6"]
                 [klapaucius "0.1.8-SNAPSHOT"]]
  :profiles {:dev {:dependencies [[midje "1.8.2"]]}})
  
