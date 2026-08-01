# ref-code-adaptation

`ref-code-adaptation` adapts concrete Java code from reference Java code and class-diagram mappings. 
## Inputs

The main entry point is `de.monticore.codeAdaption.CodeAdapter`.

Required inputs:

- reference class diagram
- concrete class diagram
- mapping names, such as `buildPat`, `observer`, or `ref`
- adapter/reference Java source directory
- output directory

The API accepts an optional concrete Java source directory. When it is omitted,
does not exist, or contains no Java files, `CodeAdapter` generates the concrete
Java baseline from the final concrete CD. A supplied directory that contains
Java remains authoritative handwritten concrete code. The CLI still requires
its existing concrete-code argument.

The class diagrams define which reference classes, fields, and methods incarnate as concrete Java elements. Java code is parsed and transformed with MontiCore JavaDSL and Spoon.

## Adaptation Flow

1. Load reference and concrete class diagrams with `JavaLoader.parseCD`.
2. Build mapping-specific incarnation contexts.
3. Validate inputs and detect unresolved or ambiguous mappings before creating
   a staging workspace or changing existing output.
4. Copy and filter reference adapter code for the active mapping.
5. Adapt types, fields, methods, parameters, constructor calls, and pattern-derived members.
6. Merge adapted code with existing concrete code, when present.
7. Clean staged handwritten Java:
   - remove `@Adapt` annotations with Spoon
   - remove invalid generated imports through JavaDSL import declarations
   - preserve valid existing imports without inventing imports for unresolved
     simple names
   - keep Spoon as the only whole-file formatter; import cleanup edits only
     import declaration source ranges
8. If no concrete Java was supplied, serialize the final concrete CD and run
   CD4Code with the staged adapted code as handwritten code. Merge generated
   `*TOP.java` companions and model-only declarations without overwriting the
   staged handwritten implementation.
9. Clean the combined Java tree and publish the completed staging directory
   transactionally, restoring the
   previous output if publication fails.
10. Compile and structurally verify generated Java in tests.

The context-building step depends on the `useConcretization` argument of
`CodeAdapter.adapt(...)`.

### `useConcretization=true`

This mode delegates model repair to cdconcretization before code adaptation:

1. `ConcretizationCompleter` completes the concrete CD by adding safe missing
   model elements, repairing deterministic inheritance gaps, adapting
   multi-incarnation names, and removing redundant inherited attributes.
2. `CDConformanceChecker` validates the completed CD and provides the
   mapping-specific incarnation mapping.
3. `JavaTypeUpdateService` projects Java-expressible elements that were added
   to the completed concrete CD, such as missing fields, methods, types, enum
   constants, inheritance, and interfaces.
4. `ReferenceCodeDependencySelector` retains the transitive source-local
   helper closure of mapped reference types. Required Java-only helper members
   and helper classes do not need ignore annotations; unrelated source units
   are excluded. Reference-CD type uses inside retained helpers are adapted by
   the normal transformation passes.

Use this mode when the concrete CD may need deterministic model-level repair
before Java adaptation.

### `useConcretization=false`

This mode does not mutate or complete the concrete CD. It builds a manual
incarnation context from:

- explicit stereotypes on concrete-CD elements for the active mapping name,
- deterministic same-name rules enabled by `CDConfParameter.NAME_MAPPING`,
- manual `<<forEach="...">>` mappings derived from already-known
  incarnations.

The manual branch does not call `ConcretizationCompleter` or
`CDConformanceChecker`, and it does not clone or create CD elements. Conflicts
are reported together as a `CodeAdaptationException` before staging is created
or existing output is touched.

Supported manual `forEach` targets:

- Type target: `<<forEach="DataClass">> class DataClassBuilder`
- Attribute target: `<<forEach="DataClass.attribute">> any attribute`
- Owner-implied attribute target: `<<forEach="attribute">> any getAttribute()`

For method `forEach` over attributes, concrete method incarnations are derived
from already-mapped concrete attributes and deterministic method names, for
example `getAttribute()` to `getFirstName()` and `getAge()`. The manual mode
does not invent cdconcretization suffix rules; concrete stereotypes or enabled
name rules must make the target deterministic.

