# ref-code-adaptation Architecture

This document describes the intended architecture of `ref-code-adaptation`.
It records the module boundaries that keep adapter orchestration, matching,
validation, model analysis, and code rewriting independent from each other.

The code adapter has one central responsibility: transform handwritten
reference Java code into concrete Java code according to class-diagram
incarnation mappings. The transformation combines two different models:

- class diagrams, parsed as MontiCore CD ASTs
- Java source code, parsed as JavaDSL ASTs and rewritten through a `CodeUpdater`

The architecture separates those models from the orchestration flow. A class
that decides which CD element maps to which concrete element does not know how
Spoon renames Java fields. A class that renames Java fields does not know how
manual incarnation conflicts are detected. This separation keeps the adapter
usable with different updater implementations and keeps model-level reasoning
testable without source-rewriting side effects.

## Layer Overview

The module is organized into these layers:

1. **Entry and orchestration**
   Coordinates one adaptation run from input files to output files.

2. **Model loading and indexing**
   Parses CD and Java sources and exposes precomputed CD relationships.

3. **Mapping and matching**
   Interprets adapter annotations, stereotypes, name rules, and incarnation
   contexts.

4. **Validation and conflict detection**
   Checks whether an adaptation run is deterministic before a staging
   workspace is created or existing output is changed.

5. **Adaptation handlers**
   Convert mapping decisions into updater operations for Java AST elements.

6. **Updater facade and updater implementations**
   Apply source-code changes through Spoon or another implementation.

7. **Post-processing and verification helpers**
   Clean generated Java and support tests with structural oracles.

The allowed dependency direction is top-down:

```text
CodeAdapter
  -> context builders / validators / handlers / output merger
  -> matchers / CD indexes / Java collectors / updater interface
  -> concrete updater implementation
```

Concrete updater code stays behind the `CodeUpdater` interface. Handler and
orchestration code talks to `CodeUpdater`, not to Spoon.

## Packages

### `de.monticore.codeAdaption`

This package contains the public entry point and CLI wrapper.

- `CodeAdapter` is the API entry point for Java adaptation.
- `CodeAdapterTool` is the CLI integration.
- `CodeAdaptationException` is the user-facing failure type for deterministic
  adaptation conflicts.
- `AdaptedCodeMerger` owns Java AST filtering, splitting, deduplication, and
  merging after isolated mapping runs.
- `OutputCodeService` owns final generated-source cleanup and concrete
  handwritten-file inclusion.
- `AdaptationWorkspace` owns normalized path validation, safe temporary
  directories, rollback, and transactional output publication.

The entry package coordinates the workflow. It owns high-level sequencing:

- parse reference and concrete CDs
- delegate optional concrete-CD completion to the context layer
- delegate incarnation context construction to the context layer
- validate mappings before destructive output changes
- run one or more mapping adaptation passes
- merge adapted Java with concrete handwritten Java through `AdaptedCodeMerger`
- invoke final cleanup and concrete file copying through `OutputCodeService`

The entry package does not own low-level rewriting. It delegates Java changes to
`CodeUpdater`, mapping lookups to context builders and validators, and CD
relationship lookups to `CDModelIndex`.

`CodeAdapter` plans mapping passes with immutable stable-key selections and accumulates their
results. `MappingAdaptationRunner` owns the complete isolated pass operation: cloning reference
Java ASTs, temporary mapping directories, updater initialization, handler execution, output
filtering, merge, cleanup, and error wrapping.

#### `AdaptedCodeMerger`

`AdaptedCodeMerger` is package-private because it is part of the orchestration
implementation, not the public API. It owns all Java compilation-unit decisions
that happen after a handler has produced adapted code:

- `filterCodeForMapping(...)` keeps only Java units relevant for the current
  mapping or explicitly ignored by adapter metadata.
- `splitCompilationUnitsByType(...)` turns one JavaDSL compilation unit with
  multiple top-level types into one unit per top-level type, preserving package
  declarations and assigning a source file name per generated type.
- `mergeAdaptedCode(...)` accumulates generated units from several mappings and
  merges same-file units through `AdapterUtils.mergeAsts(...)`.
- `mergeAdaptedCodeIntoConcreteBase(...)` overlays adapted pattern code on top
  of concrete handwritten Java and filters out reference-only template artifacts. When a unique
  same-name concrete declaration moves an adapted type into another package, the merger reconciles
  imports in dependent adapted units before changing package identity. Ambiguous concrete packages
  and conflicting simple-name imports are rejected.
