# ref-code-adaptation Architecture

[Back to the overview](../README.md)

This document describes the current responsibilities and intended boundaries of
`ref-code-adaptation`. It also records existing dependency exceptions and
outstanding refactoring work.

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
   workspace is created. Working-CD persistence has a separate early-write
   policy, described under Output Files.

5. **Adaptation handlers**
   Convert mapping decisions into updater operations for Java AST elements.

6. **Updater facade and updater implementations**
   Apply source-code changes through Spoon or another implementation.

7. **Post-processing and verification helpers**
   Clean generated Java and support tests with structural oracles.

The main control flow is top-down:

```text
CodeAdapter
  -> context builders / validators / handlers / output merger
  -> matchers / CD indexes / Java collectors / updater interface
  -> concrete updater implementation
```

Handlers request Java mutations through `CodeUpdater` without importing Spoon
model types. The physical package dependencies are not a strict top-down graph:

- `ReferenceCodeDependencySelector` directly constructs
  `SpoonReferenceCodeDependencyAnalyzer`. Its parser-independent result keeps
  Spoon model types out of orchestration, but the selector depends on the
  concrete analyzer package.
- The updater interface and Spoon services use `StableElementKey`, currently
  located in `handler.multiIncarnation`, as a shared method-identity value.
- The Spoon analyzer returns `dependency.ReferenceCodeSelection`, and
  analysis, context services and conflict detection use the root
  `CodeAdaptationException` type.

These are documented dependency exceptions, not completed layer-isolation
refactorings. Moving shared value types or introducing an analyzer interface
would be separate implementation work.

## Packages

### `de.monticore.codeAdaption`

This package contains the public entry point and CLI wrapper.

- `CodeAdapter` is the API entry point for Java adaptation.
- `CodeAdapterTool` is the CLI integration.
- `MappingAdaptationRunner` owns the isolated per-mapping/per-pass lifecycle.
- `HelperVariantRegistry` deduplicates identical helper variants across passes
  and mappings, rejecting structurally divergent variants.
- `CodeAdaptationException` is the user-facing failure type for deterministic
  adaptation conflicts.
- `AdaptedCodeMerger` owns Java AST filtering, splitting and merging by
  package-qualified identity. It also exposes package relocation
  without member merging for TOP composition.
- `TopCodeComposer` performs a read-only per-type analysis and then creates
  separate HWC/TOP ASTs. It never globally renames public concrete type
  references.
- `OutputCodeService` owns final source cleanup, TOP self-type repair, and
  concrete handwritten-file and resource inclusion.
- `AdaptationWorkspace` owns normalized path validation, safe temporary
  directories, rollback, and transactional output publication. The concrete
  source is optional at this boundary.

The entry package coordinates the workflow. It owns high-level sequencing:

- parse reference and concrete CDs
- delegate optional concrete-CD completion to the context layer
- delegate incarnation context construction to the context layer
- validate mappings before destructive output changes
- run one or more mapping adaptation passes
- compose final Java through member merging, `directFinalCode`, or `TopCodeComposer`
- invoke final cleanup and concrete file/resource copying through
  `OutputCodeService`
- repair explicit self values in generated TOP implementations against their
  public HWC subtype before transactional publication

The entry package does not own low-level rewriting. It delegates Java changes to
`CodeUpdater`, mapping lookups to context builders and validators, and CD
relationship lookups to `CDModelIndex`.

`CodeAdapter` plans mapping passes with immutable stable-key selections and accumulates their
results. `MappingAdaptationRunner` owns the complete isolated pass operation: cloning reference
Java ASTs, temporary mapping directories, updater initialization, handler execution, output
filtering, merge, cleanup, and error wrapping.

#### `AdaptedCodeMerger`

`AdaptedCodeMerger` is package-private because it is part of the orchestration
implementation, not the public API. It owns Java compilation-unit selection,
splitting and composition before and after handler execution:

