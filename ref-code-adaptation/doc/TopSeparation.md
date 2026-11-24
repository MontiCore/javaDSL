# TOP separation

[Back to the overview](../README.md)

TOP separates code you maintain from code produced by a tool. A public class
inherits from a generated base whose name ends in `TOP`. Regeneration updates
the base while your class supplies application-specific behavior.

## The adapter's TOP workflow

Use `adaptWithTopSeparation(...)` or CLI `--top-separation`. The decision is made
for each adapted type:

| Situation | Output |
| --- | --- |
| No matching concrete HWC | Adapted implementation keeps its public name, e.g. `Student` |
| Matching concrete `Student` has no TOP parent | Concrete `Student` gains `extends StudentTOP`; adapted implementation becomes `StudentTOP` |
| Concrete `Student` already extends `StudentTOP` | Existing inheritance is retained and adapted implementation supplies `StudentTOP` |
| Matching concrete interface `Port` exists | Concrete `Port` gains `extends PortTOP` alongside its existing parent interfaces; the adapted interface becomes `PortTOP` |
| Reference implementation already uses TOP and concrete HWC also exists | Three layers: `Student extends StudentTOP extends StudentTOPTOP` |

The last row represents these separate files:

```java
// Concrete handwritten code
public class Student extends StudentTOP { /* application behavior */ }

// Adapted reference handwritten code
public class StudentTOP extends StudentTOPTOP { /* reused behavior */ }

// Externally generated model structure
public abstract class StudentTOPTOP { /* fields and generated methods */ }
```

The adapter produces the first two layers. The consuming project must supply
the third base class. Without concrete HWC, reference `User extends UserTOP`
simply becomes `Student extends StudentTOP`; the consuming project must also
supply that `StudentTOP` base. The adapter does not generate missing model
structure from the CD alone.

For matching interfaces, an existing `PortTOP` parent is retained without
duplication. If the adapted interface already extends its corresponding TOP
interface, that parent is shifted to `PortTOPTOP`, just as for the class case.

## Rejected TOP compositions

When matching concrete HWC requires a TOP companion, composition rejects:

- an enum on either side, or different adapted/concrete declaration kinds
- a `final` adapted class, which cannot become an inherited implementation
- a concrete class with an unrelated existing superclass
- a collision with the newly emitted TOP type
- ambiguous concrete HWC candidates in different packages when no exact
  package-qualified match determines the target

Without matching concrete HWC, an adapted type is emitted directly; these
restrictions on creating a TOP companion do not turn direct emission into TOP
composition.

The internal composer requires one top-level type per adapted compilation unit.
The public adaptation pipeline splits multi-type units before invoking it, so
this is an internal precondition, not a ban on source files with multiple types.

TOP composition does not merge or compare members across the layers:
normal Java inheritance, visibility, and override rules apply. Ordinary type
references remain references to public `Student`. An explicit self-value in the
moved implementation becomes `(Student) this` where needed for public return
types; accesses such as `this.field` keep targeting the inherited implementation.

## Compiling the output

Compile the concrete handwritten classes, adapted implementations, and any
required base classes together. The adapter and CLI do not compile the result
or supply external base classes.
