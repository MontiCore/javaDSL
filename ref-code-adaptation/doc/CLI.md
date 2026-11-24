# Command-line tool

[Back to the overview](../README.md) · [First runnable example](QuickStart.md)

Build the executable with `gradle :ref-code-adaptation:shadowJar` from the
repository root, or `gradle.bat :ref-code-adaptation:shadowJar` on Windows.
Its entry point is `de.monticore.codeAdaption.CodeAdapterTool`.

```shell
java -jar ref-code-adaptation/target/libs/CodeAdapter.jar --help
```

## Options

Paths are relative to the current working directory unless absolute. Quote
paths containing spaces. Required inputs apply to adaptation; `--help` can be
used on its own.

| Option | Required | Meaning/default |
| --- | --- | --- |
| `-r`, `--reference <path>` | Yes | Reference `.cd` file |
| `-c`, `--concrete <path>` | Yes | Concrete `.cd` file |
| `-rc`, `--reference-code <path>` | Yes | Reference Java source directory |
| `-m`, `--mapping <name...>` | Yes | One or more mapping stereotype names, e.g. `stud prof` |
| `-o`, `--output <path>` | Yes | Directory for adapted output |
| `-cc`, `--concrete-code <path>` | No | Concrete handwritten source directory; absent by default |
| `--top-separation` | No | Keep matching concrete HWC and adapted implementations in separate TOP classes |
| `--concretize` | No | Complete the concrete CD, then prefer usable conformance mappings; otherwise use conflict-checked manual derivation per mapping. Default is manual mode without completion. |
| `--matching <name...>` | No | Java matching strategies: `name`, `infix`, `annotation`; default is all three |
| `--no-common-parent` | No | Disable exact common-interface grouping for multiple incarnations |
| `--no-persist-concretized-cd` | No | Suppress completed-CD output and partial-CD diagnostics; relevant only with `--concretize` |
| `--ignore-unmatched` | No | Allow unmatched Java elements; see the retention rules below |
| `-d`, `--debug` | No | Enable debug logging |
| `-h`, `--help` | No | Print help and exit |

The CLI enables these CD-conformance policies: `STEREOTYPE_MAPPING`,
`NAME_MAPPING`, `ADAPTED_NAME_MAPPING`, `SRC_TARGET_ASSOC_MAPPING`, `INHERITANCE`,
`ALLOW_CARD_RESTRICTION`, `METHOD_OVERLOADING`, and `ALLOW_ADDITIONAL_PARAMETERS`.
Use the [Java API](JavaAPI.md) for a different parameter set.

## Selecting a composition workflow

The paths in the following examples are placeholders for your own inputs.

Adapt without concrete Java:

```shell
java -jar ref-code-adaptation/target/libs/CodeAdapter.jar --reference Reference.cd --concrete Concrete.cd --reference-code reference-java --mapping ref --output generated
```

Merge adapted members into concrete Java:

```shell
java -jar ref-code-adaptation/target/libs/CodeAdapter.jar --reference Reference.cd --concrete Concrete.cd --reference-code reference-java --concrete-code concrete-java --mapping ref --output generated
```

Keep concrete HWC and adapted implementations separate, with model completion:

```shell
java -jar ref-code-adaptation/target/libs/CodeAdapter.jar --reference Reference.cd --concrete Concrete.cd --reference-code reference-java --concrete-code concrete-java --mapping ref --output generated --top-separation --concretize
```

`--top-separation` decides per type whether matching HWC exists. Without a match,
the adapted class keeps its ordinary name. Omitting `--concrete-code` is valid
with this flag and emits all adapted classes directly. Neither choice starts a
regular Java generator. See [TOP and generated bases](TopSeparation.md).

## Output and failures

With `--concretize`, the working CD is saved in `--output` under the input
concrete CD's filename. Successful completion writes it there before Java
validation and staging; a failed completion attempts to save its partial model
there as a diagnostic. A successful run also includes the completed CD in the
published output. Use `--no-persist-concretized-cd` to suppress all these writes.

Java output is staged and published only after successful adaptation. Publication
replaces the entire existing output directory, including old files absent from
the new result. Use a dedicated output directory. Validation failures do not
publish new Java, but the early CD write described above can still change that
directory. Input and output paths must not overlap, and a filesystem root cannot
be the output directory.

The CLI does not invoke `javac`: compile the result with your application's
dependencies and any generated bases.

| Exit code | Current behavior |
| --- | --- |
| `0` | Successful adaptation or help |
| `2` | Invalid arguments or a reported adaptation failure (`ParseException`, `IllegalArgumentException` or `CodeAdaptationException`), including model completion rejection and ambiguous grouping |
| `1` | An unexpected runtime failure; completion runtime bugs remain distinct from model rejection |

Completion failure stops adaptation. Only a later conformance check without a
usable incarnation mapping can trigger manual mapping fallback.

`--ignore-unmatched` is not a universal “copy everything” switch. It allows
unmatched members and variables to remain unchanged. Both modes automatically
retain required source-local helpers, independently of this flag. Other unmatched
types are excluded from mapping-specific adapted output. To retain a deliberately
unchanged reference type, use `@Adapt(ignore = true)`; see [Java matching](Mappings.md).
