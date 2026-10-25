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
| `--concretize` | No | Complete the concrete CD, then obtain mappings through conformance checking; default is manual mapping without completion |
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

With `--concretize`, the working CD is saved under the input concrete CD's
filename. A failed completion or later adaptation may also save this CD as a
diagnostic. Use `--no-persist-concretized-cd` to suppress both forms.

Java output is staged and published only after successful adaptation. Validation
failures leave existing Java output intact. The CLI does not invoke `javac`:
compile the result with your application's dependencies and any generated bases.

Exit code `0` means success or help, `2` means invalid arguments or a reported
adaptation failure, and `1` means an unexpected runtime failure.

`--ignore-unmatched` is not a universal “copy everything” switch. It allows
unmatched members and variables to remain unchanged. Unmatched types are
excluded from mapping-specific adapted output. To retain a deliberately
unchanged reference type, use `@Adapt(ignore = true)`; see [Java matching](Mappings.md).
