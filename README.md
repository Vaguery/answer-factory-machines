# answer-factory

A platform for generative programming.

### Design goals

Most Genetic Programming platforms depend on very tight coupling between the _representation language_ (used to express potential solutions to one's problem of interest) with the _search language_ that implements the process of search _among_ those potential solutions. Here I'm attempting to explicitly divorce search dynamics and the comparison of potential solutions' behavior from their intrinsic structure. Here, I've started with Lee Spector's Push language as a default; it's qualitatively unlike most tree-based GP representations, far more expressive and extensible. Eventually I expect to include support for arbitrary representation schemes, including Cartesian GP, Grammatical Evolution and other less well-known approaches that have many strengths but which are harder to find in usable (and interoperable) implementations.

Even beyond this tendency for representational lock-in, most extant genetic programming systems hard-code the outdated "biologically inspired" model of crossover-and-mutation search, usually based on decades-old sketches which the active research field has for the most part abandoned. "Genetic" programming has for many years invoked a far more diverse suite of complex search operators, heuristics and design patterns that have little to do with biological dynamics, in service of actually finding interesting solutions to problems and of addressing the formal structure of practical problem-solving. Most diagrams of "programs" as DNA-like lines (or S-expression trees) that are cut and then crossed over fall short of what actually happens in effective modern GP systems, which can use hill-climbing, other machine learning algorithms, and modern statistical methods in support of discovery. It should be as easy to try simple hillclimbing with mutation on your problem as it is to throw the kitchen sink at it. Here I'll be abstracting search dynamics into what I hope is a suite of representation-agnostic (or representation-specific) "stages", so simple approaches to GP search can be tried before the more ornate special-purpose ones are invoked.

One of the most antiquated aspects of Genetic Programming as it's often approached is the notion of _fitness_. Just as basic genetic algorithms, with their simple crossover and mutation operations on linear genomes, are no longer representative of modern GP, simple "fitness functions" based on aggregate statistics are rarely the most useful approach for finding interesting results—not least because numerical optimization should  _never_ the goal of GP search. Coevolutionary systems, multiobjective problem-solving, dynamic or subjective evaluations, and disaggregated contingent "fitness" (like Spector's Lexicase selection) are much more common and useful in practice. These many "fitnesses" (which I'll call _rubrics_ here) can be used as a much more diverse and complex toolkit than the brute force summary of summed-squared-error. They serve as both our _sensors_ and our _actuators_ for controlling the dynamics of GP search, and so I'll be supporting that diversity as much as possible.

The field of machine learning has advanced tremendously in the last few years, and the traditional association of GP with machine learning practice has suffered. Rightly so: GP is a _terrible_ machine learning approach, at least when you define "machine learning" as the numerical optimization of statistical models based on sound first-principles models. One applies most ML algorithms knowing ahead of time what particular _aspect_ of the data is being addressed, and what well-posed numerical quantities are being minimized. A machine learning algorithm is something you set up, and apply to your dataset as a _step_ in some larger question-answering context: What structure does this oracle expose? What did people buy on our website that you might also want to buy? Where do extra eyeballs appear on the dog when I use this training data for deep learning?

GP (whether you think of it as "genetic programming" or "generative" as I prefer) is much better applied as an _exploratory_ system, something used for "machine discovery" not "machine learning". One uses machine learning algorithms to capture stylized decisions in easily-maintained production code. One uses genetic programming to _explore how to even begin_. GP may be "machine learning" in a historical sense, or because it does things to data with computers, but in a practical sense it doesn't even try to do what most ML things do: it is a search for structures—algorithms, designs, inventions, rules, proofs, models—not a way of _optimizing_ one structure in a given context. The nerdiest way I can think of describing it for computer folks is that GP should be thought of as _higher-order functions over machine learning_.

### Status

Preliminary work only so far. [An earlier Rub-based system](https://github.com/Vaguery/Answer-Factory) is the inspiration, but this Clojure-and-Javascript thing is being designed for modern cloud systems.

### Plan

- Simple single-machine population-based GP algorithms with [Push](https://github.com/Vaguery/push-in-clojure), in support of colleagues using [Clojush](https://github.com/lspector/Clojush) for active research projects
- Distributed cloud-based version
- Interactivity with long-term searches
- [more here]

### Contributing

Nothing for the moment.


The project uses [Midje](https://github.com/marick/Midje/).

## How to run the tests

`lein midje` will run all tests.

`lein midje namespace.*` will run only tests beginning with "namespace.".

`lein midje :autotest` will run all the tests indefinitely. It sets up a
watcher on the code files. If they change, only the relevant tests will be
run again.
