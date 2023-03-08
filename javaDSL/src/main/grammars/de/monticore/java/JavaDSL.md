# JavaDSL

JavaDSL provides a full spec-compliant parser for Java 17, a pretty printer and
additional tools for working with Java source artifacts.

The core grammar definition can be found in [`de.monticore.java.JavaDSL`][JavaDSL].


## Testing

### Corpus Tests

To ensure that the parser covers the entire specification, it is tested on a
large corpus of open-source libraries. _Corpus Tests_ are executed as part of
the regular unit tests.

To add a library to the corpus, modifications should be made in two places:

1. The dependency should be added to the [corpus' version catalog](../../../../../../../gradle/corpus.versions.toml), and
2. the dependency should be added to the "corpus" configuration in the [build script](../../../../../../build.gradle).

Note, that the libraries in the corpus must not be kept up-to-date. Instead, a
variety of libraries targeting different Java language versions and use-cases
should be used to cover different code-styles and language features.


[JavaDSL]: https://git.rwth-aachen.de/monticore/javaDSL/blob/dev/javaDSL/src/main/grammars/de/monticore/java/JavaDSL.mc4
