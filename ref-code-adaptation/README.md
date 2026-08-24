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

The API can run without a concrete Java source directory. In that case it emits
only Java obtained by adapting the reference implementation; it does not run a
regular model-to-Java generator. `adapt(...)` retains the legacy member-merge
behavior, while `adaptWithTopSeparation(...)` keeps matching concrete HWC and
adapted implementations in separate TOP-related classes. The CLI still exposes
only its existing merge workflow and requires a concrete-code argument.

The class diagrams define which reference classes, fields, and methods incarnate as concrete Java elements. Java code is parsed and transformed with MontiCore JavaDSL and Spoon.

## Adaptation Flow

1. Load reference and concrete class diagrams with `JavaLoader.parseCD`.
2. Build mapping-specific incarnation contexts.
3. Validate inputs and detect unresolved or ambiguous mappings before creating
   a staging workspace or changing existing output.
4. Copy and filter reference adapter code for the active mapping.
5. Adapt types, fields, methods, parameters, constructor calls, and pattern-derived members.
6. Apply the final API-selected composition:
   - merge adapted members into concrete HWC for `adapt(...)`, or
   - keep HWC separate through `Concrete extends ConcreteTOP` for
     `adaptWithTopSeparation(...)`.
7. Copy remaining concrete files and clean staged Java:
   - remove `@Adapt` annotations with Spoon
   - remove invalid generated imports through JavaDSL import declarations
   - preserve valid existing imports without inventing imports for unresolved
     simple names
   - keep Spoon as the only whole-file formatter; import cleanup edits only
     import declaration source ranges
8. Publish the completed staging directory transactionally, restoring the
   previous output if publication fails.
9. Compile and structurally verify generated Java in tests where all external
   regular-generator dependencies are supplied.

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

#### Persisting the concretized CD

The full adapter APIs accept `persistConcretizedCD` as their final boolean
argument:

```java
adapter.adapt(
    referenceCD,
    concreteCD,
    mappings,
    referenceCodePath,
    concreteCodePath,
    outputPath,
    true,   // run cdconcretization
    true,   // allow exact common-parent grouping
    false); // do not persist the concretized CD
```

When `persistConcretizedCD` is `true`, the working concrete CD is written to the
output directory under the input concrete CD's filename. It is also written
when concretization or a later adaptation step fails, so the partial model can
help diagnose the failure. Set the argument to `false` to suppress both the
successful-output file and this failure diagnostic. The argument is ignored
when `useConcretization` is `false`.

Existing overloads without this argument remain available and default to
persisting the CD. The option applies to `adapt(...)`,
`adaptWithoutConcreteCode(...)`, and both `adaptWithTopSeparation(...)` forms.
For the CLI, persistence is enabled with `--concretize` unless
`--no-persist-concretized-cd` is supplied.

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
6. The adapted Java is cleaned and published directly. No model-only types,
   association fields, or generated TOP bases are synthesized. An empty
   reference Java directory therefore produces no Java output.

R-026 is intentionally a concretization-mode feature. Its transitive
Java-helper selection is not used by the manual path above; unmatched manual
helpers must still satisfy the normal manual validation policy.

#### TOP separation

```java
adapter.adaptWithTopSeparation(
    referenceCD,
    concreteCD,
    mappings,
    referenceCodePath,
    concreteCodePath, // omit this argument when no concrete HWC exists
    outputPath);
```

TOP decisions are made per adapted concrete type. Without matching HWC, the
adapted `Concrete` declaration is emitted directly. With matching HWC, the
adapter emits the implementation as `ConcreteTOP` and adds or retains
`extends ConcreteTOP` on handwritten `Concrete`. If ordinary adaptation had
already produced `Concrete extends ConcreteTOP`, the separated implementation
becomes `ConcreteTOP extends ConcreteTOPTOP`.

