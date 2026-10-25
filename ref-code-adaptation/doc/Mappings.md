# Mappings and adaptation modes

[Back to the overview](../README.md)

Adaptation has two mapping steps: connect concrete-CD elements to reference-CD
elements, then connect reference Java to those reference-CD elements.

## Class-diagram mappings

For mapping `ref`, `<<ref="Entity">>` on concrete class `Student` declares that
`Student` is an incarnation of reference class `Entity`. A stereotype on a member
such as `<<ref="id">> String studentId;` maps that field within its owner.
One reference element can have several concrete incarnations. Multiple mapping
names, such as `stud` and `prof`, are processed independently.

`NAME_MAPPING` enables deterministic equal-name matching. `ADAPTED_NAME_MAPPING`
enables the upstream adapted-name conformance policy. These are CD policies;
they are distinct from the Java matching options below. In manual mode, explicit
stereotypes take precedence and enabled same-name rules provide the fallback.

## Reference-Java matching

| Strategy | Example |
| --- | --- |
| `NAME_MATCHING` / CLI `name` | Java `Entity` matches CD `Entity` |
| `INFIX_MATCHING` / CLI `infix` | A Java identifier containing a reference element name can be adapted, e.g. `EntityBuilder` |
| `ANNOTATION_MATCHING` / CLI `annotation` | `@Adapt` explicitly identifies reference elements and a naming template |

For example:

```java
@Adapt(ref = "Role", template = "print${}s")
public String printRoles() { /* reference behavior */ }
```

If `Role` maps to `HiwiRole`, the method name becomes `printHiwiRoles`. Annotation
matching has precedence when enabled. Templates support `${}`, `${cap_first}`,
and `${uncap_first}`, consuming the referenced elements in order. For an
overloaded method, a signature such as `Order.update(int)` selects a specific
overload. `@Adapt(ignore = true)` marks deliberately unchanged code. The adapter
removes adaptation annotations and their imports from final Java output.

## Manual mode: no model completion

This is the default (`useConcretization=false`, no CLI `--concretize`). The
adapter derives mappings from the existing CD without adding or repairing CD
elements. It does not run `ConcretizationCompleter` or `CDConformanceChecker` in
this branch. It validates mapping conflicts before staging Java output.

Manual `forEach` supports type targets, attribute targets, and owner-implied
attribute targets, for example:

```cd
<<forEach="DataClass">> class DataClassBuilder;
<<forEach="DataClass.attribute">> any attribute;
<<forEach="attribute">> any getAttribute();
```

These are individual stereotype examples, not a complete CD. They expand
already-known incarnations. A method associated with reference `attribute` may
become `getFirstName()` and `getAge()` for mapped attributes. Manual mode does
not invent completion-specific suffix rules or missing target elements.

Reported conflicts include missing or ambiguous incarnations, unresolved `any`
types, incompatible type kinds, invalid inheritance, conflicting fields,
duplicate method signatures with incompatible returns, ambiguous overloads,
enum ordering conflicts, and ambiguous association directions or cardinalities.

## Completion mode

Enable `useConcretization=true` or CLI `--concretize` when the concrete CD needs
deterministic completion:

1. `ConcretizationCompleter` adds safe missing model elements and repairs supported
   inheritance and naming gaps in a working CD.
2. `CDConformanceChecker` checks each requested mapping. Its incarnation mapping
   is used when available. If a mapping lacks a usable checker result, the
   context factory falls back to conflict-checked manual derivation for that
   mapping.
3. `JavaTypeUpdateService` projects Java-expressible completion changes such as
   fields, methods, enum constants, types, and inheritance into the adaptation.
4. `ReferenceCodeDependencySelector` retains source-local helpers needed
   transitively by mapped reference types and excludes unrelated source units.

Required Java-only helper members and classes do not need ignore annotations
in completion mode. Manual mode retains its stricter validation policy.

Completion and the choice to supply concrete Java are independent: either mode
can run without a concrete Java directory. Neither mode invokes a general
model-to-Java generator or produces association fields by itself.

By default, completion saves the working CD under the input concrete CD's
filename, including a partial model on failure. Disable this with
`persistConcretizedCD=false` or `--no-persist-concretized-cd`.

## Associations

Navigable association roles correspond to Java field accesses. For example,
reference `User -> (roles) Role` can map to concrete
`Student -> (hiwiRoles) HiwiRole`. Adaptation changes both endpoint type uses
and direct owner-scoped accesses such as `this.roles` to `this.hiwiRoles` when
the mapping supplies that role rename.

For bidirectional associations, each role belongs to the opposite endpoint.
Reads and assignments are changed only in that owning Java type. Creating the
actual collection fields belongs to an external generator; see
[TOP separation](TopSeparation.md).

In the Student case study, the input concrete CD still declares
`Student -> (roles) HiwiRole`. Manual mode preserves this explicit role name,
so the adapted loop accesses `this.roles`. Completion adapts `roles` to
`hiwiRoles` in its working CD, so that loop accesses `this.hiwiRoles` instead.
The original CD is unchanged. The Java class supplying the association field
must expose the field named by the selected mode.
Enabling `NAME_MAPPING` and `ADAPTED_NAME_MAPPING` does not make manual mode
complete or rename the model. The method name `printHiwiRoles()` is independent:
its Java annotation template derives the name from `HiwiRole` in both modes.

When the concrete CD explicitly declares `(hiwiRoles)`, manual mode uses that
name too.

## Imports and packages

Declare Java library imports, such as `java.lang.String` and `java.util.List`,
in the CDs. Final cleanup preserves valid imports and removes known invalid
generated imports; it does not infer missing imports from unresolved names.

When a concrete handwritten class uniquely determines the package of an
adapted type, that package is authoritative. Ambiguous package candidates or
conflicting imports are rejected. [Testing and known boundaries](Testing.md)
describes completion cases that are intentionally rejected.