- `filterCodeForMapping(...)` keeps only Java units relevant for the current
  mapping or explicitly ignored by adapter metadata.
- `splitCompilationUnitsByType(...)` turns one JavaDSL compilation unit with
  multiple top-level types into one unit per top-level type, preserving package
  declarations and assigning a source file name per generated type.
- `mergeAdaptedCode(...)` accumulates units from several mappings and merges
  matching package-qualified top-level identities through `AdapterUtils.mergeAsts(...)`.
- `mergeAdaptedCodeIntoConcreteBase(...)` overlays adapted pattern code on top
  of concrete handwritten Java, preserving competing concrete declarations.
  It relies on the earlier `filterCodeForMapping` selection rather than filtering
  reference-only artifacts itself. When a unique
  same-name concrete declaration moves an adapted type into another package, the merger reconciles
  imports in dependent adapted units before changing package identity. Ambiguous concrete packages
  and conflicting simple-name imports are rejected.
- `relocateAdaptedCodeToConcretePackages(...)` applies those package/import
  decisions without merging members, allowing `TopCodeComposer` to keep the
  adapted implementation separate.
- Internal `indexAndMerge(...)` indexes units by package-qualified identity.
  Equal simple names in different packages remain distinct; package depth is
  not a deduplication preference.

This class depends on JavaDSL AST traversal, `CodeValidator`,
`IncarnationContext`, `StableElementKey`, `AdapterUtils`, `CDModelIndex`,
`JavaSourceNames`, `JavaLoader`, and `JavaAstElemCollector`.
It does not create updaters, run conformance checks, or write files.

#### `OutputCodeService`

`OutputCodeService` is package-private and owns the final filesystem-facing
operations after Java AST generation:

- `cleanCode(...)` creates a fresh updater and delegates implementation-specific
  cleanup to `CodeUpdater.cleanCode(...)`.
- `copyConcreteFiles(...)` includes concrete handwritten files that were not
  already produced by adaptation. Multi-type units are split and their fragments
  merged with already emitted units through `AdapterUtils.mergeAstsPreferringLeft`,
  preserving declarations already composed into the output.
- `concreteCopyTarget(...)` preserves package-based Java output paths when a
  concrete Java file parses successfully. Non-Java files preserve their original
  relative paths. Unparsable concrete Java is rejected because its authoritative
  package identity cannot be determined safely.

This class keeps final file inclusion and cleanup out of `CodeAdapter`. It does
not select incarnation mappings, but it does merge Java AST fragments when
including concrete files.

### `de.monticore.codeAdaption.dependency`

This package owns reference-source dependency selection in both adaptation modes.

- `ReferenceCodeDependencySelector` is the parser-independent facade used by
  orchestration.
- `ReferenceCodeSelection` is an immutable mapping-specific result containing
  package-qualified roots and helpers, source-local dependency edges, and the
  reference-CD type keys used by retained units, plus sorted non-fatal diagnostics.

The complete graph is built once from the same reference Java snapshot used by
validation. Mapping-specific selection starts at mapped top-level types and
follows transitive source-local type dependencies. Nested, local, and anonymous
classes remain with their top-level owner. Ambiguous source-local identities
fail deterministically; unrelated Java-only units are not retained. The
Spoon-backed analyzer is implemented behind this facade in the Spoon updater
package, so Spoon model types do not escape into orchestration.

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

- `StableElementKey` is the stable, model-independent key for types, fields and
  methods. Enum constants are handled directly during projection, not as a
  separate stable-key kind.
- `IncarnationContext` is the single immutable source of truth for one mapping.
  It maps stable reference keys to `MappedElement` values containing the stable
  concrete key and the concrete symbol needed by AST operations. Symbols are
  payload and never lookup keys.
- `IncarnationContextBuilder` converts conformance-checker incarnation mappings.
  Production calls `buildContextForMapping(mapping, false)` without a stereotype
  overlay; overlay-enabled overloads are currently exercised by tests.
