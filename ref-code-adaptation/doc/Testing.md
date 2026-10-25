# Testing and known boundaries

[Back to the overview](../README.md)

Run all adapter tests from the repository root:

```shell
gradle :ref-code-adaptation:test
```

On Windows, use `gradle.bat` instead. Tests need a JDK, including the Java compiler.
The build uses Java 21 and an installed Gradle (8.8 is used for this project).

To run just the CLI and association/TOP examples:

```shell
gradle :ref-code-adaptation:test --tests de.monticore.codeAdaption.CodeAdapterToolTest --tests de.monticore.codeAdaption.evaluation.CodeAdapterTestCase2a
```

The association/TOP examples run in manual and completion modes. They also
assert conformance of the relevant CDs: the original inputs for manual mode,
and the persisted completed CD for completion mode. This test-level assertion
does not change the production manual pipeline into a checker-based mode.

The Student TOP examples compile and execute the composed Java. They check
both the manual `roles` field and the completed `hiwiRoles` field, plus a manual
case whose concrete CD explicitly declares `hiwiRoles`. The tests supply the
base classes needed for compilation; supplying those bases is outside the
production adapter's scope. See [TOP separation](TopSeparation.md).

## Internal subpackages

Run the dedicated regression with:

```shell
gradle :ref-code-adaptation:test --tests de.monticore.codeAdaption.InternalSubpackageAdaptationTest
```

Its [fixtures](../src/test/resources/de/monticore/codeAdaption/internalSubpackages)
contain internal `model` and `service` CD packages. Mapping targets use the
qualified names `model.Entity` and `service.Service`. The completer successfully
adds the missing primitive field and method inside the corresponding concrete
packages, and the result passes conformance checking.

The Java adaptation test then renames and relocates reference types from
`reference.model` and `reference.service` into `application.model` and
`application.service`. A required Java-only helper in the deeper
`reference.service.internal` package is retained, its model-type import is
updated, and an unrelated helper is excluded. Both merge and TOP composition
are compiled against only the emitted sources and the JDK, then executed to
check the returned value. The input models and helper source are preserved.

A separate executable regression records a current completion limitation:
adding the qualified cross-package method signature
`model.Entity echo(model.Entity entity)` makes completion produce
`model.Customer echo(model.Customer customer)`, but its conformance check
rejects the return-type incarnation. The expected failure is asserted.

The same qualified signature is supported in manual mode with a complete
concrete CD. Four regression cases cover merge and TOP composition, each with
an explicit Java `@Adapt` method mapping and with name matching alone. They
require exactly one adapted `Customer echo(Customer customer)` declaration,
preserve its body and call, compile the emitted Java independently, and execute
`value()` to check that it returns `7`. Method projection checks whether the
updater reused the original declaration before deciding to remove a template.
This Java adaptation fix does not resolve the separate conformance rejection
in completion mode.

These tests use unique simple
type names and do not establish support for same-named types in different CD
packages or every Java import form.

For inherited-member cleanup and its separate Java boundary, see
[Inheritance cleanup](InheritanceCleanup.md).

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
