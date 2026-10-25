# Quick start

[Back to the overview](../README.md)

This walkthrough adapts an `Entity` Java class to a `Student` class. Run commands
from the repository root, the directory containing `settings.gradle` and
`ref-code-adaptation`. You need Java 21 and an installed Gradle 8.8. This repository does not include a Gradle wrapper.

## Step 1: Build the CLI

```shell
gradle :ref-code-adaptation:shadowJar
java -jar ref-code-adaptation/target/libs/CodeAdapter.jar --help
```

Use `gradle.bat` instead of `gradle` on Windows. The executable JAR is
written to `ref-code-adaptation/target/libs/CodeAdapter.jar`.

## Step 2: Understand the inputs

This example uses existing files:

| Input | Meaning |
| --- | --- |
| [App.cd](../src/test/resources/de/monticore/codeAdaption/App.cd) | Reference model containing `Entity.id` |
| [UniApp.cd](../src/test/resources/de/monticore/codeAdaption/UniApp.cd) | Application model mapping `Entity` to `Student` and `id` to `studentId` |
| [Entity.java](../src/test/resources/de/monticore/codeAdaption/adapter/name/Entity.java) | Reference Java to adapt |

In `UniApp.cd`, `<<ref="Entity">>` on `Student` means “Student realizes Entity
under mapping ref.” The field stereotype works the same way within that class.
The reference Java is matched to the reference CD by equal names.

## Step 3: Adapt the reference Java

```shell
java -jar ref-code-adaptation/target/libs/CodeAdapter.jar --reference ref-code-adaptation/src/test/resources/de/monticore/codeAdaption/App.cd --concrete ref-code-adaptation/src/test/resources/de/monticore/codeAdaption/UniApp.cd --reference-code ref-code-adaptation/src/test/resources/de/monticore/codeAdaption/adapter/name --mapping ref --matching name --output ref-code-adaptation/target/quickstart
```

`--mapping ref` selects the stereotype named `ref`. `--matching name` selects how
Java declarations are associated with the reference CD. These are different
steps: CD mapping tells the adapter the target; Java matching tells it what to
transform.

## Step 4: Inspect the result

The output directory contains `Student.java` with this class content, ignoring
formatting and any package/import declarations:

```java
public class Student {
  String studentId;
}
```

The inputs are preserved. A successful run publishes a new output directory,
so use an output location dedicated to generated results.

There is no reference implementation of `Application` or `Service` in this
source directory. The example therefore does not generate their concrete
counterparts from the CD alone.

## Step 5: Choose the next workflow

- Add `--concretize` when the concrete model needs completion before adaptation.
  See [mappings and modes](Mappings.md).
- Add `--concrete-code path/to/java` to merge with your application Java.
- Also add `--top-separation` to keep adapted implementations in separate base
  classes. See [TOP separation](TopSeparation.md).
- Use the [Java API](JavaAPI.md) to configure matching and conformance policies
  in a build integration.
