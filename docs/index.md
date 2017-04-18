# answer-factory-machines

A library of support systems for evolving Push and [Klapaucius](https://github.com/Vaguery/klapaucius) programs.

The design patterns (and metaphors) used in `answer-factory` are qualitatively different from most genetic programming and software synthesis algorithms you may have encountered before. The overarching design is for a continuous, batch-based, distributed manufacturing process. The fundamental components being "built" in this process are `Answers`, which are like (but not identical with) the "individuals" you may see mentioned in evolutionary algorithms literature.

Each `Answer` contains one or more `genome` structures, representing a given Push program (and possibly additional information, or an auxiliary program in another representation language). The `genome` of a given `Answer` can be recorded in any of several representation schemes, each with different features.

The evaluation of a particular `Answer` is done by a `Rubric` object. Each `Rubric` embodies _one particular training or test case_, a constraint, or some other explicit design goal of search. A `Rubric` is essentially a _function_ which accepts an `Answer` (plus additional context needed), and produces a numeric or vector-valued _score_. By convention, all scores are treated as _minimizations_, though they don't necessarily need to be zero-based. In the case of vector-valued scores, _domination_ is defined in the direction of minimization for all components.

Search itself is handled by a variety of `Machine` objects. In the most general (but probably least helpful) sense, a `Machine` can be thought of as a _transducer_ that accepts as its argument a collection of `Answer` items, and produces a new collection of `Answer` items. For example

- a "guessing machine" might, when activated, produce a collection of 100 random `Answer` objects with `plush` genomes, using particular parameters specified by the designer
- a "mutation machine" might accept a collection of `Answer` objects and add to that collection a single variant of each `Answer` already present
- a "lexicase selection machine" might accept a collection of `Answer` objects, and a collection of `Rubric` items, and return a collection containing only one "winning" `Answer`, selected on the basis of the specified `Rubric`s
- and so on

## What this library _doesn't_ do

You'll note this library is called `answer-factory-machines`. It makes no assumptions about the larger-scale "factory" systems in which they are being used, nor does it care explicitly about data stores, asynchronous thread coordination, or reporting. It's intended to provide components for use in a larger-scale system to be built externally. There will be several demo cases included, but these don't implement "real" general-purpose software synthesis systems, but are rather intended as inspirations.

## More documentation

- [Recent changes](https://github.com/Vaguery/answer-factory-machines/blob/master/CHANGELOG.md)
- [Design goals](design-goals.md)
- [Genomes](genomes.md)
  - [`bb8` genomes](bb8.md)
  - [`plush` genomes](plush.md)
- Answers [TBD]
- Rubrics [TBD]
- [Search operators](operators.md)
- Data stores [TBD]
- Demos [TBD]
