(ns design-spikes.whole-stack-fitness
  (:use midje.sweet)
  )

;; So here we're going to find the "best" results in an entire stack of values, using multiobjective sorting to do it.


;; Let me make a random stack of values:

(defn some-number-stack
  [size]
  (take size (repeatedly #(rand-int 1000))))

;; Here's a stack of numbers I got using that code, which I've saved for stability:

(def my-stack
  '(998 333 672 752 658 226 153 484 805 120
    937 936 323 805 543 161 740 314 100 353))


;; For this first step, let's assume the goal is to return 0. In general, for scores returning continuous-valued or discrete numerical scores with sufficient resolution (hold that thought), we can simply generalize this approach to apply to the _error_ associated with each value in the stack, not the value itself. WLOG and so forth.

;; Now we are going to calculate a _vector_ of two scores for each item in the stack: The first its error (here its value), the second its _depth_ in the stack.

(defn error-stack-to-sorting-vectors
  [stack]
  (map-indexed #(vector %1 %2) stack))

; (println (error-stack-to-sorting-vectors my-stack))


; ([0 998] [1 333] [2 672] [3 752] [4 658] [5 226] [6 153] [7 484] [8 805] [9 120] [10 937] [11 936] [12 323] [13 805] [14 543] [15 161] [16 740] [17 314] [18 100] [19 353])

;; we need a simple dominated-by? predicate

(defn dominated-by?
  "takes two vectors of the same size, and returns `true` if the first is dominated by the second."
  [scores1 scores2]
  (cond
    (not-every? number? scores1) false
    (not-every? number? scores2) false
    :else (let [delta (map compare scores1 scores2)]
              (and (boolean (some pos? delta))
                   (not-any? neg? delta)))))


(fact "dominated-by? works as expected"
  :prototype
  (dominated-by? [3 2] [1 1]) => true
  (dominated-by? [3 2] [1 5]) => false
  (dominated-by? [1 2] [1 5]) => false
  (dominated-by? [1 5] [1 2]) => true
  )



(defn filter-out-dominated
  "Takes a vector and a collection of other vectors, and returns the subset of the collection that is not dominated by the first one."
  [checker scores]
  (remove #(dominated-by? % checker) scores))


(fact "filter-out-dominated works as expected"
  :prototype
  (filter-out-dominated [3 2] '([1 1] [1 5] [9 9] [3 3])) => '([1 1] [1 5])
  (filter-out-dominated [3 3] '([1 1] [1 5] [9 9] [3 2])) => '([1 1] [1 5] [3 2])
  (filter-out-dominated [0 0] '([1 1] [1 5] [9 9] [3 2])) => '()
  (filter-out-dominated [9 9] '([1 1] [1 5] [9 9] [3 2])) => '([1 1] [1 5] [9 9] [3 2])
  )


(defn nondominated-selection
  "Takes a collection of vectors, and returns those that are nondominated by one another."
  [scores]
  (reduce
    (fn [survivors checker] (filter-out-dominated checker survivors))
    scores
    scores))


(fact "nondominated-selection works as expected"
  :prototype
  (nondominated-selection '([1 1] [1 5] [9 9] [3 3])) => '([1 1])
  (nondominated-selection '([3 1] [1 5] [9 9] [3 3])) => '([3 1] [1 5])
  (nondominated-selection '([3 1] [1 5] [3 3] [3 3])) => '([3 1] [1 5])
  (nondominated-selection '([3 1] [1 5] [3 1] [3 3])) => '([3 1] [1 5] [3 1])
  (nondominated-selection '([3 1] [2 2] [1 3] [0 4])) => '([3 1] [2 2] [1 3] [0 4])
  )


;; OK! Now we have all we need to determine the "best score" of a whole stack of error values:


(fact
  :prototype
  (nondominated-selection
    (error-stack-to-sorting-vectors my-stack)) =>
    '([0 998] [1 333] [5 226] [6 153] [9 120] [18 100]))


;; That's the set of 6 [error] numbers in the stack, which are mutually non-dominated on both stack depth and error. You can see that the lower-error values are located down farther in the stack, and the top number is included. The top number will _always_ be included, since no other item can _ever_ be higher and it can thus never be strictly dominated.

;; Notice what that means: The "normal result" from a stack, the top value, will _always_ be part of the set of nondominated items, _no matter what its score is_, because it's the top item! Think of this approach as an "amelioration" of the strictness of the original approach, not a strange replacement.

;; How should we interpret them? 18 layers down a pretty good answer appears... but we wouldn't normally count it as being "the answer". And it could be any old number that happened to be close to the right one. Doesn't this just reward programs that return loads of random guesses?

;; Let's look.


;; First I'll summarize that wordy function in a smaller one, called "mo-bests":
;
; (defn mo-bests
;   [stack]
;   (nondominated-selection (error-stack-to-sorting-vectors stack)))
;
; ;; Let's start with a sample of 1000 stacks, each of 100 numbers each (just to make it interesting).
;
; (def many-stacks
;   (take 1000 (repeatedly #(some-number-stack 100))))
;
;
; (def many-winners
;   (map mo-bests many-stacks))
;
; ;; How many of them are there? Let's count.
;
; ; (println (into (sorted-map) (frequencies (map count many-winners))))
;
; ;; {1 10, 2 62, 3 126, 4 192, 5 220, 6 186, 7 105, 8 63, 9 26, 10 7, 11 1, 12 1, 13 1}n
;
;
; ;; That's a pretty squashed distribution, when you think about it. I mean, there are 100 numbers, so it must be _super_ unlikely that all the numbers end up in reverse order on the list. Indeed, if you think about it and assume that each score/number here is unique value, the whole list is very close to being a random permutation of 100 numbers. Do the math and it'll be clear why there are so few non-dominated values here.
;
; ;; Once again, remember these are supposed to be numbers appearing in a _single stack_ of a single program run. They're not individuals, they're the behavior of one individual, sampled at some end point.
;
; ;; How might we select one vs another? Let's look at two of them next to one another:
;
; (println (first many-stacks))
; (println (second many-stacks))
;
;
; ;; here's the values I see:
;
;
; (def alice '(838 446 587 697 781 577 308 625 946 25 122 580 711 5 527 350 281 854 271 789 149 501 22 852 556 849 604 358 54 186 964 535 656 85 964 835 680 126 700 68 322 146 542 388 202 692 253 289 598 779 588 328 882 78 544 945 668 156 975 430 566 457 676 745 12 324 103 672 539 423 878 505 41 646 846 280 32 696 961 380 611 306 536 200 915 261 375 421 82 193 188 387 356 389 223 997 838 733 455 529))
;
; (def bob '(483 229 833 794 419 481 13 84 335 922 717 425 672 546 383 774 641 410 37 899 602 153 376 897 208 398 914 437 802 924 657 99 946 158 175 411 654 682 145 473 832 100 393 397 685 856 218 127 338 330 319 595 758 788 952 589 100 610 42 130 989 521 857 684 883 427 486 975 573 884 844 764 111 104 87 510 904 932 389 406 505 151 750 969 674 62 118 976 606 290 427 272 838 379 281 679 233 860 927 7))
;
;
; (def alice-bests (mo-bests alice))
;
; ;; '([0 838] [1 446] [6 308] [9 25] [13 5])
;
; (def bob-bests   (mo-bests bob))
;
; ;; '([0 483] [1 229] [6 13] [99 7])
;
;
; ;; Which of Alice vs Bob is "better"? Alice has a low score of 5, but Bob has a lower top score, so he would "win" in a traditional competition.
;
; ;; What if I plot the points? What does that look like?
;
; ;; [plot]
;
; ;; OK so here's an idea to explore: What happens to these lists when I add items from one to the other?
;
; (println (nondominated-selection (concat alice-bests bob-bests)))
;
; ;; ([13 5] [0 483] [1 229] [6 13])
; ;;    A       B       B      B
;
;
; ;; Hmm. I'm not sure how to interpret that. How about this?
;
;
; (println (nondominated-selection
;   (concat (error-stack-to-sorting-vectors alice)
;           (error-stack-to-sorting-vectors bob))))
;
; ;; ([13 5] [0 483] [1 229] [6 13])
;
; ;; Oooh, same thing! I guess that makes sense. It doesn't matter if you do the domination tournament first or last.
;
; ;; On the face of it, it feels like Bob wins over Alice.
;
; ;; So if we can count the number of items in a _joint_ mo-best list, compared to the items in each individual's list, what sort of behavior do we see?
;
;
; (defn sample-scores
;   []
;   (let [alice   (some-number-stack 100)
;         bob     (some-number-stack 100)
;         charlie (some-number-stack 100)
;         mo-a    (mo-bests alice)
;         mo-b    (mo-bests bob)
;         mo-c    (mo-bests charlie)
;         all     (nondominated-selection (concat mo-a mo-b mo-c))]
;     {:alice {:mo (count mo-a) :wins (count (filter (set all) mo-a)) :best (first alice)}
;      :bob   {:mo (count mo-b) :wins (count (filter (set all) mo-b)) :best (first bob)}
;      :chuck {:mo (count mo-c) :wins (count (filter (set all) mo-c)) :best (first charlie)}}))
;
;
;
; (println (sample-scores))
;
; ;; here's the sort of thing I get:
;
; ;; {:alice {:mo 4, :wins 0, :best 120},
; ;; :bob    {:mo 3, :wins 3, :best 75},
; ;; :chuck  {:mo 4, :wins 0, :best 698}}
;
;
; ;; Interestingly, it feels as if the score of the ":best" (the top item on a given stack) is correlated with the number of wins... but not 100%. I think we should investigate.
;
;
; (def buncha-triples (take 100 (repeatedly #(sample-scores))))
;
; (doseq [x buncha-triples]
;   (println (str (get-in x [:alice :best]) ","
;                 (get-in x [:bob   :best]) ","
;                 (get-in x [:chuck :best]) ","
;                 (get-in x [:alice :wins]) ","
;                 (get-in x [:bob   :wins]) ","
;                 (get-in x [:chuck :wins]) ","
;                 (first (sort-by #(- (get-in x [% :wins])) [:alice :bob :chuck])) ","
;                 (first (sort-by #(get-in x [% :best]) [:alice :bob :chuck]))
;                   )))
;
;
;
