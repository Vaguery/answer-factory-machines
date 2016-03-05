(ns answer-factory.genome.plush-test
  (:use midje.sweet)
  (:use answer-factory.genome.plush)
  (:require [clojure.zip :as zip])
  (:require [answer-factory.genome.bb8 :as bb8])
  (:use clojure.pprint))


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


(defn tree-vector-helper
  [tree-vector]
  [(zip/root (first tree-vector)) (second tree-vector)])


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


;; lift-branch

(fact "lift-branch moves only the current cursor position"
  (zip/root little-tree) => '(1 2 (3 4 (99))) ;; cursor was at «99»
  (zip/root (lift-branch little-tree)) => '(1 2 (3 4 (99))) ;; cursor is was «99»
  (zip/root (lift-branch (zip/prev little-tree))) => '(1 2 (3 4 99)) ;; cursor was at «(99)»
  (zip/root (lift-branch (-> little-tree zip/up zip/up))) =>
    '(1 2 3 4 (99)) ;; cursor was at «(3 4 (99))»
  (zip/root (lift-branch (-> little-tree zip/up zip/up zip/left))) =>
    '(1 2 (3 4 (99))) ;; cursor was at «2»
    )


;; delete-prev-paren-pair

(fact "delete-prev-paren-pair leaves the cursor in the last position but one"
  (delete-prev-paren-pair (zip/seq-zip '())) => (bb8/fast-forward (zip/seq-zip '()))
  (delete-prev-paren-pair (zip/seq-zip '(1 2 3 ()))) =>
    (bb8/fast-forward (zip/seq-zip '(1 2 3 ())))

  ;; just to make sure
  (zip/root (zip/replace (delete-prev-paren-pair (zip/seq-zip '(1 2 3 ()))) 9999)) =>
    '(1 2 3 (9999))
  (zip/root (clean-insert (bb8/fast-forward (zip/seq-zip '(1 2 3 ()))) 9999)) =>
    '(1 2 3 (9999))

  (zip/root (delete-prev-paren-pair 
    (zip/prev (bb8/fast-forward (zip/seq-zip '(1 (2) 3 ())))))) => '(1 2 3 ())
  (zip/root (delete-prev-paren-pair 
    (zip/prev (bb8/fast-forward (zip/seq-zip '(1 ((2)) 3 ())))))) => '(1 (2) 3 ())
  (zip/root (delete-prev-paren-pair 
    (zip/prev (bb8/fast-forward (zip/seq-zip '((1 (((2))) 3) ())))))) => '(1 (((2))) 3 ())


  (zip/root (delete-prev-paren-pair 
    (zip/prev (bb8/fast-forward (zip/seq-zip '(1 2 3 4 5)))))) => '(1 2 3 4 5))


;; apply-one-gene-to-state

(fact "simple gene, no close-nows"
  (new-root-helper
    (apply-one-gene-to-state
      [little-tree '()]
      {:item :foo :close 0})) => '(1 2 (3 4 (99 :foo)))
  (new-node-helper
    (apply-one-gene-to-state
      [little-tree '()]
      {:item :foo :close 0})) => :foo)


(fact "simple gene, no close-nows, some open-now"
  (tree-vector-helper
    (apply-one-gene-to-state
      [little-tree '()]
      {:item :foo :open 0 :close 0})) => '[(1 2 (3 4 (99 :foo))) ()]
  (tree-vector-helper
    (apply-one-gene-to-state
      [little-tree '()]
      {:item :foo :open 1 :close 0})) => '[(1 2 (3 4 (99 :foo ()))) (:END)]
  (tree-vector-helper
    (apply-one-gene-to-state
      [little-tree '()]
      {:item :foo :open 2 :close 0})) => '[(1 2 (3 4 (99 :foo ()))) (:AGAIN :END)]
  (tree-vector-helper
    (apply-one-gene-to-state
      [little-tree '()]
      {:item :foo :open 5 :close 0})) => 
        '[(1 2 (3 4 (99 :foo ()))) (:AGAIN :AGAIN :AGAIN :AGAIN :END)])


(fact "genes lacking :item fields do not insert `nil` in the program"
  (tree-vector-helper
    (apply-one-gene-to-state
      [little-tree '()]
      {:open 0 :close 0})) => '[(1 2 (3 4 (99))) ()]
  (tree-vector-helper
    (apply-one-gene-to-state
      [little-tree '()]
      {:open 1 :close 0})) => '[(1 2 (3 4 (99 ()))) (:END)]
  (tree-vector-helper
    (apply-one-gene-to-state
      [little-tree '()]
      {:open 5 :close 2})) => '[(1 2 (3 4 (99 () () ()))) (:AGAIN :AGAIN :END)])



(fact "simple gene, some close-nows, some open-now"
  (tree-vector-helper
    (apply-one-gene-to-state
      [little-tree '()]
      {:item :foo :open 0 :close 2})) => '[(1 2 (3 4 (99 :foo))) ()]
  (tree-vector-helper
    (apply-one-gene-to-state
      [little-tree '()]
      {:item :foo :open 1 :close 2})) => ' [(1 2 (3 4 (99 :foo ()))) ()]
  (tree-vector-helper
    (apply-one-gene-to-state
      [little-tree '()]
      {:item :foo :open 2 :close 2})) =>
        '[(1 2 (3 4 (99 :foo () ()))) ()]
  (tree-vector-helper
    (apply-one-gene-to-state
      [little-tree '()]
      {:item :foo :open 5 :close 2})) => 
        '[(1 2 (3 4 (99 :foo () () ()))) (:AGAIN :AGAIN :END)])



(fact "branching gene, no close-nows: note no :open field is present!"
  (tree-vector-helper
    (apply-one-gene-to-state
      [little-tree '()]
      {:item :foo :close 0}
      :branch-map {:foo 1})) => '[(1 2 (3 4 (99 :foo ()))) (:END)]

  (tree-vector-helper
    (apply-one-gene-to-state
      [little-tree '()]
      {:item :foo :close 0}
      :branch-map {:foo 6})) =>
        '[(1 2 (3 4 (99 :foo ()))) (:AGAIN :AGAIN :AGAIN :AGAIN :AGAIN :END)])



(fact "branching gene, with close-nows: note no :open gene is present!"
  (tree-vector-helper
    (apply-one-gene-to-state
      [little-tree '()]
      {:item :foo :close 1}
      :branch-map {:foo 6})) =>
        '[(1 2 (3 4 (99 :foo () ()))) (:AGAIN :AGAIN :AGAIN :AGAIN :END)]
  
  (tree-vector-helper
    (apply-one-gene-to-state
      [little-tree '()]
      {:item :foo :close 2}
      :branch-map {:foo 6})) => 
        '[(1 2 (3 4 (99 :foo () () ()))) (:AGAIN :AGAIN :AGAIN :END)]
 
  (tree-vector-helper
    (apply-one-gene-to-state
      [little-tree '()]
      {:item :foo :close 99}
      :branch-map {:foo 6})) => '[(1 2 (3 4 (99 :foo () () () () () ()))) ()])



(fact "explicit :open trumps implicit"

  ;; without an explicit gene
  (tree-vector-helper
    (apply-one-gene-to-state
      [little-tree '()]
      {:item :foo :close 99}
      :branch-map {:foo 6})) => '[(1 2 (3 4 (99 :foo () () () () () ()))) ()]

  ;; with an explicit gene
  (tree-vector-helper
    (apply-one-gene-to-state
      [little-tree '()]
      {:item :foo :open 2 :close 99}
      :branch-map {:foo 6})) => '[(1 2 (3 4 (99 :foo () ()))) ()])


(fact "silent gene"
  (tree-vector-helper
    (apply-one-gene-to-state
      [little-tree '(:AGAIN :AGAIN :END)]
      {:item :foo :close 0 :silent true})) => '[(1 2 (3 4 (99))) (:AGAIN :AGAIN :END)])


(fact "a silent gene closes no blocks"
  (tree-vector-helper
    (apply-one-gene-to-state
      [little-tree '()]
      {:item :foo :close 3 :silent true})) => (tree-vector-helper [little-tree '()]))


(fact "a silent gene opens no blocks"
  (tree-vector-helper
    (apply-one-gene-to-state
      [little-tree '()]
      {:item :foo :open 3 :silent true})) => (tree-vector-helper [little-tree '()]))


(fact "a silent gene doesn't change the block-stack"
  (second
    (apply-one-gene-to-state
      [little-tree '(:AGAIN :AGAIN :END)]
      {:item :foo :close 3 :silent true}
      :branch-map {:foo 8})) => '(:AGAIN :AGAIN :END))


(fact "noop_open_paren gene"
  (new-root-helper
    (apply-one-gene-to-state
      [little-tree '()]
      {:item :noop_open_paren :close 0})) => '(1 2 (3 4 (99 ())))
  (new-root-helper
    (apply-one-gene-to-state
      [little-tree '()]
      {:item :noop_open_paren :close 0 :silent true})) => '(1 2 (3 4 (99)))




  )

;; plush->push

(fact "plush->push for empty genomes"
  (plush->push []) => [])


(fact "plush->push for simple genomes"
  (plush->push [{:item 1 :close 0}
                {:item 2 :close 0}
                {:item 3 :close 0}]) => [1 2 3])


(fact "plush->push for branching genomes"
  (plush->push [{:item :branches :close 0}
                {:item 2 :close 0}
                {:item 3 :close 0}] :branch-map {:branches 2}) => '[:branches (2 3) ()]
  (plush->push [{:item :branches :close 1}
                {:item 2 :close 0}
                {:item 3 :close 0}] :branch-map {:branches 2}) => '[:branches () (2 3)]
  (plush->push [{:item :branches :close 1}
                {:item 2 :close 1}
                {:item 3 :close 1}] :branch-map {:branches 3}) =>
      '[:branches () (2) (3)]
  (plush->push [{:item :branches :close 1}
                {:item 2 :close 1}
                {:item :branches :close 1}] :branch-map {:branches 3}) => 
                  '[:branches () (2) (:branches () () ())])


;; derived-push-branch-map

(fact "derived-push-branch-map is a thing"
  (keys derived-push-branch-map) => (contains :code-quote))

;; some acceptance tests & bug fixes


(def helmuth-1
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
    {:item 34.44            :close 0}])



(fact "plush->push with closes up parens right"
  (plush->push
    [ {:item :foo :close 0}
      {:item 1    :close 0}
      {:item 2    :close 3}
      {:item 3    :close 0}]
    :branch-map {:foo 1}) => '[:foo (1 2) 3])


(fact "plush->push example per Tom Helmuth"
  (plush->push
    helmuth-1
    :branch-map { :exec-do*times 1
                  :exec-if       2
                  :code-quote    1
                  :exec-rot      3}) =>
  '[:exec-do*times (8 11) :exec-if ()
    (17 (false :code-quote (:float-mult)) :exec-rot (34.44) () ())])


(fact "plush->push for noop_delete_prev_paren_pair genes"
  (plush->push
    (conj helmuth-1 {:item :noop_delete_prev_paren_pair :close 0})
    :branch-map { :exec-do*times 1
                  :exec-if       2
                  :code-quote    1
                  :exec-rot      3}) =>
  ; '[:exec-do*times (8 11) :exec-if ()
  ;   (17 (false :code-quote (:float-mult)) :exec-rot (34.44) () ())])
  ;                                                   «34.44» cursor
  ;       «false :code-quote (:float-mult)»                   previous block
    '[:exec-do*times (8 11) :exec-if () 
      (17  false :code-quote (:float-mult) :exec-rot (34.44) () ())]
      )
                                             


;; :item-less genomes can build trees


(fact "item-less plush genomes can be translated into trees"
  (plush->push [{} {} {} {}]) => []
  (plush->push [{:open 3 :close 2} {:item 2}]) => '[() () (2)]
  (plush->push [{:open 2} {:open 1 :close 1} {:open 1} {:close 2} {:open 1}]) => '[(() ()) (())])


;; derived-push-branch-map (the default)


(fact "a backwards-compatible :branch-map is available"
  (plush->push []) => []
  (plush->push [{:item 1 :close 0}
                {:item 2 :close 0}
                {:item 3 :close 0}]
                :branch-map derived-push-branch-map) => [1 2 3]
  (plush->push [{:item :code-quote :close 0}
                {:item 2 :close 0}
                {:item 3 :close 0}]
                :branch-map derived-push-branch-map) => '[:code-quote (2 3)]
  (plush->push [{:item :code-quote :close 0}
                {:item 2 :close 1}
                {:item 3 :close 0}]
                :branch-map derived-push-branch-map) => '[:code-quote (2) 3]
  (plush->push [{:item :code-quote :close 1}
                {:item 2 :close 0}
                {:item 3 :close 0}]
                :branch-map derived-push-branch-map) => '[:code-quote () 2 3]
  (plush->push [{:item :exec-rotate :close 0}
                {:item :exec-rotate :close 0}]
                :branch-map derived-push-branch-map) => '[:exec-rotate (:exec-rotate () () ()) () ()])


;;; push->plush


(future-fact "push->plush will eventually work"
  (push->plush []) => []
  (push->plush [1 (2 (3) (4))] =>
    [ {:item 1 :open 1}
      {:item 2 :open 2}
      {:item 3 :close 1}
      {:item 4}
    ]))



;;; facts for README.md


(fact "an empty genome produces an empty program"
  (plush->push []) => '[])


(fact "an genome with genes containing only :items produces a linear program, without a :branch-map"
  (plush->push [
    {:item 1}
    {:item 2}
    {:item 3}]) => '[1 2 3])


(fact "a genome with :open values produces a branched program"
  (plush->push [
    {:item 1 :open 1}
    {:item 2 :open 0}
    {:item 3 :open 1}]) => '[1 (2 3 ())]
  (plush->push [
    {:item 1 :open 0}
    {:item 2 :open 1}
    {:item 3 :open 1}]) => '[1 2 (3 ())]
  (plush->push [
    {:item 1 :open 2}
    {:item 2 :open 0}
    {:item 3 :open 0}]) => '[1 (2 3) ()])


(fact "a gene with :close values immediately closes open and pending branches"
  (plush->push [
    {:item 1 :open 1 :close 1}
    {:item 2 :open 0}
    {:item 3 :open 1}]) => '[1 () 2 3 ()] ;; compare with above
  (plush->push [
    {:item 1 :open 0 :close 1}
    {:item 2 :open 1 :close 1}
    {:item 3 :open 1}]) => '[1 2 () 3 ()]
  (plush->push [
    {:item 1 :open 2}
    {:item 2 :open 0 :close 1}
    {:item 3 :open 0}]) => '[1 (2) (3)])


(fact ":open and :close values are enough to build branching trees"
  (plush->push [
    {:open 1 :close 1}
    {:open 0}
    {:open 1}]) => '[() ()] ;; compare with above
  (plush->push [
    {:open 0 :close 1}
    {:open 1 :close 1}
    {:open 1}]) => '[() ()]
  (plush->push [
    {:open 2}
    {:open 1 :close 1}
    {:open 0}]) => '[(()) ()]
  (plush->push [
    {:open 4}
    {:open 4 :close 1}
    {:open 4}
    {:item 1}]) => '[(() ((1) () () ()) () ()) () () ()])


(fact "translating with a `:branch-map` argument creates default `:open` values for specified `:item` values"
  (plush->push [{:item :foo} {:item :bar} {:item :baz}]
    :branch-map {:foo 2 :baz 2}) => '[:foo (:bar :baz () ()) ()]
  (plush->push [{:item :foo} {:item :foo} {:item :foo}]
    :branch-map {:foo 2}) => '[:foo (:foo (:foo () ()) ()) ()])

(fact "a :branch-map's default can be overridden explicitly in a gene"
  (plush->push [{:item :foo}
                {:item :bar} 
                {:item :baz}]
    :branch-map {:foo 2 :baz 2}) => '[:foo (:bar :baz () ()) ()]
  (plush->push [{:item :foo :open 0} 
                {:item :bar :open 3}
                {:item :baz}]
    :branch-map {:foo 2 :baz 2}) => '[:foo :bar (:baz () ()) () ()])


(fact "a 'readymade' `:branch-map` exists, analogous to Clojush's translation behavior"
  (plush->push [{:item :exec-swap} {:item 3 :close 1} {:item 2 :close 1}]
    :branch-map answer-factory.genome.plush/derived-push-branch-map) =>
      '[:exec-swap (3) (2)])


(fact ":noop_delete_prev_paren_pair"
  (plush->push [
    {:item 1 :open 1 :close 1}
    {:item 2 :open 0 :close 1}
    {:item 3 :open 1}]) => '[1 () 2 3 ()]
  (plush->push [
    {:item :noop_delete_prev_paren_pair}
    {:item 1 :open 1 :close 1}
    {:item 2 :open 0 :close 1}
    {:item 3 :open 1}]) => '[1 () 2 3 ()]
  (plush->push [
    {:item 1 :open 1 :close 1}
    {:item 2 :open 0 :close 1}
    {:item :noop_delete_prev_paren_pair}
    {:item 3 :open 1}]) => '[1 2 3 ()]
  (plush->push [
    {:item 1 :open 1 :close 1}
    {:item 2 :open 0 :close 1}
    {:item 3 :open 1}
    {:item :noop_delete_prev_paren_pair}
    ]) => '[1 2 3 ()])


(fact ":noop_open_paren"
  (plush->push [
    {:item :noop_open_paren}
    {:item 1 :open 1 :close 1}
    {:item 2 :open 0 :close 1}
    {:item 3 :open 1}]) => '[(1 () 2 3 ())]
  (plush->push [
    {:item 1 :open 1 :close 1}
    {:item 2 :open 0 :close 1}
    {:item :noop_open_paren}
    {:item 3 :open 1}]) => '[1 () 2 (3 ())]
  (plush->push [
    {:item 1 :open 1 :close 1}
    {:item 2 :open 0 :close 1}
    {:item 3 :open 1}
    {:item :noop_open_paren}
    ]) => '[1 () 2 3 (())])
