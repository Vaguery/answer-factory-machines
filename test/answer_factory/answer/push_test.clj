(ns answer-factory.answer.push-test
  (:use midje.sweet)
  (:use answer-factory.answer.push))


(fact "make-pushanswer creates a new UUID id"
  (class (:id (make-pushanswer [] :bb8))) => java.util.UUID)


(fact "make-pushanswer accepts a genome and dialect argument"
  (:genome (make-pushanswer [] :bb8)) => []
  (:dialect (make-pushanswer [] :bb8)) => :bb8
  )


(fact "make-pushanswer throws an exception if an unrecognized dialect is specified"
  (make-pushanswer [] :bad-dates) => (throws #"unknown genome dialect"))


(fact "make-pushanswer knows about bb8 genomes"
  (:program (make-pushanswer [] :bb8)) => []
  (:program (make-pushanswer [{:from :prev, :put :R, :item 1}     
                              {:from :down, :put :L, :item '()}   
                              {:from :append, :put :L, :item '()} 
                              {:from :prev, :put :L, :item 4}     
                              {:from :prev, :put :R, :item 5}     
                              {:from :prev, :put :R, :item 6}] :bb8)) => '[(6) (4) 5 1]
  )


(fact "make-pushanswer knows about plush genomes"
  (:program (make-pushanswer [] :plush)) => []
  (:program (make-pushanswer [{:item :code-quote :close 0}{:item 1 :close 8}] :plush)) => 
    '[:code-quote (1)]
  )