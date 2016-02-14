(defproject answer-factory "0.0.1-SNAPSHOT"
  :description "A platform for generative programming."
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [danlentz/clj-uuid "0.1.6"]
                 [klapaucius "0.1.8-SNAPSHOT"]
                 [korma "0.4.0"]
                 [org.xerial/sqlite-jdbc "3.8.11.2"]
                 [ragtime "0.5.2"]]
  :profiles {:dev {:dependencies [[midje "1.8.2"]]}})
  
