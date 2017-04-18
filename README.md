# answer-factory-machines

A collection of Clojure tools for generative programming of Push and Klapaucius programs.

[View the project documentation here](http://vaguery.github.io/answer-factory-machines/).

### Status & Contributing

Nothing for the moment. Comments are welcome. The library is under active development, and almost all features may be renamed, moved, or swapped to a different library at any point, so watch the feed for breaking changes if you want to follow along.

## Testing

The project uses [Midje](https://github.com/marick/Midje/).

### How to run the tests

`lein midje` will run all tests.

`lein midje namespace.*` will run only tests beginning with "namespace.".

`lein midje :autotest` will run all the tests indefinitely. It sets up a
watcher on the code files. If they change, only the relevant tests will be
run again.