- `deduplicateBySimpleName(...)` keeps one unit per simple file name, preferring
  the unit with the deeper package-relative path.

This class depends on JavaDSL AST traversal, `CodeValidator`,
`IncarnationContext`, `AdapterUtils`, `CDModelIndex`, and `JavaSourceNames`.
It does not create updaters, run conformance checks, or write files.

#### `OutputCodeService`

`OutputCodeService` is package-private and owns the final filesystem-facing
operations after Java AST generation:

- `cleanCode(...)` creates a fresh updater and delegates implementation-specific
  cleanup to `CodeUpdater.cleanCode(...)`.
- `copyConcreteFiles(...)` includes concrete handwritten files that were not
  already produced by adaptation.
- `concreteCopyTarget(...)` preserves package-based Java output paths when a
  concrete Java file parses successfully. Non-Java files preserve their original
  relative paths. Unparsable concrete Java is rejected because its authoritative
  package identity cannot be determined safely.

This class keeps file-copying and cleanup out of `CodeAdapter`. It does not
inspect adaptation mappings and does not merge ASTs.

### `de.monticore.codeAdaption.context`

The context package contains services that prepare model-level adaptation
context before Java rewriting starts.

- `MappingConformanceService` creates `CDConformanceChecker` instances and runs
  conformance checks with the adapter's fail-quick handling.
- `ConcretizationService` wraps `ConcretizationCompleter` and owns the
  concretization-mode fail-quick policy.
- `AdaptationContextFactory` builds one `IncarnationContext` per mapping. It
  uses manual contexts directly in manual mode and chooses conformance-derived
  or stereotype-derived contexts in concretization mode.
- `GroupingMappingService` computes concrete-type-simple-name to
  grouping-type-simple-name replacements for one mapping context, preventing
  grouping decisions from leaking between mappings.

This package operates on CD ASTs, conformance checkers, and incarnation
contexts. It does not parse Java code, create updater instances, or mutate the
output directory.

The context package is the boundary between model-level preparation and
Java-level adaptation. Manual mode and concretization mode meet here: both
produce the same `Map<String, IncarnationContext>` shape for later workflow
phases.

### `de.monticore.codeAdaption.matcher`

Matchers translate annotation and naming information into `CodeMatching`
objects. They answer the question:

```text
Which reference CD element(s) does this Java AST element represent?
```

Important matcher groups:

- annotation matchers read explicit `@Adapt` metadata
- name matchers use same-name relationships
- infix matchers support generated names that contain the reference name
- composition matchers combine multiple matching strategies
- ignore and error matchers encode non-adapted and invalid cases

Matchers do not mutate Java code. They produce mapping information consumed by
validators and handlers.

### `de.monticore.codeAdaption.validator`

`CodeValidator` owns Java-side validation and exposes matched elements to the
handlers. It initializes matchers for the current Java AST set and provides
queries such as:

- matched type for a Java type declaration
- matched field for a Java field declaration
- matched method for a Java method declaration
- matched local variable or method parameter
- matched supertype usage

Validator classes and CoCos check adapter annotation syntax and semantic
validity. They do not write output files and do not use Spoon.

### `de.monticore.codeAdaption.handler`

Handlers convert validated mapping information into updater operations.

`BasicUpdateHandler` handles both ordinary and multi-incarnation passes. Its optional immutable
selection maps stable reference keys to mapped concrete elements; the empty selection is the
ordinary case. It coordinates three coarse-grained collaborators: `ConcreteSymbolResolver`
resolves stable pass selections, concrete symbols, and method signatures,
`JavaMemberUpdateService` owns variables, members, supertypes, and association-role rewrites, and
`JavaTypeUpdateService` owns type renaming, generation, and completed-CD member projection. The
handler is final and exposes no subclass variation points; its package-local forwarding methods
exist only for those collaborators.

Handlers are the bridge between model reasoning and Java rewriting. They know
both CD concepts and Java AST concepts, but they still depend only on the
updater interface.

### `de.monticore.codeAdaption.handler.multiIncarnation`

This package owns incarnation context construction, selection, and manual
conflict detection.

Core concepts:

- `StableElementKey` is the stable, model-independent key for types, fields,
  methods, and enum constants.
