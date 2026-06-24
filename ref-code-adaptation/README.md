# ref-code-adaptation

`ref-code-adaptation` adapts concrete Java code from reference Java code and class-diagram mappings. 
## Inputs

The main entry point is `de.monticore.codeAdaption.CodeAdapter`.

Required inputs:

- reference class diagram
- concrete class diagram
- mapping names, such as `buildPat`, `observer`, or `ref`
- adapter/reference Java source directory
- concrete Java source directory
- output directory

The class diagrams define which reference classes, fields, and methods incarnate as concrete Java elements. Java code is parsed and transformed with MontiCore JavaDSL and Spoon.

## Adaptation Flow

1. Load reference and concrete class diagrams with `JavaLoader.parseCD`.
2. Build mapping-specific incarnation contexts.
3. Detect unresolved or ambiguous mappings before the output directory is cleaned.
4. Copy and filter reference adapter code for the active mapping.
5. Adapt types, fields, methods, parameters, constructor calls, and pattern-derived members.
6. Merge adapted code with existing concrete code.
7. Clean generated Java:
   - remove `@Adapt` annotations with Spoon
   - remove invalid generated imports through JavaDSL import declarations
   - preserve valid existing imports without inventing imports for unresolved
     simple names
   - keep Spoon as the only whole-file formatter; import cleanup edits only
     import declaration source ranges
8. Compile and structurally verify generated Java in tests.

The context-building step depends on the `useConcretization` argument of
`CodeAdapter.adapt(...)`.

### `useConcretization=true`

This mode delegates model repair to cdconcretization before code adaptation:

1. `ConcretizationCompleter` completes the concrete CD by adding safe missing
   model elements, repairing deterministic inheritance gaps, adapting
   multi-incarnation names, and removing redundant inherited attributes.
2. `CDConformanceChecker` validates the completed CD and provides the
   mapping-specific incarnation mapping.
3. `CompletedCDJavaProjector` writes Java-expressible elements that were added
   to the completed concrete CD, such as missing fields, methods, types, enum
   constants, inheritance, and interfaces.

Use this mode when the concrete CD may need deterministic model-level repair
before Java adaptation.

### `useConcretization=false`

This mode does not mutate or complete the concrete CD. It builds a manual
incarnation context from:

- explicit stereotypes for the active mapping name,
- deterministic name and adapted-name rules enabled by `CDConfParameter`,
- manual `<<forEach="...">>` mappings derived from already-known
  incarnations.

The manual branch does not call `ConcretizationCompleter` or
`CDConformanceChecker`, and it does not clone or create CD elements. Conflicts
are reported together as a `CodeAdaptationException` before
`JavaLoader.removeDirectory(outputPath)` is executed, so existing output is
preserved when adaptation cannot safely start.

Supported manual `forEach` targets:

- Type target: `<<forEach="DataClass">> class DataClassBuilder`
- Attribute target: `<<forEach="DataClass.attribute">> any attribute`
- Owner-implied attribute target: `<<forEach="attribute">> any getAttribute()`

For method `forEach` over attributes, concrete method incarnations are derived
from already-mapped concrete attributes and deterministic method names, for
example `getAttribute()` to `getFirstName()` and `getAge()`. The manual mode
does not invent cdconcretization suffix rules; concrete stereotypes or enabled
name rules must make the target deterministic.

## Important Implementation Points

- `JavaLoader.parseCD` must use the same symbol-table setup as `loadCD`, including built-in types. CDs must declare imports explicitly for Java library types such as `Object`, `String`, `List`, or `Optional`.
- `BasicUpdateHandler` handles normal single-incarnation adaptation and builder generation.
- `MultiIncarnationUpdateHandler` handles reference elements with multiple concrete incarnations.
- `CDTypeRelations` is the only place that should contain compatibility reflection for CD APIs such as interfaces, superclasses, modifiers, and type-reference printing.
- `ManualIncarnationContextBuilder` is the non-mutating context builder for `useConcretization=false`.
- `AdaptationConflictDetector` validates manual mappings before Java output is written.
- `CompletedCDJavaProjector` fills Java-expressible gaps only after cdconcretization, such as missing fields, methods, types, enum constants, inheritance, and interfaces.
- `JavaSourcePostProcessor` is the final source cleanup step. It parses JavaDSL
  compilation units for import declarations and removes only known invalid or
  malformed generated imports. It does not infer missing JDK imports from
  unresolved simple names.
- `JavaSourceNames` centralizes Java/CD type naming, signature keys, generic
  rendering, arrays, primitives, `void`, and `any` normalization. Callers should
  use it instead of open-coded simple-name or signature parsing.
- `SpoonUpdater` is the supported code updater. `RegexUpdater` remains only as a
  deprecated compatibility wrapper and must not reintroduce whole-file
  `replaceAll` behavior.
- Generated builder bodies are represented as structured `MethodBodySpec`
  values, so setter-return and constructor-return methods are built through
  Spoon statements instead of parsed string snippets.
- `GeneratedJavaOracle` uses structural Java extraction.
- Final generated output must not contain adapter metadata.
- Generated Java cleanup intentionally avoids broad source-text regex formatting.
  Comments, literals, generics, operators, and method bodies should be left to
  Spoon, JavaDSL, or AST-level code.



## Manual Conflict Detection

When `useConcretization=false`, the adapter should detect risks that
cdconcretization would otherwise repair or reject on the model level. Reported
conflict categories include:

- missing used type or `any` type incarnation
- type-kind mismatches that cannot be handled by the selected parent/interface mode
- illegal or ambiguous inheritance situations
- field name/type conflicts and duplicate generated field targets
- duplicate Java method signatures with incompatible return types
- ambiguous overloaded stereotypes without a signature
- enum constant order conflicts
- association-derived Java field conflicts, including cardinality and direction ambiguity
- missing manual `forEach` target incarnations

One intentionally strict example is
`CodeAdapterTestCase1`: for mapping `stud`, `Observer` maps to `WiMi` and
`Observable` maps to `StudentData`. The intended association is
`WiMi -> StudentData (observes)`, but `GL -> WiMi (observes)` also creates a
role-derived field named `observes` involving the mapped concrete type `WiMi`.
Without cdconcretization there is no deterministic rename or removal step, so
manual adaptation reports an `association role field conflict`.

## Verification

The cdconcretization-derived oracle. A correct case means:

- final Java files exist
- no final Java file contains `@Adapt` or the `Adapt` import
- generated Java compiles
- generated Java structure matches the expected `*Out.cd` for Java-expressible elements

Exact Java source text is not the main oracle, but generated Java should still be readable and consistently spaced.

## Current Limitations

The 8 disabled cdconcretization-derived cases. They cover cases that need additional semantics or test design before they should become required Java-code adaptation behavior.

- Static delegation and static method adaptation need explicit Java-level handling.
- Interface multiple-incarnation support may need stronger selection rules.
- Underspecified parameter or return types without a concrete incarnation may need type hints or a stricter diagnostic.
- Association subtype targets needs mapping rules.
