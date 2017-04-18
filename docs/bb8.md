# `bb8` genomes

The `bb8` genome structure depends on a generalized dynamic tree-constructing algorithm to read a sequence of `gene` maps and produce a derived tree program.

The model used is closely derived from the `zipper` tree-walking data structure, with the addition of a few extra "moves". A genome is translated into a Push program (or potentially any other tree-structured object), gene-by-gene, by imagining a "cursor" sitting at the head of an empty list. Each gene specifies how to move the cursor in the tree, where to insert a new item, and what that item is supposed to be.

[more TBD]