- `IncarnationContext` is the single immutable source of truth for one mapping.
  It maps stable reference keys to `MappedElement` values containing the stable
  concrete key and the concrete symbol needed by AST operations. Symbols are
  payload and never lookup keys.
- `IncarnationContextBuilder` builds contexts from cdconcretization results and
  stereotype overlays.
- `ManualIncarnationContextBuilder` builds contexts without mutating or
  completing the concrete CD.
- `AdaptationConflictDetector` validates manual mappings before output is
  removed. It is a facade over `AdaptationConflictCheck` strategies in the
  `handler.multiIncarnation.conflict` package.
- adaptation passes select concrete types by stable key; member selection is derived directly from
  the selected stable concrete owner.

This package operates on CD ASTs and symbols. It does not read or write Java
source files.

#### Conflict Checks

Manual conflict detection is strategy-based. `ConflictDetectionContext` owns
the read-only model indexes, mapping names, incarnation contexts, conformance
parameters, type maps, and common-parent flag. `ConflictCollector` owns message
formatting and deterministic sorting.

The default registry `DefaultConflictChecks` runs these checks:

- `ManualMappingConflictCheck` for explicit type, field, and method mapping
  conflicts.
- `ConcreteTypeStructureConflictCheck` for invalid inheritance shapes and
  inheritance cycles.
- `ConcreteMemberConflictCheck` for duplicate concrete members and inherited
  field conflicts.
- `EnumConflictCheck` for enum constant order conflicts.
- `AssociationConflictCheck` for association direction, cardinality, and role
  field conflicts.
- `UnderspecifiedTypeConflictCheck` for unresolved `any` types in manual mode.
- `AmbiguousStereotypeConflictCheck` for overloaded method stereotypes without
  signatures.
- `ForEachConflictCheck` for `forEach` declarations without concrete
  incarnations.

`AdaptationConflictDetector` exposes one static validation path over this fixed built-in set.
Test-only injection of additional checks is not part of the production API.

### `de.monticore.codeAdaption.updater`

The updater package defines Java source-rewriting capabilities.

`CodeUpdater` is the facade used by orchestration and handlers. It contains the
operations the rest of the adapter needs:

- rename Java types, fields, methods, variables, parameters, and supertypes
- update CD-only type references
- register owner-aware method rewrites and concrete invocation signatures
- add or remove fields, methods, and generated types through one field-generation operation and one
  structured `MethodBodySpec` method-generation operation
- write the current Java model
- clean generated source code

The interface is intentionally broader than simple renaming because cdconcretization and template
expansion create Java elements that were not present in the reference code. It contains only
capabilities used by production orchestration and handlers. Spoon decides internally whether a
generated method must remain signature-only; that implementation detail is not part of the updater
contract. Unsupported defaults remain only where the protected `RegexUpdater` compatibility class
cannot implement Spoon-backed generation behavior.

`CodeUpdaterMill` owns one synchronized updater provider and one current updater
instance. `init()` restores Spoon, `init(Supplier)` configures an interchangeable
implementation, `getUpdater()` lazily creates the updater for one pass, and
`reset()` discards that instance while retaining its provider. The mill does not
use thread-local state because complete adaptations are serialized around the
process-global MontiCore mills and symbol scopes. `MappingAdaptationRunner` and
`OutputCodeService` own exception-safe reset boundaries for isolated passes and
final cleanup respectively.

### `de.monticore.codeAdaption.updater.spoonUpdater`

`SpoonUpdater` is the supported updater implementation. It parses Java files
with Spoon, maps MontiCore Java AST elements to Spoon elements, applies
refactorings, creates missing members, and prints generated Java.

Spoon-specific responsibilities stay in this package:

- Spoon model setup and output
- Spoon refactoring calls
- Spoon type-reference creation
- Spoon method-body construction
- Spoon cleanup of adapter annotations

`SpoonUpdater` is a facade over four coarse-grained collaborators:

- `SpoonWorkspace` owns model/factory lifecycle, printing, cleanup, and shared
  type-reference construction.
- `SpoonTransformationService` owns declaration and variable renames, grouping, and
  type-reference transformations.
- `SpoonExecutableRepairService` owns invocation renames, overload-aware method rewrites,
  concrete signature repair, and interface executable validity.
- `SpoonGenerationService` owns type/member creation, method bodies, and removal.

