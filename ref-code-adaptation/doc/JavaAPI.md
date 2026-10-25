# Java API

[Back to the overview](../README.md)

The main class is `de.monticore.codeAdaption.CodeAdapter`. In this Gradle
multi-project build, a consuming module can depend on
`implementation project(":ref-code-adaptation")`.

## Configure an adapter

Java matching connects source declarations to the reference CD. CD-conformance
parameters control how the concrete CD realizes the reference CD. This example
uses the same parameter set as the CLI:

```java
import static de.monticore.cdconformance.CDConfParameter.*;
import static de.monticore.codeAdaption.utils.AdapterParam.*;

import de.monticore.codeAdaption.CodeAdapter;
import java.nio.file.Path;
import java.util.Set;

CodeAdapter adapter = new CodeAdapter(
    Set.of(NAME_MATCHING, INFIX_MATCHING, ANNOTATION_MATCHING),
    Set.of(STEREOTYPE_MAPPING, NAME_MAPPING, ADAPTED_NAME_MAPPING,
        SRC_TARGET_ASSOC_MAPPING, INHERITANCE, ALLOW_CARD_RESTRICTION,
        METHOD_OVERLOADING, ALLOW_ADDITIONAL_PARAMETERS));

var referenceCD = Path.of("models/Reference.cd").toFile();
var concreteCD = Path.of("models/Concrete.cd").toFile();
var mappings = Set.of("ref");
var referenceJava = Path.of("reference-java");
var concreteJava = Path.of("concrete-java");
var output = Path.of("target/adapted");
```

The snippets below belong inside a method. Replace the paths with your inputs
and choose one invocation per run.

## Adapt without concrete Java

```java
adapter.adaptWithoutConcreteCode(
    referenceCD, concreteCD, mappings, referenceJava, output);
```

The concrete CD is still required. Only the concrete Java directory is omitted.
This emits the adapted reference implementation directly and does not launch a
regular model-to-Java generator.

## Merge with concrete handwritten code

```java
adapter.adapt(
    referenceCD, concreteCD, mappings, referenceJava, concreteJava, output);
```

This uses member merging when adapted and concrete declarations coincide.
Concrete sources are read-only inputs; composition is written to `output`.

## Separate implementations through TOP inheritance

```java
adapter.adaptWithTopSeparation(
    referenceCD, concreteCD, mappings, referenceJava, concreteJava, output);
```

A matching handwritten `Student` extends the adapted `StudentTOP`. If there is
no matching concrete class, the adapted class keeps the name `Student`.
There is also an overload omitting `concreteJava`. See
[TOP separation](TopSeparation.md) for existing TOP inheritance and external bases.

## Completion, grouping, and CD persistence

Each workflow also has overloads accepting these final boolean arguments:

```java
adapter.adaptWithTopSeparation(
    referenceCD, concreteCD, mappings, referenceJava, concreteJava, output,
    true,   // useConcretization: complete the concrete CD before adaptation
    true,   // useCommonParentForMultipleIncarnations: allow exact grouping
    false); // persistConcretizedCD: suppress completed/partial-CD output
```

The shortest overloads use `false, true, true`. Overloads accepting only the
first two booleans also default persistence to `true`. Persistence is ignored
when completion is disabled. When enabled, it includes failure diagnostics and
successful output; the original concrete-CD file is not overwritten.

Common-parent grouping permits an interface whose concrete implementers exactly
match a reference element's incarnation group. An interface with additional,
unrelated implementers is not selected. See [mappings and modes](Mappings.md).

Modeled adaptation conflicts are reported as `CodeAdaptationException`.
Input validation and model completion can also throw unchecked exceptions such
as `IllegalArgumentException` or `IllegalStateException`. Input paths and output
paths should refer to separate trees. Compile the output with the external Java
dependencies required by your application.