#### Manual mappings without concrete Java (R-024)

The no-concrete-code API and manual mapping derivation are independent choices.
This call combines them explicitly:

```java
adapter.adaptWithoutConcreteCode(
    referenceCD,
    concreteCD,
    mappings,
    referenceCodePath,
    outputPath,
    false,  // do not run cdconcretization
    true);  // allow exact common-parent grouping
```

The implementation then follows one transactional pipeline:

1. `AdaptationWorkspace` represents the concrete Java input as absent, while
   both CDs and the reference Java directory remain read-only inputs.
2. Because `useConcretization` is false, the parsed concrete CD is neither
   cloned nor completed. `AdaptationContextFactory` delegates every mapping to
   `ManualIncarnationContextBuilder`.
3. The builder scans concrete types first, then members within their mapped
   owners. An explicit `<<mapping="ReferenceElement">>` wins; enabled
   same-name/signature rules are the deterministic fallback. Reference-side
   `forEach` stereotypes copy or expand mappings already found in those passes.
4. `AdaptationConflictDetector` and strict reference-Java validation run before
   staging. Manual mode does not enable R-026's unannotated-helper policy.
5. Normal isolated mapping passes transform the reference Java. With no
   concrete Java base, this adapted result becomes the staged authoritative
   handwritten code.
6. `ConcreteCodeGenerationService` serializes the unchanged concrete CD and
   runs CD4Code in a separate JVM, using the staged adapted Java as HWC.
   `OutputCodeService` keeps that HWC and adds generated `*TOP.java` companions,
   association fields, and model-only declarations.
7. The combined sources are cleaned and published only after the entire run
   succeeds. Empty reference Java still reaches generation, so the concrete CD
   alone can produce a model baseline.

R-026 is intentionally a concretization-mode feature. Its transitive
Java-helper selection is not used by the manual path above; unmatched manual
helpers must still satisfy the normal manual validation policy.

## Association Adaptation

Associations affect Java through navigable role fields. For example,

```cd
association [1] User -> (roles) Role [*];
```

is generated by the CD4Code configuration as a collection-valued
field comparable to `Set<Role> roles` on `User`. `CodeAdapter` and CD4Code have
separate responsibilities:

- `CodeAdapter` adapts handwritten Java that uses an association. It updates
  endpoint type references and owner-scoped direct role-field reads or writes,
  such as `this.roles`.
- CD4Code creates the role-derived fields with `--fieldfromrole navigable`.
  Association fields are not projected as ordinary CD attributes by
  `CodeAdapter`.

Given `User -> (roles) Role` and the concrete association
`Student -> (roles) HiwiRole`, code such as
`for (Role role : this.roles)` is adapted to use `HiwiRole` while retaining the
role `roles`. If the concrete role is renamed, direct field accesses are renamed
as well. Bidirectional associations are handled per role: a role written on one
association side produces a field on the opposite endpoint, and reads and
assignments are rewritten only inside that owning Java type.

## Important Implementation Points

- `JavaLoader.parseCD` must use the same symbol-table setup as `loadCD`, including built-in types. CDs must declare imports explicitly for Java library types such as `Object`, `String`, `List`, or `Optional`.
- `BasicUpdateHandler` handles ordinary and multi-incarnation adaptation through one optional,
  immutable stable-key selection. An empty selection represents the ordinary case.
- `MappingAdaptationRunner` owns the isolated per-pass updater lifecycle, output filtering, merge,
  cleanup, and error wrapping.
- `ReferenceCodeDependencySelector` builds a package-qualified dependency graph
  once from the reference source snapshot and selects mapping-specific helper
  closures in concretization mode. Ambiguous source-local type resolution is
  rejected deterministically.
- `ConcreteCodeGenerationService` runs CD4Code in an isolated JVM and workspace when
  the API receives no concrete Java. `OutputCodeService` merges its result with
  staged handwritten code, which always has precedence.
- `CDTypeRelations` centralizes direct generated-AST access for interfaces,
  superclasses, modifiers, and type-reference printing. Runtime Java reflection
  is not used by the adapter.