`SpoonElementResolver` owns JavaDSL-to-Spoon element lookup and resets its caches
when a new workspace is loaded. The transformation and executable-repair services share the same
workspace and resolver instances. Grouping/type transformations run before executable repair, and
the grouping map is passed read-only for that repair pass rather than cached twice. The facade
keeps its public compatibility wrappers.

Other packages do not import `spoon.reflect.*`.

### `de.monticore.codeAdaption.updater.regexUpdater`

`RegexUpdater` is protected deprecated legacy source. It remains outside the
main architecture path and is preserved unchanged for source compatibility;
its historical text replacement is not a supported updater implementation.
New production code uses `SpoonUpdater` through `CodeUpdater`.

### `de.monticore.codeAdaption.utils`

Utility classes provide shared infrastructure:

- `JavaLoader` parses and prints Java/CD inputs.
- `CDModelIndex` precomputes CD type, member, owner, inheritance, and
  association relationships.
- `CDTypeRelations` centralizes direct access to CD type relationships.
- `JavaSourceNames` centralizes Java/CD type rendering, signature keys, simple
  names, generics, arrays, primitives, and `any` normalization.
- `JavaMethodSignatures` validates method lookup keys with JavaDSL. Parameter
  lists are represented as one synthetic generic type so the grammar, not
  comma/depth string logic, decides whether they are valid.
- `JavaSourcePostProcessor` performs final import cleanup.
- `AdapterUtils` contains AST merge and file-name helpers.

Utilities are shared only when they are stable and domain-neutral. New behavior
that belongs to a specific phase belongs in the package for that phase, not in
`utils`.

### `de.monticore.codeAdaption.utils.visitors`

Visitors collect Java AST elements and remove adapter metadata. They provide
small traversal helpers rather than adaptation decisions.

`JavaAstElemCollector` gives handlers stable access to type declarations,
fields, methods, local variables, parameters, and supertypes in one Java
compilation unit.

## Runtime Adaptation Flow

The main workflow has these phases:

1. `CodeAdapter` parses the concrete and reference CDs through `JavaLoader`.
2. The reference CD and original concrete CD receive immutable indexes. In
   concretization mode, `ConcretizationService` completes a cloned concrete CD
   once for all mappings and publishes the clone only after success. In manual
   mode, the concrete CD remains unchanged.
3. The completed concrete CD receives one immutable index. The reference and
   completed indexes are reused by context construction and every Java
   adaptation pass; the original concrete index remains available only for
   pre-completion decisions.
4. `AdaptationContextFactory` builds one incarnation context for each mapping.
5. Manual mode and any stereotype-derived concretization fallback run
   `AdaptationConflictDetector` before staging or output publication begins.
6. `GroupingMappingService` computes common-parent grouping replacements when
   common-parent adaptation is enabled.
7. `CodeAdapter` creates conformance checkers and Java validators for each
   mapping.
8. `AdaptationWorkspace` creates an isolated staging directory only after all
   preflight checks succeed.
9. Reference Java source is parsed into JavaDSL ASTs.
10. `CodeAdapter` plans zero, one, or many isolated passes using immutable stable-key selections;
    the ordinary case is one pass with an empty selection.
11. `AdaptedCodeMerger.filterCodeForMapping(...)` keeps the Java units relevant
   to the active mapping.
12. `MappingAdaptationRunner` configures one `BasicUpdateHandler` for the pass and runs the updater
    lifecycle in a temporary output directory.
13. `AdaptedCodeMerger.splitCompilationUnitsByType(...)` gives generated
   top-level types separate Java units.
14. `AdaptedCodeMerger.mergeAdaptedCode(...)` accumulates results from all
   mapping passes.
15. `AdaptedCodeMerger.mergeAdaptedCodeIntoConcreteBase(...)` overlays adapted
   Java on concrete handwritten Java.
16. `OutputCodeService.cleanCode(...)` removes adapter annotations and invalid
    generated imports in staging.
17. `OutputCodeService.copyConcreteFiles(...)` copies remaining concrete
    handwritten files into staging.
18. `AdaptationWorkspace` atomically publishes staging and restores the prior
    output on publication failure.

The workflow protects user output by performing conflict detection before
staging and replacing output only after every mapping and cleanup phase
succeeds. Complete `CodeAdapter.adapt(...)` calls are serialized because the
MontiCore mills and symbol scopes used throughout a run are process-global.

## Manual Mode and Concretization Mode

The adapter supports two modes.

### Manual Mode

