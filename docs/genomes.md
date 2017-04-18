# Genomes for Push Programs

At the moment there are two representation schemes for Push programs: `plush` and `bb8`. Both of these genomic schemes address the problem of representing a tree-like Push program (tree-like because it can contain arbitrary code blocks surrounded by matching parentheses) into a linear genome that can be translated back into the same program. Each comes at this problem from a different direction, though.

The `plush` genome is concerned with where and how many parenthesized code blocks are opened and closed, in the course of a depth-first traversal of the program tree. When the linear genome is transcribed into a Push program, a buffer of _potential_ branches is created, and consumed as additional genes are translated.

By convention in the original Clojush , `plush` genomes refer to deep information about the syntax of the Push language itself, constructing branches in the program whenever instructions that would be expected to _use_ those branches appear in the code. So for example because the Push instruction `:exec-swap` uses two `:exec` arguments, the Clojush `plush` convention would be to _automatically_ and immediately open two consecutive branches following the insertion of an `:exec-swap` instruction. The implementation here permits this behavior through a backwards-compatible interface described below.

The `bb8` genome takes a more abstract and generative approach. Each `bb8` gene contains instructions that indicate how to move a "cursor" to a new position on the developing Push program tree, and how to insert a new item in that cursor's new position. Unlike `plush` genomes, the tree structure of the program is explicitly captured; branches are inserted by inserting an empty code block in the developing tree, which can then be "filled" by later transcribed genes.

- [more about `plush`](plush.md)
- [more about `bb8`](bb8.md)
