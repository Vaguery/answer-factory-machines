# `bb8` genomes

The `bb8` genome structure depends on a generalized dynamic tree-constructing algorithm to read a sequence of `gene` maps and produce a derived tree program.

The model used is closely derived from the `zipper` tree-walking data structure, with the addition of a few extra "moves". A genome is translated into a Push program (or potentially any other tree-structured object), gene-by-gene, by imagining a "cursor" sitting at the head of an empty list. Each gene specifies how to move the cursor in the tree, where to insert a new item, and what that item is supposed to be.

## rolling around

The name of the `bb8` genomic representation is a play on words on several levels. The "bb" part refers to the notion of _building blocks_ in genetic algorithms, since the design motivation for the representational scheme arose out of a desire to capture in a linear form some useful structures in a Push program that may not be contiguous in that program. But of course it is also a reference to the cute robot in _The Force Awakens_, who is always rolling around all over the place, and seems remarkably creative.

Each `bb8` gene is a map. The salient keys are

- `:from`
- `:put`
- `:item`
- `:branch?`

### an example

Let's walk through an example of a `bb8` genome with six genes, just to get a flavor of the algorithmic juggling going on to transform a `vector` of `bb8` genes into a Push program.

~~~ clojure
[{:from :head :put :L :item 1}
 {:from :tail :put :L :item 2 :branch? true}
 {:from :next :put :R :item 3 :branch? true}
 {:from :down :put :L :item 4}
 {:from :here :put :R :item 5}
 {:from :prev :put :R :item 6}
 {:from -4.5 :put :L :item 7 :branch? true}
 ]
~~~

We translate this genome into a Push program one gene at a time, starting from a "seed" that is an empty Clojure vector `'[]`. We'll treat this vector as the _boundaries_ of the unfolding program, and as we process each `bb8` gene we'll move around inside these boundaries and add new items.

1. We start with an empty "seed" program: `[]`. The cursor is in the only available position: right in the middle of those two square brackets. We can visualize it like this if you prefer, using «guillemets» for the cursor `[«»]`.
2. The first gene tells us to move the cursor to the `:head` position. But the program is empty, so we're already _at_ the `:head` position (and the `:tail` position, and any other position we might specify). We're told to put a `1` to the "left", which because there aren't any other positions means "where we are" in this special case. As a result, we end up with the program `[«1»]`, with the cursor on the `1`.
3. The second gene tells us to move the cursor to the `:tail`. That's the last item or subtree we'd encounter in a depth-first traversal of this program, and... well, we're already there too. Now we are going to insert `2` to the "left", but note that `:branch?` is `true`. Therefore we'll insert the subtree `(2)` instead of simply `2`. The result is `[(2) «1»]`, and notice that the cursor is _still_ on the `1`.
4. The third gene tells us to move the cursor to the `:next` position. This is the next step in a depth-first traversal of the tree, but we're already at the end, so we will _wrap_ to the first position (the `:head`) of the tree. Note that the first thing in the tree is a subtree, the one we just put there. Then to the _right_ we will insert `(3)`, producing `[«(2)» (3) 1]`.
5. The fourth gene tells us to move the cursor `:down`. It's currently pointing at a subtree, so we can actually do that. When we enter a subtree, we move to its head automatically, which in this case is the `2`. Then we'll insert `4` to the left, producing `[(4 «2») (3) 1]`
6. The fifth gene tells us to stay where we are, and put a `5` to the right. That produces `[(4 «2» 5) (3) 1]`
7. The sixth gene tells us to move to the `:prev` location. As with `:next`, this is the previous step of a depth-first traversal of the tree, which in this case means we move to the `4`. Then we insert `6` to the right of that cursor position, giving `[(«4» 6 2 5) (3) 1]`
8. The final gene tells us to `:jump` to a location indexed as `-4.5`. First, we count how many cursor locations are actually present in the current state; there are 8 (the six numerical items, plus the two subtrees present already). We calculate `(mod -4.5 8)`, which produces `3.5`, which we _round up and around_ (see below) to index `4`. Let's walk the cursor from the head (position `0`) to position `4` together: `[«(4 6 2 5)» (3) 1]`, `[(«4» 6 2 5) (3) 1]`, `[(4 «6» 2 5) (3) 1]`, `[(4 6 «2» 5) (3) 1]`, `[(4 6 2 «5») (3) 1]`. Now we are going to place `(7)` to the left, giving us `[(4 6 2 (7) «5») (3) 1]`.

