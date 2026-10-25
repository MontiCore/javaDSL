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

Existing concrete files that collide with a newly emitted `StudentTOP` are
rejected. TOP composition does not merge or compare members across the layers:
normal Java inheritance, visibility, and override rules apply. Ordinary type
references remain references to public `Student`. An explicit self-value in the
moved implementation becomes `(Student) this` where needed for public return
types; accesses such as `this.field` keep targeting the inherited implementation.

## Compiling the output

Compile the concrete handwritten classes, adapted implementations, and any
required base classes together. The adapter and CLI do not compile the result
or supply external base classes.
