# Internals

[Back to the overview](../README.md)

This page is for developers extending the adapter. Start with the [quick start](QuickStart.md) to run it first.

## Adaptation pipeline

1. `JavaLoader` loads both CDs and their symbol tables.
2. `AdaptationContextFactory` constructs one incarnation context per mapping.
   [Manual and completion modes](Mappings.md) differ at this stage.
3. Java validation and conflict detection reject unresolved or ambiguous
   mappings before a staging workspace is created.
4. `MappingAdaptationRunner` selects the reference sources for each mapping and
   runs isolated passes over types, fields, methods, parameters, constructor
   calls, and pattern-derived members.
5. The selected API merges with concrete code, emits adapted code directly, or
   invokes `TopCodeComposer` for inheritance-based separation.
6. Remaining concrete files are copied. Spoon removes adaptation annotations
   and formats whole Java files; JavaDSL import cleanup edits import declaration
   ranges without reformatting method bodies.
7. `AdaptationWorkspace` publishes the completed staging directory
   transactionally and restores previous output if publication fails.

Compilation is a separate verification step in tests and consuming builds.
External regular-generator artifacts must be supplied when the reference Java
depends on them. In completion mode, the working CD may also be saved as a
failure diagnostic, independently of Java publication.

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
