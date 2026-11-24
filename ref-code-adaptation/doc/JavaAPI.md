# Java API

[Back to the overview](../README.md)

The main class is `de.monticore.codeAdaption.CodeAdapter`. In this Gradle
multi-project build, a consuming module declares the adapter dependency:

```groovy
dependencies {
    implementation project(":ref-code-adaptation")
}
```

`CDConfParameter` is part of the public constructor and comes from the `cddiff`
artifact. The adapter exports that dependency through Gradle `api`, making it
available on the consumer's compile and runtime classpaths. A separate `cddiff`
declaration is not needed for the configuration example below.

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
when completion is disabled. When enabled, the working CD is saved in `output`
under the input concrete-CD filename, immediately after successful completion
and before Java validation/staging. Failed completion attempts to save a partial
CD there. Successful publication also includes the completed CD in staging.
These early writes are independent of Java publication; the input CD file is
not overwritten.

Common-parent grouping permits an interface whose concrete implementers exactly
match a reference element's incarnation group. An interface with additional,
unrelated implementers is not selected. See [mappings and modes](Mappings.md).

Modeled adaptation conflicts, expected completion rejections and ambiguous
grouping are reported as `CodeAdaptationException`. Invalid input arguments can
throw `IllegalArgumentException`. Unexpected completion runtime failures remain
`IllegalStateException`, preserving their original cause.

The output tree must not overlap either Java input tree or either input CD
file, and a filesystem root is rejected as output. Overlap is rejected with
`IllegalArgumentException`; it is not merely discouraged. Successful publication
replaces the entire output directory, so dedicate that directory to adapter
results. Compile the output with your application's external Java dependencies.
