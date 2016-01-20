(ns answer-factory.genomes.plush
  (:require [clojure.zip :as zip])
  (:require [answer-factory.genomes.bb8 :as bb8]))


(defn clean-insert
  "takes a zipper, and inserts the given item at the current cursor position; if the cursor is currently in an empty sub-list, it does so by deleting the `nil` placeholder; cursor then moves to the inserted item"
  [z item]
  (if (nil? (zip/node z))
    (zip/replace z item)
    (-> z (zip/insert-right item) zip/right)))


(defn empty-program
  "returns a seq-zip built on an empty list, with the cursor advanced into the list"
  []
  (zip/next (zip/seq-zip '())))


(defn append-token
  "takes a zipper and any item; inserts the item to the right of the current cursor position, or replaces the current cursor if it is in an empty sub-list; cursor moves to the insered item"
  [z item]
  (clean-insert z item))


(defn move-up-safely
  "takes a zipper; moves the cursor up one level towards the root, or stays in the root if already there; then moves to the rightmost position in that new level"
  [program]
  (if (bb8/root? (zip/up program))
      (zip/rightmost program)
      (-> (zip/up program)
          zip/rightmost)))


(defn append-as-sibling
  "takes a zipper and an item; inserts the item as the 'cousin' of the current cursor position, moving up (bounded by root), by a new list to the right of the current sub-list and placing the item inside that new list; cursor moves to the new item"
  [z item]
  (-> (move-up-safely z)
      (clean-insert '())
      zip/down
      (clean-insert item)))


(defn begin-branch
  "takes a zipper; adds a new sub-list at the right of the current cursor position, and moves into that empty sub-list"
  [z]
  (-> (clean-insert z '())
      zip/down))


(defn last-closed-block
  "returns nil if not found, otherwise returns the zipper with the cursor moved up and then left (and with :PLACEHOLDER inserted to the right of the original cursor position)"
  [z]
  (loop [loc (move-up-safely (zip/insert-right z :PLACEHOLDER))]
    (cond
      (bb8/root? loc) nil
      (and (empty? (zip/lefts loc))
           (some? (zip/up loc))) (recur (zip/up loc))
      (and (zip/branch? loc)
           (not-any? #(= % :PLACEHOLDER) (zip/children loc))) loc
      :else (recur (zip/left loc)))))


(defn lift-sublist
  "if the cursor in the zipper holds a sub-list, it is replaced with its own children; otherwise, the zipper is returned unchanged"
  [z]
  (if (not (zip/branch? z))
    z
    (let [c (zip/children z)]
      (loop [loc       (zip/replace z (first c))
             remainder (rest c)]
        (if (empty? remainder)
          loc
          (recur (-> loc
                     (zip/insert-right (first remainder))
                     (zip/right))
                 (rest remainder)))))))


(defn undo-placeholder
  "takes a zipper which may have a (single) :PLACEHOLDER item somewhere; if present, it's removed and the cursor is left in the previous position; if absent, the zipper is returned unchanged"
  [z]
  (loop [loc (bb8/rewind z)]
    (cond
      (zip/end? loc)
        z
      (= :PLACEHOLDER (zip/node (zip/next loc)))
        (-> loc zip/right zip/remove)
      :else
        (recur (zip/next loc)))))


(defn lift-last-closed-block
  "if there is a 'last closed block' relative to the current cursor position, it is 'lifted' to its parent's level; otherwise, the zipper is returned unchanged"
  [z]
  (if (some? (last-closed-block z))
    (undo-placeholder (lift-sublist (last-closed-block z)))
    z))



(defn close-up-one
  "takes a tuple composed of a program (zipper) and a branch-stack (seq) from which it pops an item; if the branch-stack is empty, it returns the tuple unchanged; if the popped item is `:AGAIN` it moves the cursor to an empty 'sibling' sub-tree it creates; if `:END` it simply moves up a level"
  [[program branch-stack]]
  (let [outcome (first branch-stack)]
    (cond
      (nil? outcome) [program branch-stack]
      (= outcome :AGAIN)
        [(-> (move-up-safely program) begin-branch)
         (drop 1 branch-stack)]
      :else
        [(move-up-safely program)
         (drop 1 branch-stack)]
         )))


(defn close-up-n
  "takes a tuple composed of a program (zipper) and branch-stack (seq), plus a counter; iterates `close-up-one`, returning the resulting altered tuple"
  [[program branch-stack] n]
  (loop [state [program branch-stack]
         counter n]
    (if (zero? counter)
      state
      (recur (close-up-one state)
             (dec counter)))))


(defn push-n-branches
  "takes a branch-stack (seq) and a counter, and pushes that many copies of `:AGAIN` onto the stack"
  [stack n]
  (concat (repeat n :AGAIN) stack))


(defn apply-one-gene-to-state
  "takes a tuple composed of a program (zipper) and branch-stack (seq), a single Plush gene (map) and a branch-map (map) which contains information about how many branches should be opened for particular gene items; if the gene's `:silent` field evaluates to true, it does nothing"
  [[program branch-stack] gene branch-map]
  (let [item         (:item gene)
        close-nows   (:close gene)
        branches     (or (get branch-map item) 0)
        silent?      (:silent gene)]
    (if silent?
      [program branch-stack]
      (-> (cond
            (= item :noop_open_paren)
              [(begin-branch program) branch-stack]
            (= item :noop_delete_prev_paren_pair)
              [(lift-last-closed-block program) branch-stack]
            (pos? branches)
              [(-> program
                   (append-token item)
                   begin-branch)
               (-> branch-stack
                   (conj :END)
                   (push-n-branches (dec branches)))]
            :else
              [(append-token program item) branch-stack])
          (close-up-n , close-nows))
      )))


(defn plush->push
  "takes a genome (collection of Plush gene maps) and a branch-map (map), and creates a complete Push program based on the specified gene values and the branch-map directives"
  [genome branch-map]
  (loop [program   (empty-program)
         gene      (first genome)
         remainder (rest genome)
         stack     (list)]
    (if (nil? gene)
      (into [] (zip/root (first (close-up-n [program stack] (count stack)))))
      (let [[p s] (apply-one-gene-to-state
                    [program stack] gene branch-map)]
        (recur p (first remainder) (rest remainder) s)))))



