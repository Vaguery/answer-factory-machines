(defproject answer-factory "0.0.1-SNAPSHOT"
  :description "A platform for generative programming."
  :dependencies [[org.clojure/clojure "1.7.0"]]
  :profiles {:dev {:dependencies [[midje "1.8.2"]
                                  [danlentz/clj-uuid "0.1.6"]
                                  [push-in-clojure "0.1.4-SNAPSHOT"]]}})
  
