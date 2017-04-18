# Search operators

The library implements a variety of search operators, which are in general functions operating on _collections_ of `Answer` objects.

## Selection

- `uniform-selection`: pick one randomly sampled `Answer` from a collection
- `uniform-cull`: remove one randomly sampled `Answer` from a collection
- `simple-selection`: return all the best-scoring `Answers` from a collection, given a specified `Rubric`
- `simple-cull`: remove all the worst-scoring `Answers` from a collection, given a specified `Rubric`
- `lexicase-selection`: randomly permute a collection of `Rubrics`, and recursively retain the best-scoring `Answers` on each one (selectedin turn). For example, if the `Rubrics` are numbered `[1 2 3]`, it might randomly permute these to `[3 1 2]`; the resulting subset of `Answers` are obtained by selecting the best at `Rubric` 3, then the subset of those best at `Rubric` 1, then the subset of _those_ best at `Rubric` 2.
- `lexicase-cull`: removes the "winners" of `lexicase-selection` applied to _negated_ scores: every numerical score is negated, and `lexicase-selection` is applied to determine which `Answers` to _remove_ from the collection
- `nondominated-selection`: removes all `Answers` from the collection which are _strongly dominated_ by any other, using [Pareto domination](https://en.wikipedia.org/wiki/Multi-objective_optimization#Introduction) over the specified set of `Rubrics`. One `Answer` dominates another if all of its comparable scores are _at least as good_, and at least one is better.
- `most-dominated-cull`: over a collection of `Answers` recursively removes _non-dominated_ `Answers` until no dominated ones remain. It then removes those "most dominated" ones from the original collection.
- `remove-uncooperative`: removes any `Answer` from a collection for which any `Score` is non-numeric (for the specified `Rubrics`)
- `select-on-nil`: returns the subset of a collection of `Answers` where any `Score` on a any specified `Rubric` is `nil`
- various mutation and crossover operators
