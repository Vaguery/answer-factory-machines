# `bb8` genomes

The `bb8` genome structure depends on a generalized dynamic tree-constructing algorithm to read a sequence of `gene` maps and produce a derived tree program.

The model used is closely derived from the `zipper` tree-walking data structure, with the addition of a few extra "moves". A genome is translated into a Push program (or potentially any other tree-structured object), gene-by-gene, by imagining a "cursor" sitting at the head of an empty list. Each gene specifies how to move the cursor in the tree, where to insert a new item, and what that item is supposed to be.

<!--

## rolling around

The name of the `bb8` genomic representation is a play on words on several levels. The "bb" part refers to the notion of _building blocks_ in genetic algorithms, since the design motivation for the representational scheme arose out of a desire to capture in a linear form some useful structures in a Push program that may not be contiguous in that program. But of course it is also a reference to the cute robot in _The Force Awakens_, who is always rolling around all over the place, and seems remarkably creative.

Each `bb8` gene is a map. The salient keys are

- `:from`
- `:put`
- `:item`
- `:branch?`

### `:from`

The `:from` key is used to specify one of several "moves" of a notional cursor. The values currently supported are

- any numerical value: jump the cursor to the position indexed by that number, counting items and sub-trees as individual steps in a depth-first traversal of the tree, and counting the first item in that traversal as position `0`. Non-integer indices are rounded _up_ to the next integer, and indices outside the range of the tree's current size "wrap" to fit.
- `:head` place the cursor on the first item in the tree; if the tree is empty, then it's the empty space inside the tree, not the _whole_ tree
- `:tail` move the cursor to the last item in the tree (by depth-first traversal)
- `:subhead` move to the first item within the current sub-tree
- `:append` move to the last item within the current level of sub-tree
- `:left` move one step to the left in the current sub-tree level, wrapping around to the last position in the sub-tree if we're already at the first position
- `:right` move one step to the right in the current sub-tree level, wrapping round to the first position in this level if we're already at the last item
- `:prev` move one step "back" in the entire tree's depth-first traversal, wrapping to the last item in the tree if we're already at the first
- `:next` move one step "forward" in the entire tree's depth-first traversal, wrapping to the first item in the tree if we're already at the last
- `:up` move from a position within a sub-tree to the next level up, selecting the sub-tree in which you were before; if there are no "up" levels, stay in the same location
- `:down` if the current cursor is positioned on  sub-tree, enter that sub-tree and move to the first position within it; otherwise, stay in place
- `:here` do not move the cursor

The `:from` cursor movement values only ever _move_ a cursor within an existing (possibly empty) tree. If there is no `:from` value, or if the value is `:here`, or if the cursor position cannot be moved in the indicated direction (see the list details), then the cursor stays where it is at the moment.

### `:put`

The `:put` key is used to specify whether the item being inserted will appear to the left or right of the current cursor, and it can take either `:L` or `:R` values. If the cursor is currently within an empty subtree, then both have the same effect of inserting the item as the contents of that subtree. If no `:put` value is provided, then nothing will be added to the genome.

### `:item`

The `:item` value is the thing that will be added to the growing tree. Its value isn't especially important, unless it happens to be a Clojure `seq`. The Push program being built, and by extension the `bb8` transcription of a genome, unfold as nested collection of `seq` sub-lists. Thus, if a collection _that is also a `seq`_ is added to a genome, then all the individual components of that item will become "part" of the program, and subsequent genes may insert items within it.

This offers one way for a Push program represented as a `bb8` genome to branch as it is constructed: If the `:item` is itself a `seq`, even an empty Clojure list like `'()`, then a new subtree will be created by that gene. If the `:item` is a more complicated `seq`, for example `'(1 (2 (3 (4))))`, then multiple items and subtrees will be inserted all at once by that gene's transcription.

This latter is probably not as desirable as it may seem, so while `bb8` `:item` values _can_ be collections of stuff, I would advise against the practice. It may be clearer conceptually, and will almost certainly produce more evolvable and explainable genome-to-program mappings, if you do not permit `seq` values for the `:item` field.

### `:branch?`

Instead of having a list `:item`, use the `:branch?` key in a `bb8` gene to create a ramified tree. If the `:branch?` value is `true` (or truthy), then when the `:item` is inserted, it will automatically be wrapped in a new sub-tree.

### edge cases

A gene with no (recognized) keys at all is not translated. That is, the cursor will not move, and nothing will be inserted.

A gene with no recognized `:from` field will default to a `:here` move. That is, the cursor will not move at all.

A gene with no `:put` value, or anything besides `:L` or `:R` specifically, will not alter the program, though the gene might move the cursor.

A gene that lacks an `:item` value will update the cursor position, but will _only_ insert something if `:branch?` is truthy. If that is the case, then an empty list will be inserted at the specified position.

A gene that lacks a `:branch?` key will act as though the value were `false`. -->

## some examples

`TBD`