The four supported cases can be illustrated by adapting the reference
`Builder` pattern to the concrete `Person` entity. The adapted pattern type is
called `PersonBuilder`. The concrete CD containing `Person` is mandatory in
every case; "concrete HWC" below means optional handwritten Java.

1. **Concrete HWC exists but does not use TOP yet.** The adapted and
   handwritten declarations would both be named `PersonBuilder`. The adapter
   moves the adapted implementation to `PersonBuilderTOP` and adds the
   inheritance relationship to the copied HWC:

   ```java
   class PersonBuilder extends PersonBuilderTOP { /* concrete HWC */ }
   class PersonBuilderTOP { /* adapted Builder pattern */ }
   ```

2. **No concrete HWC exists.** There is no Java-name collision, so the adapted
   implementation keeps its ordinary name:

   ```java
   class PersonBuilder { /* adapted Builder pattern */ }
   ```

3. **Concrete HWC already explicitly uses TOP.** The handwritten Java already
   contains `extends PersonBuilderTOP`, but normally no corresponding TOP
   source file exists yet. The adapter retains that relationship and produces
   the expected class from the adapted pattern implementation:

   ```java
   class PersonBuilder extends PersonBuilderTOP { /* concrete HWC */ }
   class PersonBuilderTOP { /* adapted Builder pattern */ }
   ```

   Unlike case 1, the adapter does not need to add the `extends` clause. If a
   concrete `PersonBuilderTOP.java` already exists, producing the adapted class
   with the same identity is rejected as a collision rather than merged or
   overwritten.

4. **The reference code already uses TOP.** For example, reference HWC may
   declare `Builder extends BuilderTOP`. Without concrete HWC, adaptation emits
   `PersonBuilder extends PersonBuilderTOP` unchanged. If concrete HWC also
   owns `PersonBuilder`, the adapted reference HWC is moved into the middle
   layer:

   ```java
   class PersonBuilder extends PersonBuilderTOP { /* concrete HWC */ }
   class PersonBuilderTOP extends PersonBuilderTOPTOP {
     /* adapted reference HWC */
   }
   ```

   Renaming the adapted declaration to `PersonBuilderTOP` would otherwise
   create the invalid self-inheritance `PersonBuilderTOP extends
   PersonBuilderTOP`. The adapter therefore changes that parent reference to
   `PersonBuilderTOPTOP`. It does not generate the actual
   `PersonBuilderTOPTOP` class; an external regular generator must supply it.

The adapter renames only the implementation declaration and its constructors;
ordinary references remain references to public `Concrete`. Fields and methods
are not merged or compared across the two classes. Consequently, Java's normal
override, hiding, and type-checking rules apply to the resulting hierarchy.
Explicit `this` values in an implementation moved to `ConcreteTOP` are emitted
as `(Concrete) this`. This preserves public self-return types and fluent APIs;
targets such as `this.field` remain unchanged so they still select the adapted
TOP member. Exact package-qualified HWC matches also work for generated pattern
types, such as `PersonBuilder`, that are not declarations in the concrete CD.
`ConcreteTOPTOP` and other regular-generator artifacts must be supplied
externally when the selected reference-code structure requires them.

## Association Adaptation

Associations affect Java through navigable role fields. For example,

```cd
association [1] User -> (roles) Role [*];
```

can be generated by an external CD4Code configuration as a collection-valued
field comparable to `Set<Role> roles` on `User`. `CodeAdapter` and CD4Code have
separate responsibilities:

- `CodeAdapter` adapts handwritten Java that uses an association. It updates
  endpoint type references and owner-scoped direct role-field reads or writes,
  such as `this.roles`.
- An external CD4Code invocation can create the role-derived fields with
  `--fieldfromrole navigable`.
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
- `TopCodeComposer` analyzes all per-type TOP decisions before modifying cloned
  ASTs. It adds or deduplicates HWC inheritance, renames only adapted
  declarations and their constructors, and leaves ordinary references pointing
  to the public concrete type.
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