- `ManualIncarnationContextBuilder` builds contexts without mutating or
  completing the concrete CD.
- `AdaptationConflictDetector` validates manual mappings before output is
  removed. It is a facade over `AdaptationConflictCheck` strategies in the
  `handler.multiIncarnation.conflict` package.
- adaptation passes select concrete types by stable key; member selection is derived directly from
  the selected stable concrete owner.

This package operates on CD ASTs and symbols. Conflict checks also use
`JavaLoader.print` to render in-memory model fragments; they do not read or write
Java source files.

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
- update CD-only type references and owner-scoped association-role accesses
- register owner-aware method rewrites and concrete invocation signatures
- add or remove fields and methods, and clone generated types with `addType`
- generate methods with `addMethod`, or project them with `projectMethod`,
  whose boolean result reports whether the original declaration was retained
- add superclass/interface relationships, set abstractness and add enum constants
- configure incarnation-to-group type replacements with `setGroupingMappings`
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
`reset()` discards that instance while retaining its provider.
`MappingAdaptationRunner` and `OutputCodeService` own exception-safe reset
boundaries for isolated passes and final cleanup respectively.

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

`SpoonUpdater` delegates to these services:

- `SpoonWorkspace` owns model/factory lifecycle, printing, cleanup, and shared
  type-reference construction.
- `SpoonTransformationService` owns declaration and variable renames, grouping, and
  type-reference mutations. It uses the workspace's type-reference factory but owns replacement
  policy because replacement changes an existing model.
- `SpoonExecutableRepairService` owns invocation renames, overload-aware method rewrites,
  concrete signature repair, and interface executable validity.
- `SpoonGenerationService` owns type/member creation, method bodies, and removal.

`SpoonElementResolver` owns JavaDSL-to-Spoon element lookup and resets its caches
when a new workspace is loaded. `SpoonTransformationService` calls
`SpoonExecutableRepairService` after type/grouping changes. Both services share the same
workspace and resolver instances. Grouping/type transformations run before executable repair, and
the grouping map is passed read-only for that repair pass rather than cached twice. The facade
keeps its public compatibility wrappers.

Additional implementation helpers in this package are:

- `ExecutableRewriteRegistry`, which stores owner-aware method rewrites and
  legacy concrete-signature registrations used by executable repair.
- `SpoonTypeIdentity`, which renders and resolves qualification-preserving
  signature types.
- `SpoonReferenceCodeDependencyAnalyzer`, constructed by the dependency selector
  rather than the updater facade, which builds the source-local helper graph
  and returns a `ReferenceCodeSelection`.

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
- `CDImportProjector` projects required external CD imports into Java ASTs before
  staging, retaining explicit Java bindings and rejecting ambiguous candidates.
- `AdapterUtils` contains AST merge and file-name helpers.
- `Adapt` defines adapter annotations, `AdaptReference` parses their references,
  and `AdaptAnnotationNames` recognizes supported annotation names.
- `AdapterParam` defines the Java matching/unmatched policies accepted by the
  public `CodeAdapter` constructor; `Constants` holds shared names and placeholders.

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

1. `CodeAdapter` validates paths and parses both CDs through `JavaLoader`.
   The reference and original concrete CD receive indexes.
2. In concretization mode, `ConcretizationService` completes a cloned concrete
   CD once for all mappings and rebuilds its symbols. If persistence is enabled,
   the working CD is written into the output directory immediately after
   successful completion, or attempted as a partial diagnostic on completion
   failure. A completion failure stops adaptation.
3. The working concrete CD receives an index. The original concrete index
   remains available throughout pass planning and completed-member projection,
   where it distinguishes input structure from completion-added structure.
4. `AdaptationContextFactory` builds one context per mapping. Only completion
   mode creates/checks conformance checkers. A usable checker mapping is kept;
   otherwise that mapping receives a manually derived fallback context.
