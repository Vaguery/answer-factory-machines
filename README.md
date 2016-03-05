# answer-factory

A platform for generative programming.

### Status

Not now.

### Design goals

Most Genetic Programming platforms depend on very tight coupling between the _representation language_ (used to express potential solutions to one's problem of interest) with the _search language_ that implements the process of search _among_ those potential solutions. Here I'm attempting to explicitly divorce search dynamics and the comparison of potential solutions' behavior from their intrinsic structure.

I've started with \([a new implementation of](https://github.com/Vaguery/klapaucius)\) Lee Spector's Push language as a default. Push is qualitatively unlike most tree-based GP representations people are familiar with: quite a bit more expressive and extensible. Eventually I plan on including support for arbitrary representation schemes, including Cartesian GP, Grammatical Evolution and other less well-known approaches that have many strengths but which are harder to find in usable (and interoperable) implementations.

Even beyond this tendency for representational lock-in, most extant genetic programming systems hard-code the outdated "biologically inspired" model of crossover-and-mutation search, usually based on decades-old sketches which the active research field has for the most part abandoned. "Genetic" programming has for many years invoked a far more diverse suite of complex search operators, heuristics and design patterns that have little to do with biological dynamics, in service of actually finding interesting solutions to problems and of addressing the formal structure of practical problem-solving. Most diagrams of "programs" as DNA-like lines (or S-expression trees) that are cut and then crossed over fall short of what actually happens in effective modern GP systems, which can use hill-climbing, other machine learning algorithms, and modern statistical methods in support of discovery.

It should be as easy to try simple hillclimbing with mutation on your problem as it is to throw the kitchen sink at it. Here I'll be abstracting search dynamics into what I hope is a suite of representation-agnostic (or representation-specific) "stages", so simple approaches to GP search can be tried before the more ornate special-purpose ones are invoked.

One of the most antiquated aspects of Genetic Programming as it's often approached is the notion of _fitness_. Just as basic genetic algorithms, with their simple crossover and mutation operations on linear genomes, are no longer representative of modern GP, simple "fitness functions" based on aggregate statistics are rarely the most useful approach for finding interesting results—not least because numerical optimization should  _never_ the goal of GP search.

Coevolutionary systems, multiobjective problem-solving, dynamic or subjective evaluations, and disaggregated contingent "fitness" (like Spector's Lexicase selection) are much more common and useful in practice. These many "fitnesses" (which I'll call _rubrics_ here) can be used as a much more diverse and complex toolkit than the brute force summary of summed-squared-error. They serve as both our _sensors_ and our _actuators_ for controlling the dynamics of GP search, and so I'll be supporting that diversity as much as possible.

The field of machine learning has advanced tremendously in the last few years, and the traditional association of GP with machine learning practice has suffered. Rightly so: GP is a _terrible_ machine learning approach, at least when you define "machine learning" as the numerical optimization of statistical models based on sound first-principles models. One applies most ML algorithms knowing ahead of time what particular _aspect_ of the data is being addressed, and what well-posed numerical quantities are being minimized. A machine learning algorithm is something you set up, and apply to your dataset as a _step_ in some larger question-answering context: What structure does this oracle expose? What did people buy on our website that you might also want to buy? Where do extra eyeballs appear on the dog when I use this training data for deep learning?

GP (whether you think of it as "genetic programming" or "generative" as I prefer) is much better applied as an _exploratory_ system, something used for "machine discovery" not "machine learning". One uses machine learning algorithms to capture stylized decisions in easily-maintained production code. One uses genetic programming to _explore how to even begin_. GP may be "machine learning" in a historical sense, or because it does things to data with computers, but in a practical sense it doesn't even try to do what most ML things do: it is a search for structures—algorithms, designs, inventions, rules, proofs, models—not a way of _optimizing_ one structure in a given context. The nerdiest way I can think of describing it for computer folks is that GP should be thought of as _higher-order functions over machine learning_.

### Status

This is still in early development. The first release will support single-machine searches only, though it will use a local database for long-term and large-scale storage. But because of Clojure's strong support for distributed and asynchronous programming, a distributed workers-and-work-queue version will follow shortly thereafter.

#### Working

- two genome types for Push programs, `plush` (from Lee Spector's lab) and `bb8` \(a new representation scheme relying on Clojure's [`zipper` library](https://clojure.github.io/clojure/clojure.zip-api.html)\)
- selection operators for
  - `uniform-selection` pick one randomly sampled `Answer` from a collection
  - `uniform-cull` remove one randomly sampled `Answer` from a collection
  - `simple-selection` return all the best-scoring `Answers` from a collection, given a specified `Rubric`
  - `simple-cull` remove all the worst-scoring `Answers` from a collection, given a specified `Rubric`
  - `lexicase-selection` randomly permute a collection of `Rubrics`, and recursively retain the best-scoring `Answers` on each one (selectedin turn). For example, if the `Rubrics` are numbered `[1 2 3]`, it might randomly permute these to `[3 1 2]`; the resulting subset of `Answers` are obtained by selecting the best at `Rubric` 3, then the subset of those best at `Rubric` 1, then the subset of _those_ best at `Rubric` 2.
  - `lexicase-cull` removes the "winners" of `lexicase-selection` applied to _negated_ scores: every numerical score is negated, and `lexicase-selection` is applied to determine which `Answers` to _remove_ from the collection
  - `nondominated-selection` removes all `Answers` from the collection which are _strongly dominated_ by any other, using [Pareto domination](https://en.wikipedia.org/wiki/Multi-objective_optimization#Introduction) over the specified set of `Rubrics`. One `Answer` dominates another if all of its comparable scores are _at least as good_, and at least one is better.
  - `most-dominated-cull` over a collection of `Answers` recursively removes _non-dominated_ `Answers` until no dominated ones remain. It then removes those "most dominated" ones from the original collection.
  - `remove-uncooperative` removes any `Answer` from a collection for which any `Score` is non-numeric (for the specified `Rubrics`)
  - `select-on-nil` returns the subset of a collection of `Answers` where any `Score` on a any specified `Rubric` is `nil`
- various mutation and crossover operators


## Genomes for Push Programs

At the moment there are two representation schemes for Push programs: `plush` and `bb8`. Both of these genomic schemes address the problem of representing a tree-like Push program (tree-like because it can contain arbitrary code blocks surrounded by matching parentheses) into a linear genome that can be translated back into the same program. Each comes at this problem from a different direction, though.

The `plush` genome is concerned with where and how many parenthesized code blocks are opened and closed, in the course of a depth-first traversal of the program tree. When the linear genome is transcribed into a Push program, a buffer of _potential_ branches is created, and consumed as additional genes are translated.

By convention in the original Clojush , `plush` genomes refer to deep information about the syntax of the Push language itself, constructing branches in the program whenever instructions that would be expected to _use_ those branches appear in the code. So for example because the Push instruction `:exec-swap` uses two `:exec` arguments, the Clojush `plush` convention would be to _automatically_ and immediately open two consecutive branches following the insertion of an `:exec-swap` instruction. The implementation here permits this behavior through a backwards-compatible interface described below.

The `bb8` genome takes a more abstract generative approach. Each `bb8` gene contains instructions that indicate how to move a "cursor" to a new position on the developing Push program tree, and how to insert a new item in that cursor's new position. Unlike `plush` genomes, the tree structure of the program is explicitly captured; branches are inserted by inserting an empty code block in the developing tree, which can then be "filled" by later transcribed genes.

### `plush` genomes

The `plush` genome implemented here is an extension of that used in the [Clojush system](https://github.com/lspector/Clojush) from Lee Spector's lab at Hampshire College. In changing the representation and translation schemes, I've tried to maintain backwards-compatibility, but the Klapaucius Push dialect is no longer much like the Push found in Clojush, so be wary of breaking changes if you are importing Clojush genomes.

A `plush` genome is simply a vector of `plush` genes.

#### `plush` genes

A `plush` gene is a Clojure hash-map consisting of several key-value pairs, _all optional_:

1. `:item`: The value should be a Push literal to be inserted in the current cursor position in the program, or one of a few special _editing commands_ (see below). Any `:item` that is not recognized as an editing command will be inserted into the program as found, unless it is `nil`.
2. `:open`: The value should be a positive integer which indicates how many branches should be _opened_ immediately following the current insertion point. For example, `{:item :x :open 1}` will insert `:x` _and_ an empty list at the current cursor position. `{:item :x :open 3}` will insert `:x` and an empty list, and set the translator up so that by the time translation is complete, a total of three lists will be inserted consecutively following the `:x` in the resulting program.
3. `:close`: The value should be a positive integer which indicates how many "pending" and current branches will be _closed_ after the `:item` is inserted and any new branches created have been queued. For example, `{:item :x :open 2 :close 1}` will result in the insertion of `:x`, followed by an empty list (opened and then closed), and the beginning of a second list. Detailed examples are shown below, since I know this seems confusing.
4. `:silent?`: The value should be a `true` or `false`, indicating whether this gene should be processed or not. If `true`, no `:item` is inserted and no change to the branching state of the program is made: the entire gene is silenced.

Of these, _none_ are strictly required. If `:item` is missing or `nil`, the gene can still modify the tree structure via the `:open` and `:close` fields. If `:open` is missing, then its value is assumed to be 0. If `:close` is missing, its value is also inferred to be 0. If `:silent?` is missing, its value is inferred to be `false`.

**NOTE:** In the original Clojush `plush` genome scheme used implementation-specific information about the Push language to _implicitly_ calculate the `:open` value for each item. Specifically, the `:open` value was assumed to be _the number of items from the `:exec` stack used by an instruction, or 0 otherwise_. In the `plush` scheme, backwards-compatibility is available simply by setting the `:open` value _explicitly_ to the same numerical value, and leaving it 0 or absent otherwise. That is, for each gene where the `:item` is a Push instruction, set an `:open` value equal to the number of `:exec` argument used by that instruction (which can be obtained via the helper function `answer-factory.genome.plush/derived-push-branch-map`).


#### editing commands

There are two `plush`-specific editing commands, which can appear as values in the `:item` field:

- `:noop_delete_prev_paren_pair`: This will "lift up" the contents of the last closed code block inserted (and closed), backtracking from the current cursor position.
- `:noop_open_paren`: This is included for backwards-compatibility with Clojush's `plush` genome translator. It behaves exactly like the gene `{:open 1}`.

#### using a `:branch-map` argument

The `plush->push` function can accept an optional `:branch-map` argument, which should be a hash-map where the keys are one or more `:item` values, and the values are non-negative integers. The numeric values are used as a sort of "default" for the `:open` field of all genes in which the key is the `:item`. So for example when calling `(plush->push [...genome...] :branch-map {:exec-dup 1 false 2}` Every occurrence of a gene with `{:item :exec-dup}` will by default also include `{:open 1}`, and every gene with `{:item false}` will by default also include `{:open 2}`.

When no `:branch-map` is specified, the _default_ `:open` value for every item iss assumed to be 0. Whenever a gene _explicitly_ specified an `:open` value, the default value in the `:branch-map` will be _overridden_ for that gene, but will still apply to genes with the same `:item` and no explicit `:open` value elsewhere in the genome.

#### Some examples might be good

I've included some explicit examples in the form of runnable [Midje `facts`](https://github.com/Vaguery/answer-factory-clj/blob/master/test/answer_factory/genome/plush_test.clj#L540-L666), which show explicit examples of all this behavior. 

### `bb8` genomes

TBD

### Contributing

Nothing for the moment. Comments are welcome.


## Testing

The project uses [Midje](https://github.com/marick/Midje/).

### How to run the tests

`lein midje` will run all tests.

`lein midje namespace.*` will run only tests beginning with "namespace.".

`lein midje :autotest` will run all the tests indefinitely. It sets up a
watcher on the code files. If they change, only the relevant tests will be
run again.
