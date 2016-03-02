(ns answer-factory.util.crossover-fixtures
  (:use midje.sweet)
  (:use answer-factory.answer.push)
  (:use answer-factory.rubric.push))


;; some fixtures


(def bb8-1
  [{:from :prev, :put :R, :item 1}      
   {:from :down, :put :R, :item 2}      
   {:from :append, :put :L, :item 3}    
   {:from :left, :put :R, :item 4}      
   {:from :up, :put :L, :item 5}        
   {:from :right, :put :R, :item 6}])   ;; [5 1 3 6 4 2]


(def bb8-2
  [{:from :prev, :put :R, :item :a}     
   {:from :down, :put :R, :item :b}     
   {:from :append, :put :L, :item :c}   
   {:from :left, :put :R, :item :d}     
   {:from :up, :put :L, :item :e}])  ;; [:e :a :c :d :b]


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