5. `CodeAdapter` parses reference Java, configures validators and computes
   mapping-specific helper selections in both modes.
6. `AdaptationConflictDetector` checks every manual-mode mapping, or only the
   fallback mappings in completion mode.
7. Java validation checks the parsed sources. Both modes use the selected
   helper-preservation policy; annotation/declaration CoCos remain active.
8. `CDImportProjector.project` updates required imports in reference Java ASTs.
   `AdaptationWorkspace` creates staging after preflight validation.
9. For each mapping, grouping replacements are computed if enabled. Reference
   Java is cloned, split into top-level units and filtered by
   `filterCodeForMapping` before pass planning.
10. `CodeAdapter.buildAdaptationPasses` plans concrete incarnation type
    selections and expected output identities. An ordinary pass has an empty
    type selection; these are not separate passes for each Java element kind.
11. `MappingAdaptationRunner` initializes an isolated updater and
    `BasicUpdateHandler` for each selection. The handler processes variables,
    members, association roles, types and member projection in one call.
12. Printed pass results are reparsed, split and filtered; helper variants are
    checked for consistency. `mergeAdaptedCode` accumulates mapping/pass results.
13. Concrete Java, when supplied, is parsed and split before final composition.
14. The selected API chooses `mergeFinalCode` for `adapt`, `directFinalCode` for
    `adaptWithoutConcreteCode`, or `composeWithTopSeparation` for
    `adaptWithTopSeparation`. The latter also has a no-concrete-HWC overload;
    without matching HWC, adapted types keep their ordinary names.
15. Final Java ASTs are written into staging and `copyConcreteFiles` includes
    remaining concrete handwritten files and resources.
16. If staging contains Java files, `OutputCodeService.cleanCode` performs
    metadata removal, formatting, TOP self-type repair and import cleanup.
17. With completion and persistence enabled, the completed CD is also written
    into staging under the input concrete CD's filename.
18. `AdaptationWorkspace.publish` replaces the whole output directory with
    staging and attempts to restore previous output if publication fails.

Java publication follows validation, adaptation, composition and cleanup.
Early working-CD writes are a separate exception to leaving existing output
unchanged; see Output Files below.

## Manual Mode and Concretization Mode

The adapter supports two modes.

### Manual Mode

Manual mode uses stereotypes, deterministic name rules, and manual `forEach`
rules. It does not call cdconcretization and does not mutate the concrete CD.

Manual mode requires strict conflict detection because there is no model-level
repair phase. `AdaptationConflictDetector` reports ambiguous or unsafe cases
before output files are touched.

`ManualIncarnationContextBuilder` constructs the shared context shape consumed
by later handlers in three ordered passes:

1. Map reference type keys to concrete types from explicit stereotypes on the
   concrete CD, falling back to enabled deterministic name rules.
2. Map fields and methods only inside the reference owners established by the
   type pass. Explicit member stereotypes take precedence over same-name or
   same-signature matching.
3. Expand reference-side `forEach` declarations from mappings already known.
   Type/member targets copy their incarnations; attribute-driven methods derive
   exact concrete method-name candidates and still require a matching concrete
   declaration.

Stable keys, rather than symbol-object identity, make the result reusable after
index construction. The builder only records relationships; it creates no
concrete CD element and performs no Java or filesystem mutation.

#### Manual mode with no concrete Java

`adaptWithoutConcreteCode(..., false, ...)` composes the manual mapping path
with direct adapted-code output:

```text
reference CD + concrete CD + reference Java
        |
        v
manual contexts -> conflict/Java validation -> mapping passes
        |
        v
adapted Java in staging -> cleanup -> staged publication
```

The absence of concrete Java does not change how mappings are derived. It only
means that adapted reference Java becomes the complete Java output. The adapter
does not run a regular generator, so an empty reference directory produces no
model-only Java. Both modes select required Java helpers and validate them with
the helper-preservation policy, without adding missing model elements in manual mode.

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