Manual mode uses stereotypes, deterministic name rules, and manual `forEach`
rules. It does not call cdconcretization and does not mutate the concrete CD.

Manual mode requires strict conflict detection because there is no model-level
repair phase. `AdaptationConflictDetector` reports ambiguous or unsafe cases
before output files are touched.

### Concretization Mode

Concretization mode calls `ConcretizationCompleter` before code adaptation.
`ConcretizationService` owns this call and the fail-quick handling around it.
It rebuilds the completed clone's symbol table after mutation so later indexes,
conformance checks, and contexts observe added model elements.
After completion, `AdaptationContextFactory` uses conformance mappings when
available and falls back to manual stereotype-derived contexts when conformance
information is missing. Such fallback mappings discard the failed checker and
must pass the same conflict detector as manual mode before staging.

Completed-CD projection is part of `JavaTypeUpdateService`. It writes
Java-expressible elements introduced by the completed CD into generated Java
while sharing generated-type tracking and template-member handling with the
rest of type generation.

## Clean Layer Boundaries

The following dependency rules keep the layers separated:

- `CodeAdapter` coordinates phases but does not inspect individual Java members
  for renaming details.
- `CodeAdapter` delegates context construction, grouping-map calculation,
  adapted-code merging, output cleanup, and concrete-file copying to dedicated
  collaborators.
- `BasicUpdateHandler` calls `CodeUpdater` but does not import Spoon.
- `JavaMemberUpdateService` owns both ordinary member changes and
  association-derived role field rewrites so their target-detection and
  exclusion rules stay together.
- `CodeUpdater` does not know how incarnation contexts are built.
- `SpoonUpdater` does not parse CD mappings or call matchers.
- `AdaptationConflictDetector` reports model conflicts but does not clean,
  copy, or print Java files.
- `CDModelIndex` exposes model relationships and avoids repeated full-CD scans
  throughout adaptation logic.
- `JavaSourceNames` owns string rendering of names and types, while
  `JavaMethodSignatures` owns signature lookup keys. Open-coded signature
  parsing or generic type printing stays out of handlers and validators.
  Malformed signature text is not partially normalized into a potentially
  valid lookup key.
- `CDTypeRelations` uses direct CD AST APIs for type relationships; runtime
  reflection is not part of normal adaptation logic.

## R-017 Refactoring State

Requirement R-017 reduces oversized classes by responsibility, not by arbitrary
line slicing. The current refactoring state is:

| Class | Current role | Current state |
| --- | --- | --- |
| `CodeAdapter` | Public API, pass planning, and adaptation-run orchestration | Context creation, grouping maps, merge logic, cleanup, concrete file copying, and isolated pass execution have dedicated collaborators. It is 543 physical lines after pass execution moved to `MappingAdaptationRunner`; pass calculation remains close to orchestration. |
| `MappingAdaptationRunner` | Isolated mapping-pass execution | Package-private runner for clone, print, updater/handler lifecycle, output filtering, merge, cleanup, and wrapped diagnostics. |
| `AdaptedCodeMerger` | Java AST filtering, splitting, deduplication, and merge policy | Extracted from `CodeAdapter`; package-private orchestration helper. |
| `OutputCodeService` | Final cleanup and concrete handwritten-file inclusion | Extracted from `CodeAdapter`; package-private orchestration helper. |
| `AdaptationContextFactory` | Mapping-to-context construction for manual and concretization modes | Extracted into `de.monticore.codeAdaption.context`. |
| `MappingConformanceService` | Safe conformance checker creation and execution | Extracted into `de.monticore.codeAdaption.context`. |
| `ConcretizationService` | Concrete CD completion and fail-quick handling | Extracted into `de.monticore.codeAdaption.context`. |
| `GroupingMappingService` | Mapping-local concrete-to-grouping replacement map with conflict rejection | Extracted into `de.monticore.codeAdaption.context`. |
| `BasicUpdateHandler` | Unified handler pipeline coordinator | About 160 lines. Accepts an optional immutable stable-key selection, delegates resolution/member/type changes, and has no compatibility constructors or subclass hooks. |
| `AdaptationConflictDetector` | Manual-mode model conflict aggregation | A roughly 60-line static facade over the built-in `AdaptationConflictCheck` strategies under `handler.multiIncarnation.conflict`. |
| `SpoonElementResolver` | JavaDSL-to-Spoon declaration lookup and caches | Extracted from `SpoonUpdater`; owns type/method matching and cloned-type cache registration. |
| `SpoonUpdater` | Concrete `CodeUpdater` facade backed by Spoon | About 184 lines after generation overloads were consolidated. Delegates to the workspace, transformation, generation, and element-resolution collaborators. |
| `SpoonTransformationService` | Spoon declaration/variable renames, grouping rewrites, and type-reference transformations | About 376 lines after executable repair was separated. It retains the lazy type-reference indexes and resolved-library safeguards. |
| `SpoonExecutableRepairService` | Invocation and concrete-signature repair | About 300 lines. It shares the workspace/resolver, treats resolved overload declarations as authoritative, and owns no duplicate type or grouping cache. |
| `IncarnationContext` | Immutable stable-key incarnation and grouping lookup | About 70 lines. Owns the only runtime incarnation representation; MontiCore symbols are mapped-element payload only. |
| `IncarnationContextBuilder` | Conformance mappings with optional stereotype overlay | About 140 lines. Populates the stable-key context directly through `IncarnationContextSupport`. |
| `ManualIncarnationContextBuilder` | Non-mutating manual and `forEach` mapping construction | About 240 lines. Uses stable-key lookups throughout, including `forEach` expansion. |
| `IncarnationContextSupport` | Shared stable-key mapping, stereotype, reference-index, and grouping infrastructure | About 380 lines. It builds one canonical map and one immutable implementer-to-grouping index; it has no symbol-identity registry. |

