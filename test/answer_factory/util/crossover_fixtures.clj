(ns answer-factory.util.crossover-fixtures
  (:use midje.sweet)
  (:use answer-factory.answer.push)
  (:use answer-factory.rubric.push))


;; some fixtures


(def bb8-1
  [{:from :up, :put :R, :item 1}      
   {:from :up, :put :R, :item 2}      
   {:from :up, :put :R, :item 3}    
   {:from :up, :put :R, :item 4}      
   {:from :up, :put :R, :item 5}        
   {:from :up, :put :R, :item 6}]) 


(def bb8-2
  [{:from :down, :put :L, :item :a}     
   {:from :down, :put :L, :item :b}     
   {:from :down, :put :L, :item :c}   
   {:from :down, :put :L, :item :d}     
   {:from :down, :put :L, :item :e}]) 


(def plush-1
  [{:item 1 :close 0}
   {:item 2 :close 0}
   {:item 3 :close 0}
   {:item 4 :close 0}
   {:item 5 :close 0}])   ;; [1 2 3 4 5]


(def plush-2
  [{:item :a :close 0}
   {:item :b :close 0}
   {:item :c :close 0}
   {:item :d :close 0}])   ;; [:a :b :c :d]