- `ManualIncarnationContextBuilder` is the non-mutating context builder for `useConcretization=false`.
- `IncarnationContext` owns one immutable stable-key mapping. MontiCore symbols
  are retained only as mapped-element payload for AST operations; symbol object
  identity is not used for incarnation lookup.
- `AdaptationConflictDetector` validates manual mappings before Java output is written.
- `JavaTypeUpdateService` fills Java-expressible gaps only after
  cdconcretization, such as missing fields, methods, types, enum constants,
  inheritance, and interfaces.
- `JavaSourcePostProcessor` is the final source cleanup step. It parses JavaDSL
  compilation units for import declarations and removes only known invalid or
  malformed generated imports. It does not infer missing JDK imports from
  unresolved simple names.
- `JavaSourceNames` centralizes Java/CD type naming, generic rendering, arrays,
  primitives, `void`, and `any` normalization. `JavaMethodSignatures` validates
  method names and parameter lists through the JavaDSL grammar rather than
  splitting them manually; invalid signatures remain unmatched. Callers should
  use these utilities instead of open-coded name or signature parsing.
- `SpoonUpdater` is the supported code updater. `RegexUpdater` is preserved
  unchanged as deprecated legacy source for compatibility and is outside the
  supported adaptation path.
- `CodeUpdaterMill` owns updater initialization and isolation. Use `init()` for
  Spoon, `init(Supplier)` for an alternative updater, `getUpdater()` for the
  current pass, and `reset()` between isolated passes.
- Generated builder bodies are represented as structured `MethodBodySpec`
  values, so setter-return and constructor-return methods are built through
  Spoon statements instead of parsed string snippets.
- Concretization compatibility tests inspect and compile the adapter's real output; they do not
  substitute a synthetic CD-to-Java projection.
- Concrete handwritten Java packages are authoritative when an adapted top-level type has one
  unique same-name concrete declaration. The adapted declaration is merged into that package, and
  other adapted units receive imports for types that no longer share their original package.
  Multiple concrete package candidates or conflicting imports are rejected instead of guessed.
- The concretization compilation oracle may stub a missing type only when it is declared by the
  original concrete CD and one concrete Java package is unambiguous. Types introduced only from the
  reference CD are not stubbed, so stale reference names and missing adapter output fail compilation.
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

`CodeAdapterTestCase1` exercises the `stud` and `prof` mappings together and is
expected to adapt successfully. Association-role conflicts that remain
ambiguous are reported before output generation; the testcase itself is no
longer an error-only scenario.

## Verification

The cdconcretization-derived tests use the adapter's generated output as their
oracle input. A correct case means:

- final Java files exist
- no final Java file contains `@Adapt` or the `Adapt` import
- generated Java compiles
- generated Java structure matches the Java-expressible `*Conc.cd` to `*Out.cd`
  completion delta for every type materialized in adapter output, except for the
  explicitly registered model boundaries described below

Exact Java source text is not the main oracle, but generated Java should still be readable and consistently spaced.

## Current Limitations

No fixture is hidden in a generic disabled-case bucket. Three underspecified
elements without an incarnation are explicit rollback/error tests: one attribute,
one method parameter, and one method return type. They are invalid because the
placeholder type `any` cannot be emitted into a concrete CD.

Three fixtures exercise completion semantics that upstream `cdconcretization`
explicitly does not implement. They remain executable rejection/rollback tests:

- attribute `forEach` across inherited declaring owners
- attribute `forEach` without a target incarnation (requires optional-member or
  `matchStructure` semantics)
- method-target `forEach`

Five fixtures still compile and run, but do not use their complete model as a
structural oracle. Each is registered by path and reason in
`CDConcretizationTestCases`: four mirror tests explicitly disabled upstream
because of unresolved association, binding, cross-incarnation, or bidirectional
`forEach` semantics; the fifth is an inherited attribute-type mismatch that the
current concretizer incorrectly accepts. These cases are not skipped locally;
metadata cleanup and strict Java compilation remain mandatory.

Fixtures for which upstream provides no separate `*Out.cd` use their reference
CD as the structural oracle, matching the assertions in the upstream tests.