Both modes enable helper-closure preservation. Java-only helper
members and transitively required helper classes may remain unannotated. They
are retained only when reachable from a mapped reference type or an explicitly
ignored root, and reference-CD
type uses within them participate in incarnation-pass planning and normal Spoon
rewriting. Divergent variants of one package-qualified helper produced by
multi-incarnation passes are rejected before AST merging.

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
- `CDTypeRelations` uses direct CD AST APIs for model relationships. Other
  helpers use limited runtime class inspection for library-type existence,
  visibility and assignability, including `Class.forName` without class
  initialization. This is distinct from rewriting source through Java/Spoon ASTs.

## R-017 Refactoring State

Requirement R-017 reduces oversized classes by responsibility, not by arbitrary
line slicing. The current refactoring state is:

| Class | Current role | Current state |
| --- | --- | --- |
| `CodeAdapter` | Public API, pass planning, and adaptation-run orchestration | Context creation, grouping maps, merge logic, cleanup, concrete file copying, Spoon member projection and isolated pass execution have dedicated collaborators. Pass calculation remains close to orchestration because it combines mapping context, source selection and output identity. No regular CD-to-Java generator is invoked. |
| `MappingAdaptationRunner` | Isolated mapping-pass execution | Package-private runner for clone, print, updater/handler lifecycle, output filtering, merge, cleanup, and wrapped diagnostics. |
| `AdaptedCodeMerger` | Java AST filtering, splitting, deduplication, and merge policy | Extracted from `CodeAdapter`; package-private orchestration helper. |
| `OutputCodeService` | Final cleanup and concrete handwritten-file inclusion | Extracted from `CodeAdapter`; package-private orchestration helper. |
| `AdaptationContextFactory` | Mapping-to-context construction for manual and concretization modes | Extracted into `de.monticore.codeAdaption.context`. |
| `MappingConformanceService` | Safe conformance checker creation and execution | Extracted into `de.monticore.codeAdaption.context`. |
| `ConcretizationService` | Concrete CD completion and fail-quick handling | Extracted into `de.monticore.codeAdaption.context`. |
| `GroupingMappingService` | Mapping-local concrete-to-grouping replacement map with conflict rejection | Extracted into `de.monticore.codeAdaption.context`. |
| `BasicUpdateHandler` | Unified handler pipeline coordinator | Accepts an optional immutable stable-key selection, delegates resolution/member/type changes, and has no compatibility constructors or subclass hooks. |
| `AdaptationConflictDetector` | Manual/fallback model conflict aggregation | Static facade over the built-in `AdaptationConflictCheck` strategies under `handler.multiIncarnation.conflict`. |
| `SpoonElementResolver` | JavaDSL-to-Spoon declaration lookup and caches | Extracted from `SpoonUpdater`; owns type/method matching and cloned-type cache registration. |
| `SpoonUpdater` | Concrete `CodeUpdater` facade backed by Spoon | Delegates to workspace, transformation, executable-repair, generation and element-resolution services. |
| `SpoonTransformationService` | Declaration/variable renames, grouping and type-reference transformations | Retains lazy type-reference indexes and resolved-library safeguards; delegates final invocation repair to `SpoonExecutableRepairService`. |
| `SpoonExecutableRepairService` | Invocation and concrete-signature repair | Shares workspace/resolver, treats resolved overload declarations as authoritative, and uses `ExecutableRewriteRegistry` for method rewrites. |
| `IncarnationContext` | Immutable stable-key incarnation and grouping lookup | Owns the runtime incarnation representation; MontiCore symbols are mapped-element payload only. |
| `IncarnationContextBuilder` | Checker-derived incarnation mapping conversion | Production disables stereotype overlays. Overlay-enabled overloads are retained and exercised by tests. |
| `ManualIncarnationContextBuilder` | Non-mutating manual and `forEach` mapping construction | Uses stable-key lookups throughout, including `forEach` expansion. |
| `IncarnationMappingSupport` | Shared mapping, stereotype, reference-index and grouping infrastructure | Builds one canonical map and an immutable implementer-to-grouping index, without a symbol-identity registry. |

