# Reference Code Adaptation

Adapt a reusable Java implementation to the names and structure of your application.
For example, these class diagrams map `Entity.id` to `Student.studentId`:

```cd
// Reference.cd
import java.lang.String;
classdiagram Reference {
  class Entity { String id; }
}
```

```cd
// Concrete.cd
import java.lang.String;
classdiagram Concrete {
  <<ref="Entity">> class Student {
    <<ref="id">> String studentId;
  }
}
```

With mapping `ref`, the reference Java `class Entity { String id; }` becomes
`class Student { String studentId; }`. Existing method bodies are adapted along
with their type and member references. The [quick start](doc/QuickStart.md)
runs this example using files already in the repository.

A **class diagram (CD)** describes classes, fields, methods, and relationships.
The **reference** describes reusable concepts; the **concrete** diagram describes
your application. A **mapping** connects them using stereotypes such as
`<<ref="Entity">>`. The corresponding concrete element is called an
**incarnation**. **HWC** means handwritten Java code.

## Examples and documentation

| I want to... | Start here |
| --- | --- |
| Run my first adaptation | [Quick start](doc/QuickStart.md) |
| Use the command-line tool | [CLI options and examples](doc/CLI.md) |
| Call the adapter from Java | [Java API](doc/JavaAPI.md) |
| Understand mappings and model completion | [Mappings and adaptation modes](doc/Mappings.md) |
| Keep handwritten and adapted code separate | [TOP separation](doc/TopSeparation.md) |
| Extend the implementation | [Internals](doc/Internals.md) |
| Run tests and understand supported boundaries | [Testing](doc/Testing.md) |

More runnable scenarios are in the [evaluation fixtures](src/test/resources/de/monticore/codeAdaption/evaluation)
and [evaluation tests](src/test/java/de/monticore/codeAdaption/evaluation).

## Command Line Interface Tool

The CLI reads two class diagrams, a mapping name, and reference Java sources.
It writes adapted Java into a separate output directory. Concrete handwritten
Java is optional. With it, the tool can merge adapted members into the concrete
classes or keep the implementations separate using inheritance.

### Building the tool

Use Java 21 and an installed Gradle 8.8 from the repository root:

```shell
gradle :ref-code-adaptation:shadowJar
java -jar ref-code-adaptation/target/libs/CodeAdapter.jar --help
```

### Actions and parameters

Supply `--reference`, `--concrete`, `--reference-code`, `--mapping`, and `--output`.
Add `--concretize` to complete the concrete model before adapting Java, or
`--concrete-code` and `--top-separation` to use concrete handwritten classes
with separate adapted base classes. See the [complete option table](doc/CLI.md).

### Using the tool: a first adaptation

The following command runs from the repository root:

```shell
java -jar ref-code-adaptation/target/libs/CodeAdapter.jar --reference ref-code-adaptation/src/test/resources/de/monticore/codeAdaption/App.cd --concrete ref-code-adaptation/src/test/resources/de/monticore/codeAdaption/UniApp.cd --reference-code ref-code-adaptation/src/test/resources/de/monticore/codeAdaption/adapter/name --mapping ref --matching name --output ref-code-adaptation/target/quickstart
```

The output contains `Student.java` with `studentId`. This command adapts the
provided reference Java. Classes that exist only in the CD are not generated
by the adapter.

## Java API

Use `de.monticore.codeAdaption.CodeAdapter` for build integrations or custom
tools. It offers member merging, adaptation without concrete Java, and TOP
separation. The [API guide](doc/JavaAPI.md) includes configuration and calls for
each workflow.

## Further information

- [CD4Analysis: class diagram syntax and tools](https://github.com/MontiCore/cd4analysis)
