# Internals

[Back to the overview](../README.md)

This page is for developers extending the adapter. Start with the [quick start](QuickStart.md) to run it first.
For the full module structure, layer boundaries, and design rules, see [Architecture](Architecture.md).

## Adaptation pipeline

1. Paths are validated, then `JavaLoader` loads both CDs and their symbol tables.
   `CDModelIndex` indexes the reference and original concrete models.
2. In completion mode, `ConcretizationService.completeConcreteCDInPlace` completes
   a working copy and rebuilds its symbols. Completion failure stops adaptation;
   the working CD may already be persisted as described below.
3. `AdaptationContextFactory` constructs one context per mapping. Completion mode
   prefers usable conformance mappings and otherwise derives a manual context
   for that mapping. Manual mode does not create a conformance checker.
4. `CodeAdapter` reads reference Java, creates validators and selects each
   mapping's transitive Java-only helper closure in both modes.
5. `AdaptationConflictDetector` checks all manual-mode mappings, or only the
   fallback mappings in completion mode. Java validation follows. These checks
   finish before a Java staging workspace is created.
6. `CDImportProjector.project` adds required CD imports to reference Java ASTs.
   `AdaptationWorkspace` then creates staging.
7. For each mapping, `CodeAdapter` clones and splits compilation units, filters
   relevant sources, and plans passes for concrete incarnation type selections.
8. `MappingAdaptationRunner` executes each isolated pass. One
   `BasicUpdateHandler.handleUpdate` call handles variables, members, roles,
   types and member projection; these are phases within a pass, not separate
   passes for each element kind.
9. Pass results are split, filtered and merged. Concrete Java is also split
   before composition. The selected API merges members, emits adapted code
   directly, or invokes `TopCodeComposer` for inheritance-based separation.
10. Remaining concrete files are copied. If staging contains Java, Spoon removes
    adapter metadata and formats files; JavaDSL cleanup edits invalid import
    declaration ranges without reformatting method bodies.
11. When enabled, the completed CD is included in staging.
    `AdaptationWorkspace` publishes the staged directory, replacing the whole
    previous output tree and attempting restoration on publication failure.

Compilation is a separate verification step in tests and consuming builds.
External regular-generator artifacts must be supplied when the reference Java
depends on them. In completion mode, `persistConcretizedCD` writes the working
CD into the output directory under the input concrete CD's filename immediately
after successful completion, before Java validation/staging. A failed completion
attempts a partial-CD diagnostic there. Successful publication includes the CD
again in staging. These early writes are independent of Java publication;
disabling persistence suppresses working-CD output on success and failure.

## Important Implementation Points

- `JavaLoader.parseCD` must use the same symbol-table setup as `loadCD`, including built-in types. CDs must declare imports explicitly for Java library types such as `Object`, `String`, `List`, or `Optional`.
- `BasicUpdateHandler` handles ordinary and multi-incarnation adaptation through one optional,
  immutable stable-key selection. An empty selection represents the ordinary case.
- `MappingAdaptationRunner` owns the isolated per-pass updater lifecycle, output filtering, merge,
  cleanup, and error wrapping.
- `ReferenceCodeDependencySelector` builds a package-qualified dependency graph
  once from the reference source snapshot and selects mapping-specific helper
  closures in both modes. Ambiguous source-local type resolution is
  rejected deterministically.
- `TopCodeComposer` analyzes all per-type TOP decisions before modifying cloned
  ASTs. It adds or deduplicates HWC inheritance, renames only adapted
  declarations and their constructors, and leaves ordinary references pointing
  to the public concrete type.
- `CDTypeRelations` centralizes direct generated-AST access for interfaces,
  superclasses, modifiers, and type-reference printing. Other helpers use
  limited runtime class inspection for available library types, public
  top-level types and return-type assignability. Source rewriting still works
  on parsed Java; there is no blanket prohibition on `Class.forName`.
- `ManualIncarnationContextBuilder` is the non-mutating context builder for `useConcretization=false`.
- `IncarnationContext` owns one immutable stable-key mapping. MontiCore symbols
  are retained only as mapped-element payload for AST operations; symbol object
  identity is not used for incarnation lookup.
- `AdaptationConflictDetector` validates manual and completion-fallback mappings
  before Java staging.
- `JavaTypeUpdateService` projects concrete field/method incarnations in both
  modes. With completion enabled, it also compares the completed and original
  concrete models to project added or changed fields, methods, enum constants,
  inheritance, abstractness and interface contracts.
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
