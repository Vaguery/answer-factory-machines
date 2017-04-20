# `plush` genomes

The `plush` genome implemented here is an extension of that used in the [Clojush system](https://github.com/lspector/Clojush) from Lee Spector's lab at Hampshire College. In changing the representation and translation schemes, I've tried to maintain backwards-compatibility, but the Klapaucius Push dialect is no longer much like the Push found in Clojush, so be wary of breaking changes if you are importing Clojush genomes.

A `plush` genome is simply a vector of `plush` genes.

[TBD]

## `plush` genes

A `plush` gene is a Clojure hash-map consisting of several key-value pairs, _all optional_:

1. `:item`: The value should be a Push literal to be inserted in the current cursor position in the program, or one of a few special _editing commands_ (see below). Any `:item` that is not recognized as an editing command will be inserted into the program as found, unless it is `nil`.
2. `:open`: The value should be a positive integer which indicates how many branches should be _opened_ immediately following the current insertion point. For example, `{:item :x :open 1}` will insert `:x` _and_ an empty list at the current cursor position. `{:item :x :open 3}` will insert `:x` and an empty list, and set the translator up so that by the time translation is complete, a total of three lists will be inserted consecutively following the `:x` in the resulting program.
3. `:close`: The value should be a positive integer which indicates how many "pending" and current branches will be _closed_ after the `:item` is inserted and any new branches created have been queued. For example, `{:item :x :open 2 :close 1}` will result in the insertion of `:x`, followed by an empty list (opened and then closed), and the beginning of a second list. Detailed examples are shown below, since I know this seems confusing.
4. `:silent?`: The value should be a `true` or `false`, indicating whether this gene should be processed or not. If `true`, no `:item` is inserted and no change to the branching state of the program is made: the entire gene is silenced.

Of these, _none_ are strictly required. If `:item` is missing or `nil`, the gene can still modify the tree structure via the `:open` and `:close` fields. If `:open` is missing, then its value is assumed to be 0. If `:close` is missing, its value is also inferred to be 0. If `:silent?` is missing, its value is inferred to be `false`.

**NOTE:** In the original Clojush `plush` genome scheme used implementation-specific information about the Push language to _implicitly_ calculate the `:open` value for each item. Specifically, the `:open` value was assumed to be _the number of items from the `:exec` stack used by an instruction, or 0 otherwise_. In the `plush` scheme, backwards-compatibility is available simply by setting the `:open` value _explicitly_ to the same numerical value, and leaving it 0 or absent otherwise. That is, for each gene where the `:item` is a Push instruction, set an `:open` value equal to the number of `:exec` argument used by that instruction (which can be obtained via the helper function `answer-factory.genome.plush/derived-push-branch-map`).


## editing commands

There are two `plush`-specific editing commands, which can appear as values in the `:item` field:

- `:noop_delete_prev_paren_pair`: This will "lift up" the contents of the last closed code block inserted (and closed), backtracking from the current cursor position.
- `:noop_open_paren`: This is included for backwards-compatibility with Clojush's `plush` genome translator. It behaves exactly like the gene `{:open 1}`.

## using a `:branch-map` argument

The `plush->push` function can accept an optional `:branch-map` argument, which should be a hash-map where the keys are one or more `:item` values, and the values are non-negative integers. The numeric values are used as a sort of "default" for the `:open` field of all genes in which the key is the `:item`. So for example when calling `(plush->push [...genome...] :branch-map {:exec-dup 1 false 2}` Every occurrence of a gene with `{:item :exec-dup}` will by default also include `{:open 1}`, and every gene with `{:item false}` will by default also include `{:open 2}`.

When no `:branch-map` is specified, the _default_ `:open` value for every item is assumed to be 0. Whenever a gene _explicitly_ specified an `:open` value, the default value in the `:branch-map` will be _overridden_ for that gene, but will still apply to genes with the same `:item` and no explicit `:open` value elsewhere in the genome.

## Some examples might be good

I've included some explicit examples in the form of runnable [Midje `facts`](https://github.com/Vaguery/answer-factory-clj/blob/master/test/answer_factory/genome/plush_test.clj#L540-L666), which show explicit examples of all this behavior.
