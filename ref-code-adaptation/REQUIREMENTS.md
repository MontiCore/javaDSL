# Requirements Protocol

Last updated: 23.06.2026

## Requirements

- [x] R-001: Preserve the non-concretization adaptation mode
  - Source/date: 09.06.2026
  - Details: `useConcretization=false` must not call cdconcretization, mutate
    the concrete CD, or remove existing output before conflicts are checked.
  - Implemented: [x]
  - Addressed: [x]
  - Notes: This mode relies on explicit stereotypes and deterministic name
    rules. It intentionally reports cases that cdconcretization would repair.

- [x] R-002: Support a cdconcretization-backed adaptation mode
  - Source/date: 09.06.2026
  - Details: `useConcretization=true` should use cdconcretization for safe
    model completion and then project Java-expressible completed CD elements.
  - Implemented: [x]
  - Addressed: [x]
  - Notes: Projection is limited to Java-expressible model additions such as
    fields, methods, inheritance, interfaces, enum constants, and added types.

- [x] R-003: Remove fragile broad source-text Java rewrites
  - Source/date: 11.06.2026
  - Details: Import cleanup, metadata removal, generated method bodies, and
    type/signature normalization should use JavaDSL, Spoon, or shared structured
    helpers rather than open-coded regex and token scanning.
  - Implemented: [x]
  - Addressed: [x]
  - Notes: A tiny malformed-string fallback remains isolated in
    `JavaSourceNames`; generated-source cleanup does not infer unresolved
    simple `java.util` imports.

- [x] R-004: Keep `RegexUpdater` only for compatibility
  - Source/date: 12.06.2026
  - Details: Existing references to `RegexUpdater` should continue to compile,
    but the class must delegate to the Spoon implementation instead of doing
    whole-file `replaceAll` updates.
  - Implemented: [x]
  - Addressed: [x]
  - Notes: New code should depend on `SpoonUpdater` or the `CodeUpdater`
    interface.

- [x] R-005: Keep updater implementations interchangeable
  - Source/date: 18.06.2026
  - Details: Code adaptation must depend on updater interfaces, not on a
    concrete Spoon implementation in the adaptation workflow. The updater must
    be injectable through the `adapt` entry point so Spoon can be replaced by
    another updater implementation without changing the adapter orchestration.
  - Implemented: [x]
  - Addressed: [x]
  - Notes: Spoon remains the default implementation. `CodeAdapter` now accepts a
    `CodeUpdaterFactory` through its `adapt` entry points, and every isolated
    mapping/incarnation run receives a fresh updater instance. The factory-based
    API is superseded by R-015, which requires a mill-style updater lifecycle.

- [x] R-006: Document evaluation test cases
  - Source/date: 15.06.2026
  - Details: Add `evaluation.md` documentation that explains the purpose,
    inputs, mappings, expected behavior of all testcases in the codeAdaptation resources, that are not in the subdirectories (13 test classes).
  - Implemented: [x]
  - Addressed: [x]
  - Notes: `evaluation.md` documents the 13 numbered evaluation resource cases.
    Testcase 1 is documented with the current post-R-007 behavior: it is
    expected to run successfully, not report the former association-role
    conflict.

- [x] R-007: Make evaluation testcase 1 functional
  - Source/date: 15.06.2026
  - Details: Testcase 1 should be converted from an error-only expectation into
    a functioning evaluation test case, or the underlying modeling/adaptation
    conflict should be resolved so the case can run successfully.
  - Implemented: [x]
  - Addressed: [x]
  - Notes: Current analysis target: testcase 1 reports an association-role field
    conflict because role-derived fields from the Observer/Observable mappings
    are ambiguous for the manual adaptation path.

- [x] R-008: Introduce a central CD lookup/index structure
  - Source/date: 15.06.2026
  - Details: Add a central class or data structure, comparable to the
    preprocessing/index approach in cd4analysis, that traverses each class
    diagram AST once and stores frequently used type/member relationships in
    maps.
  - Implemented: [x]
  - Addressed: [x]
  - Notes: Adaptation code should look up CD types, attributes, methods, and
    associations from this index instead of repeatedly iterating the whole AST
    or resolving through utility methods such as `CDUtil`/`CDDiffUtil` at every
    use site.

- [ ] R-009: Document and preserve the package architecture
  - Source/date: 15.06.2026
  - Details: Add `architecture.md` that explains the intention of each package
    in more detail and records the desired separation between adapter
    orchestration, handlers, matchers, validators, utilities, and updaters.
  - Implemented: [ ]
  - Addressed: [ ]
  - Notes: Updater implementations must not be integrated into
    `BasicUpdateHandler` or `CodeAdapter`; the architecture should remain
    well-structured and cleanly separated.

- [ ] R-010: Create a milestone plan
  - Source/date: 15.06.2026
  - Details: Create a Meilensteinplan that breaks the remaining work into
    concrete milestones, including architecture cleanup, updater
    interchangeability, testcase stabilization, documentation, and evaluation.
  - Implemented: [ ]
  - Addressed: [ ]
  - Notes: The plan should be usable for thesis/project tracking.