R-017 remains open. Nine production classes exceed the
500-physical-line target: `CodeAdapter`, `SpoonGenerationService`, `AdapterUtils`,
`JavaTypeUpdateService`, `AdaptedCodeMerger`, `ConcreteSymbolResolver`,
`JavaSourceNames`, `CodeValidator` and `SpoonExecutableRepairService`. Further
splits should follow responsibilities, not line slicing. Exact line counts are
omitted because they become stale as behavior evolves.

`CodeAdapter` and the updater mill retain their documented entry points.
Internal helpers also retain test-exercised entry points: the overlay-enabled
`IncarnationContextBuilder` overloads and cloning
`ConcretizationService.completeConcreteCD` wrapper are not currently called by
production orchestration. Production uses a caller-owned working copy with
`completeConcreteCDInPlace`. These retained convenience entry points are
distinct from the production call path.

The concretization oracle compares Java-expressible completion deltas from the
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

`AdaptationWorkspace` creates a staging directory beside the output directory
and per-pass `.mapping-` directories inside staging. Publication uses a sibling
backup; Spoon final cleanup has its own temporary transaction directory. There
are no regular CD4Code-generation directories in the production pipeline.

Java adaptation, composition and cleanup finish in staging before publication.
Successful publication replaces the entire output directory, including files
not present in the new result; publication failure attempts restoration.

Working-CD persistence has a separate policy. With concretization and
`persistConcretizedCD` enabled, the concrete input filename is resolved in the
output directory. Successful completion writes the working model there before
Java validation/staging; completion failure attempts to save the partial model
there before rethrowing. Successful publication also includes the completed CD
in staging. Thus a later failure can leave an updated CD alongside existing
Java output. Disabling persistence suppresses all working-CD writes.

## Error Handling

Conflict detection errors are aggregated into `CodeAdaptationException` so users
receive all manual-mode problems at once.

Expected completion rejections (`CompletionException` from the completer or
reported completion error findings) stop adaptation with `CodeAdaptationException`.
Ambiguous grouping uses the same exception; the CLI reports these failures with
exit code `2`. Unexpected completion runtime failures remain `IllegalStateException`
and reach the CLI's exit code `1` path. Neither completion failure category
enters manual fallback. After successful completion, a failed
conformance check or missing usable incarnation mapping makes that mapping use
manual derivation plus conflict checks. Other mappings may retain their usable
checkers. Unsafe manual/fallback cases remain rejected by conflict detection.

Updater errors indicate source-rewriting failures and are allowed to fail the
current adaptation pass. If concretized-CD persistence is enabled, the last
working model remains in the output directory to help explain these failures.

## Testing Architecture

The test suite uses several verification levels:

- adaptation tests assert generated files and compile generated Java
- cdconcretization-derived tests compare materialized generated Java structure
  against the expected `*Conc.cd` to `*Out.cd` completion delta, with every
  non-applicable golden model registered with an explicit reason
- conflict detector tests assert deterministic diagnostics for unsafe manual
  cases
- updater behavior is covered through end-to-end adaptation scenarios and direct
  `SpoonServicesTest` unit tests
- `CodeUpdaterMillTest` directly checks provider configuration and lifecycle
  isolation

The concretization compilation oracle treats the original concrete CD as the boundary for
external generated dependencies. It prefers real adapted and handwritten Java sources and creates
a minimal stub only for an original concrete-CD type in one unambiguous concrete package. It never
completes the stub set from the reference CD. This makes stale reference types, wrong packages, and
missing adapter-generated types visible as compilation failures.

Structural verification is preferred over exact source text comparison. Source
formatting remains the responsibility of Spoon and final post-processing.
