(ns answer-factory.genomes.plush-test
  (:use midje.sweet)
  (:use answer-factory.genomes.plush)
  (:require [clojure.zip :as zip])
  (:require [answer-factory.genomes.bb8 :as bb8])
  (:use clojure.pprint))


;; [+] noop_open_paren
;; [ ] noop_delete_prev_paren_pair
;; [+] :parentheses metadata
;; [+] :close values
;; [+] :close-open values
;; [+] :close count
;; [+] :silent metadata


(fact "clean-insert adds a thing to a zipper without leaving the nil placeholder in an empty sub-list"
  (zip/root (zip/insert-left (zip/next (zip/seq-zip '())) 99)) =>
    '(99 nil) ;; because of the placeholder nil
  (zip/root (clean-insert (zip/next (zip/seq-zip '())) 99)) =>
    '(99))


(fact "empty-program returns an empty program zipper"
  (zip/root (empty-program)) => '()
  (zip/node (empty-program)) => nil
  (bb8/root? (empty-program)) => false)


;; adding a single item to an existing program

;; basic cases

(fact "append-token adds an item to the program without branching"
  (zip/root
    (append-token (empty-program) 99)) => '(99)
  (zip/root
    (append-token (bb8/fast-forward (zip/seq-zip '(1 2 (3 4)))) 99)) =>
      '(1 2 (3 4 99)))


(fact "append-as-sibling adds an item to the program in a new branch"
  (zip/root
    (append-as-sibling
      (bb8/fast-forward (zip/seq-zip '((88))))
      99)) => '((88) (99))
  (zip/root
    (append-as-sibling
      (bb8/fast-forward (zip/seq-zip '(1 2 (3 4 (88)))))
      99)) => '(1 2 (3 4 (88) (99))))


;; genes

;; fixture

(def little-tree
  (bb8/fast-forward (zip/seq-zip '(1 2 (3 4 (99))))))


(defn new-node-helper
  [tree-vector]
  (zip/node (first tree-vector)))


(defn new-root-helper
  [tree-vector]
  (zip/root (first tree-vector)))


(fact "move-up-safely works as intended"
  (zip/node little-tree) => 99
  (zip/node (move-up-safely little-tree)) => '(99)
  (zip/node (move-up-safely
              (move-up-safely little-tree))) => '(3 4 (99))
  (zip/node (move-up-safely
              (move-up-safely
                (move-up-safely little-tree)))) => '(3 4 (99))
  (zip/node (move-up-safely
              (move-up-safely
                (move-up-safely
                  (move-up-safely little-tree))))) => '(3 4 (99)))


(fact "begin-branch"
  (zip/root (begin-branch (empty-program))) => '(())
  (zip/node (begin-branch (empty-program))) => nil
  (zip/root (begin-branch little-tree)) => '(1 2 (3 4 (99 ())))
  (zip/node (begin-branch little-tree)) => nil)


(fact "close-up-one does nothing if the branch-stack is empty"
  (new-node-helper (close-up-one [little-tree '()])) => 99
  (new-node-helper (close-up-one [(zip/up little-tree) '()])) => '(99))



(fact "close-up-one pops one item off the branch-stack if possible"
  (second (close-up-one [little-tree '()])) => '()
  (second (close-up-one [little-tree '(:END)])) => '()
  (second (close-up-one [little-tree '(:END :foo)])) => '(:foo))


(fact "close-up-one moves the cursor up a level (until root) if the branch-stack ends in :END"
  (new-node-helper (close-up-one [little-tree '(:END)])) => '(99)
  (new-node-helper (close-up-one [(zip/up little-tree) '(:END)])) => '(3 4 (99)))



(fact "close-up-one moves the cursor up a level (until root) and starts a sibling if the branch-stack ends in :AGAIN"
  (new-root-helper (close-up-one [little-tree '(:AGAIN)])) => '(1 2 (3 4 (99) ()))
  (new-node-helper (close-up-one [little-tree '(:AGAIN)])) => nil
  
  (new-root-helper
    (close-up-one
      [(zip/up little-tree) '(:AGAIN)])) => '(1 2 (3 4 (99)) ())
  (new-node-helper
    (close-up-one
      [(zip/up little-tree) '(:AGAIN)])) => nil)


(fact "close-up-n iterates close-up-one on the state n times"
  (close-up-n [little-tree '(:END :END :END)] 0) => [little-tree '(:END :END :END)]
  (close-up-n [little-tree '(:END :END :END)] 1) =>
    (close-up-one [little-tree '(:END :END :END)])
  (close-up-n [little-tree '(:END :END :END)] 2) =>
    (close-up-one (close-up-one [little-tree '(:END :END :END)]))
  (close-up-n [little-tree '(:END :END :END)] 3) =>
    (close-up-one (close-up-one (close-up-one [little-tree '(:END :END :END)])))
  )


(fact "close-up-n exhausts the branch-stack eventually"
  (second (close-up-n [little-tree '(:END :END :END)] 3)) => '())


(fact "close-up-n stops changing things when it's run out of branch-stack items"
  (close-up-n [little-tree '(:END :END :END)] 99) =>
    (close-up-n [little-tree '(:END :END :END)] 3))


(fact "close-up-n works with branches"
  (new-root-helper
    (close-up-n [little-tree '(:AGAIN)] 1)) =>
      '(1 2 (3 4 (99) ()))
  (second
    (close-up-n [little-tree '(:AGAIN)] 1)) =>
      '()
  (new-root-helper
    (close-up-n [little-tree '(:AGAIN)] 10)) =>
      '(1 2 (3 4 (99) ()))


  (new-root-helper
    (close-up-n [little-tree '(:AGAIN :END :END)] 6)) =>
      '(1 2 (3 4 (99) ()))


  (new-root-helper
    (close-up-one [little-tree '(:AGAIN :AGAIN :END)])) =>
      '(1 2 (3 4 (99) ()))
  (new-node-helper
    (close-up-one [little-tree '(:AGAIN :AGAIN :END)])) =>
      nil
  (second
    (close-up-one [little-tree '(:AGAIN :AGAIN :END)])) =>
      '(:AGAIN :END)

  (new-root-helper
    (close-up-one 
      (close-up-one [little-tree '(:AGAIN :AGAIN :END)]))) =>
      '(1 2 (3 4 (99) () ()))
  (second
    (close-up-one 
      (close-up-one [little-tree '(:AGAIN :AGAIN :END)]))) =>
      '(:END)

  (new-root-helper
    (close-up-n [little-tree '(:AGAIN :AGAIN :END)] 2)) =>
      '(1 2 (3 4 (99) () ()))
  (second
    (close-up-n [little-tree '(:AGAIN :AGAIN :END)] 2)) =>
      '(:END)

  (new-root-helper
    (close-up-n [little-tree '(:AGAIN :AGAIN :AGAIN)] 6)) =>
      '(1 2 (3 4 (99) () () ()))
  (new-root-helper
    (close-up-n [little-tree '(:AGAIN :AGAIN :END)] 6)) =>
      '(1 2 (3 4 (99) () ()))
  (new-root-helper
    (close-up-n [little-tree '(:END :AGAIN :END)] 6)) =>
      '(1 2 (3 4 (99)) ()))


;; empty gene

;; simple programs, no branches

;; only branching



;; apply-one-gene-to-state

(fact "simple gene, no close-nows"
  (new-root-helper
    (apply-one-gene-to-state
      [little-tree '()]
      {:item :foo :close 0}
      {})) => '(1 2 (3 4 (99 :foo)))
  (new-node-helper
    (apply-one-gene-to-state
      [little-tree '()]
      {:item :foo :close 0}
      {})) => :foo
    )


(fact "branching gene, no close-nows"
  (new-root-helper
    (apply-one-gene-to-state
      [little-tree '()]
      {:item :foo :close 0}
      {:foo 1})) => '(1 2 (3 4 (99 :foo ())))
  (new-node-helper
    (apply-one-gene-to-state
      [little-tree '()]
      {:item :foo :close 0}
      {:foo 1})) => nil

  (new-root-helper
    (apply-one-gene-to-state
      [little-tree '()]
      {:item :foo :close 0}
      {:foo 6})) => '(1 2 (3 4 (99 :foo ())))
  (second
    (apply-one-gene-to-state
      [little-tree '()]
      {:item :foo :close 0}
      {:foo 6})) => '(:AGAIN :AGAIN :AGAIN :AGAIN :AGAIN :END)    
  )


(fact "branching gene, with close-nows"
  (new-root-helper
    (apply-one-gene-to-state
      [little-tree '()]
      {:item :foo :close 1}
      {:foo 6})) => '(1 2 (3 4 (99 :foo () ())))
  (second
    (apply-one-gene-to-state
      [little-tree '()]
      {:item :foo :close 1}
      {:foo 6})) => '(:AGAIN :AGAIN :AGAIN :AGAIN :END)   
  
  (new-root-helper
    (apply-one-gene-to-state
      [little-tree '()]
      {:item :foo :close 2}
      {:foo 6})) => '(1 2 (3 4 (99 :foo () () ())))
  (second
    (apply-one-gene-to-state
      [little-tree '()]
      {:item :foo :close 2}
      {:foo 6})) => '(:AGAIN :AGAIN :AGAIN :END)    
 
  (new-root-helper
    (apply-one-gene-to-state
      [little-tree '()]
      {:item :foo :close 99}
      {:foo 6})) => '(1 2 (3 4 (99 :foo () () () () () ())))
  (second
    (apply-one-gene-to-state
      [little-tree '()]
      {:item :foo :close 99}
      {:foo 6})) => '()    
  )


(fact "silent gene"
  (new-root-helper
    (apply-one-gene-to-state
      [little-tree '()]
      {:item :foo :close 0 :silent true}
      {})) => '(1 2 (3 4 (99))))


(fact "a silent gene closes no blocks"
  (new-node-helper
    (apply-one-gene-to-state
      [little-tree '()]
      {:item :foo :close 3 :silent true}
      {})) => 99)


(fact "a silent gene doesn't change the block-stack"
  (second
    (apply-one-gene-to-state
      [little-tree '(:AGAIN :AGAIN :END)]
      {:item :foo :close 3 :silent true}
      {:foo 8})) => '(:AGAIN :AGAIN :END))


(fact "noop_open_paren gene"
  (new-root-helper
    (apply-one-gene-to-state
      [little-tree '()]
      {:item :noop_open_paren :close 0}
      {})) => '(1 2 (3 4 (99 ())))
  (new-root-helper
    (apply-one-gene-to-state
      [little-tree '()]
      {:item :noop_open_paren :close 0 :silent true}
      {})) => '(1 2 (3 4 (99)))

  )

;; plush->push

(fact "plush->push for empty genomes"
  (plush->push [] {}) => [])


(fact "plush->push for simple genomes"
  (plush->push [{:item 1 :close 0}
                {:item 2 :close 0}
                {:item 3 :close 0}] {}) => [1 2 3])


(fact "plush->push for branching genomes"
  (plush->push [{:item :branches :close 0}
                {:item 2 :close 0}
                {:item 3 :close 0}] {:branches 2}) => '[:branches (2 3) ()]
  (plush->push [{:item :branches :close 1}
                {:item 2 :close 0}
                {:item 3 :close 0}] {:branches 2}) => '[:branches () (2 3)]
  (plush->push [{:item :branches :close 1}
                {:item 2 :close 1}
                {:item 3 :close 1}] {:branches 3}) =>
      '[:branches () (2) (3)]
  (plush->push [{:item :branches :close 1}
                {:item 2 :close 1}
                {:item :branches :close 1}] {:branches 3}) => 
                  '[:branches () (2) (:branches () () ())])



;; some acceptance tests & bug fixes

(fact "plush->push with closes up parens right"
  (plush->push
    [ {:item :foo :close 0}
      {:item 1    :close 0}
      {:item 2    :close 3}
      {:item 3    :close 0}]
    {:foo 1}) => '[:foo (1 2) 3])


(fact "plush->push example per Tom Helmuth"
  (plush->push
    [ {:item :exec-do*times   :close 0}
      {:item 8                :close 0}
      {:item 11               :close 3}
      {:item :integer-add     :close 0 :silent true}
      {:item :exec-if         :close 1}
      {:item 17               :close 0}
      {:item :noop_open_paren :close 0}
      {:item false            :close 0}
      {:item :code-quote      :close 0}
      {:item :float-mult      :close 2}
      {:item :exec-rot        :close 0}
      {:item 34.44            :close 0}]
    { :exec-do*times 1
      :exec-if       2
      :code-quote    1
      :exec-rot      3}) =>
  '[:exec-do*times (8 11) :exec-if ()
    (17 (false :code-quote (:float-mult)) :exec-rot (34.44) () ())])