The resulting program for this example `bb8` genome is therefore `[(4 6 2 (7) 5) (3) 1]`.

## The gene values

### `:from`

The `:from` key is used to specify one of several "moves" of a notional cursor. The cursor always points to items and subtrees containing items. The values currently supported are

- any numerical value: jump the cursor to the position indexed by that number, counting items and subtrees as individual steps in a depth-first traversal of the tree, and counting the first item in that traversal as position `0`. We first reduce the index value `mod` the number of cursor positions, and then round "up and around". That is, if we have 7 possible cursor positions, and the index is `13.8`, then we first take `(mod 13.8 7)` to get `6.8`, and then round "up and around": rounding up gives us an index of `7`, but since we're zero-based this becomes index `0`, or the first cursor position at the head of the tree.
- `:head` Place the cursor on the first item in the tree (by a depth-first traversal). Note: if the first item is a subtree, point at the subtree itself, not the first item inside it. If the tree is empty, then it's "pointing to" the empty space inside the tree, not the _whole_ tree
- `:tail` Move the cursor to the last item in the tree (by depth-first traversal).
- `:subhead` Move to the first item within the current subtree.
- `:append` Move to the last item within the current level of subtree.
- `:left` Move one step to the left in the current subtree level, wrapping around to the last position in the subtree if we're already at the first position.
- `:right` Move one step to the right in the current subtree level, wrapping round to the first position in this level if we're already at the last item.
- `:prev` Move one step "back" in the entire tree's depth-first traversal, wrapping to the last item in the tree if we're already at the first
- `:next` Move one step "forward" in the entire tree's depth-first traversal, wrapping to the first item in the tree if we're already at the last.
- `:up` Move from a position within a subtree to the next level up, selecting the subtree in which you were before; if there are no "up" levels, stay in the same location.
- `:down` If the current cursor is positioned on a subtree, enter that subtree and move to the first position within it; otherwise, stay in place.
- `:here` Do not move the cursor.

The `:from` cursor movement values only ever _move_ a cursor within an existing (possibly empty) tree. If there is no `:from` value, or if the value is `:here`, or if the cursor position cannot be moved in the indicated direction (see the list details), then the cursor stays where it is at the moment.

### `:put`

The `:put` key is used to specify whether the item being inserted will appear to the left or right of the current cursor, and it can take either `:L` or `:R` values. If the cursor is currently within an empty subtree, then both have the same effect of inserting the item as the contents of that subtree. If no `:put` value is provided, then nothing will be added to the genome.

### `:item`

The `:item` value is the thing that will be added to the growing tree. Its value isn't especially important, unless it happens to be a Clojure `seq`. The Push program being built, and by extension the `bb8` transcription of a genome, unfold as nested collection of `seq` sub-lists. Thus, if a collection _that is also a `seq`_ is added to a genome, then all the individual components of that item will become "part" of the program, and subsequent genes may insert items within it.

This offers one way for a Push program represented as a `bb8` genome to branch as it is constructed: If the `:item` is itself a `seq`, even an empty Clojure list like `'()`, then a new subtree will be created by that gene. If the `:item` is a more complicated `seq`, for example `'(1 (2 (3 (4))))`, then multiple items and subtrees will be inserted all at once by that gene's transcription.

This latter is probably not as desirable as it may seem, so while `bb8` `:item` values _can_ be collections of stuff, I would advise against the practice. It may be clearer conceptually, and will almost certainly produce more evolvable and explainable genome-to-program mappings, if you do not permit `seq` values for the `:item` field.

### `:branch?`

Instead of having a list `:item`, use the `:branch?` key in a `bb8` gene to create a ramified tree. If the `:branch?` value is `true` (or truthy), then when the `:item` is inserted, it will automatically be wrapped in a new subtree.

### edge cases

A gene with no (recognized) keys at all is not translated. That is, the cursor will not move, and nothing will be inserted.

A gene with no recognized `:from` field will default to a `:here` move. That is, the cursor will not move at all.

A gene with no `:put` value, or anything besides `:L` or `:R` specifically, will not alter the program, though the gene might move the cursor.

A gene that lacks an `:item` value will update the cursor position, but will _only_ insert something if `:branch?` is truthy. If that is the case, then an empty list will be inserted at the specified position.

A gene that lacks a `:branch?` key will act as though the value were `false`.

## some examples

You can follow along with some simple examples in the [midje tests](https://github.com/Vaguery/answer-factory-machines/blob/master/test/answer_factory/genome/bb8_test.clj#L880-L919) to see how `bb8` genomes get translated into programs.