Most production Java classes are below the 500-physical-line target. R-017 remains
open because `CodeAdapter` is the known deferred exception; its next split should
follow the responsibilities described above. `CodeAdapter` and the updater
mill retain their documented entry points. Handler, context-builder, and conflict-detector helpers
are internal implementation surfaces and retain only production-used entry points. The
concretization oracle compares Java-expressible completion deltas from the
original `*Conc.cd` to `*Out.cd` for materialized adapter types and compiles
actual adapter output rather than generating a synthetic substitute. Golden
models that depend on completion behavior absent from the current upstream
dependency, or that cannot describe valid concrete Java, are registered with a
fixture-specific reason; those cases still pass the strict compilation oracle.

## Data Ownership

### CD ASTs

Reference and concrete CD ASTs are owned by the adaptation run. In manual mode,
both ASTs are read-only after parsing. In concretization mode, the concrete CD
is completed before indexing and adaptation.

### Incarnation Contexts

An `IncarnationContext` belongs to one mapping. Stable reference keys map to
immutable lists of mapped concrete elements. Each mapped element retains its
stable key and MontiCore symbol, but symbols never participate in identity or
fallback lookup. An adaptation pass optionally selects mapped elements with another immutable
stable-key map. A single immutable implementer-to-grouping index supports common-parent handling.

### Java ASTs

`CodeAdapter` parses reference Java into JavaDSL ASTs. Each isolated mapping
pass works on a deep clone of those ASTs. The `CodeUpdater` implementation owns
the source model it rewrites and prints.

### Output Files

Temporary mapping directories and the final merged output live in an isolated
staging workspace. Existing output remains untouched until successful
transactional publication.

## Error Handling

Conflict detection errors are aggregated into `CodeAdaptationException` so users
receive all manual-mode problems at once.

Conformance and concretization failures are logged and converted into fallback
paths where deterministic manual mapping information exists. Fallback behavior
does not hide ambiguity; unsafe cases are still rejected by conflict detection.

Updater errors indicate source-rewriting failures and are allowed to fail the
current adaptation pass.

## Testing Architecture

The test suite uses several verification levels:

- adaptation tests assert generated files and compile generated Java
- cdconcretization-derived tests compare materialized generated Java structure
  against the expected `*Conc.cd` to `*Out.cd` completion delta, with every
  non-applicable golden model registered with an explicit reason
- conflict detector tests assert deterministic diagnostics for unsafe manual
  cases
- updater behavior is covered through end-to-end adaptation scenarios

The concretization compilation oracle treats the original concrete CD as the boundary for
external generated dependencies. It prefers real adapted and handwritten Java sources and creates
a minimal stub only for an original concrete-CD type in one unambiguous concrete package. It never
completes the stub set from the reference CD. This makes stale reference types, wrong packages, and
missing adapter-generated types visible as compilation failures.

Structural verification is preferred over exact source text comparison. Source
formatting remains the responsibility of Spoon and final post-processing.