- [ ] R-011: Add a large multi-pattern evaluation testcase
  - Source/date: 15.06.2026
  - Details: Create a large evaluation testcase that combines different
    adaptation patterns in one scenario so the adapter can be evaluated on
    interactions between patterns rather than only isolated examples.
  - Implemented: [ ]
  - Addressed: [ ]
  - Notes: The testcase should include multiple mappings and pattern styles,
    for example builder/repository, observer, strategy, adapter/factory,
    command, composite, or decorator patterns.

- [ ] R-012: Merge the testcase from MSh and add a test to assert its expected behavior
  - Source/date: 15.06.2026
  - Details: Merge the test case from MSh's branch into the main evaluation
    suite and add an assertion that checks whether the expected error or
    successful output is produced.
  - Implemented: [ ]
  - Addressed: [ ]
  - Notes: The test case should be added to the `evaluation` package and should
    have a clear assertion for its expected outcome, whether it's an error or a
    successful adaptation.

- [ ] R-013: Address the warnings from the IntelliJ inspection report
  - Source/date: 15.06.2026
  - Details: Review the IntelliJ inspection report for the project and address
    any warnings or issues that are relevant to code quality, maintainability,
    or correctness.
  - Implemented: [ ]
  - Addressed: [ ]
  - Notes:

- [ ] R-014: Stabilize the remaining disabled cdconcretization cases
  - Source/date: 20.06.2026
  - Details: Re-enable stale-successful cases and define the required fix path
    for cases that still need model functionality, Java projection behavior,
    fixture repair, or negative-test assertions.
  - Implemented: [ ]
  - Addressed: [ ]
  - Notes: `AssocSubtypeTarget` and `InterfaceMI` were re-enabled after
    successful isolation runs.

- [ ] R-015: Replace the updater factory with an updater mill
  - Source/date: 23.06.2026
  - Details: Replace `CodeUpdaterFactory` / updater-factory usage with a
    mill-style lifecycle that exposes `init`, `getUpdater`, and `reset`.
    The mill should own updater initialization and provide the current updater
    to the adaptation workflow without leaking concrete Spoon construction into
    orchestration code.
  - Implemented: [ ]
  - Addressed: [ ]
  - Notes: The mill should preserve updater interchangeability from R-005 while
    making lifecycle boundaries explicit for single-shot and isolated
    multi-incarnation adaptation runs.

- [ ] R-016: Update the adaptation workflow
  - Source/date: 23.06.2026
  - Details: Rework the documented and implemented adaptation workflow so it
    reflects the last changes.
  - Implemented: [ ]
  - Addressed: [ ]
  - Notes:

- [ ] R-017: Refactor classes larger than 500 lines
  - Source/date: 23.06.2026
  - Details: Refactor classes that exceed 500 lines, especially `CodeAdapter`,
    `AdaptationConflictDetector`, `BasicUpdateHandler`, and `SpoonUpdater`.
    Split responsibilities into logical categories, helper classes, or dedicated
    strategy objects.
  - Implemented: [ ]
  - Addressed: [ ]
  - Notes: `AdaptationConflictDetector` is a strong candidate for pluggable
    conflict-check strategies. `SpoonUpdater` should separate Spoon model setup,
    member/type updates, cleanup, and formatting concerns where practical.

- [ ] R-018: Clarify multi-incarnation mapping versus multi-pattern cases
  - Source/date: 23.06.2026
  - Details: Make the distinction between multi-incarnation mapping and
    multi-pattern adaptation explicit in class names, method names, comments,
    and documentation. Multi-incarnation mapping means one reference concept is
    mapped to multiple concrete incarnations; multi-pattern adaptation means one
    testcase or workflow combines multiple adaptation patterns.
  - Implemented: [ ]
  - Addressed: [ ]
  - Notes: Avoid using "multi-pattern" as a synonym for multi-incarnation and
    avoid hiding multi-pattern testcase behavior behind incarnation-specific
    names.

- [ ] R-019: Rename `JavaSourcePostProcessor`
  - Source/date: 23.06.2026
  - Details: Rename `JavaSourcePostProcessor` to a clearer name that describes
    its actual responsibility in source cleanup and generated Java
    normalization.
  - Implemented: [ ]
  - Addressed: [ ]
  - Notes: Update production code, tests, and documentation references together
    so the old post-processor name does not remain as the public concept.

- [ ] R-020: Improve testcase names
  - Source/date: 23.06.2026
  - Details: Rename unclear testcases and test methods so their names describe
    the scenario, mapping shape, and expected result instead of relying on
    numbers or overly broad labels.
  - Implemented: [ ]
  - Addressed: [ ]
  - Notes: Evaluation testcase resources may keep stable numeric identifiers
    when needed, but test class and method names should communicate intent.
