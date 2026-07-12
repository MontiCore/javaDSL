# Requirements Protocol

Last updated: 11.07.2026

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
  - Notes: New code depends on `SpoonUpdater` or the `CodeUpdater` interface.
    The compatibility class must never perform source-wide regex rewriting.

- [x] R-005: Keep updater implementations interchangeable
  - Source/date: 18.06.2026
  - Details: Code adaptation must depend on updater interfaces, not on a
    concrete Spoon implementation in the adaptation workflow. The updater must
    be injectable through the `adapt` entry point so Spoon can be replaced by
    another updater implementation without changing the adapter orchestration.
  - Implemented: [x]
  - Addressed: [x]
  - Notes: Spoon remains the default implementation. R-015 moved updater
    interchangeability to `CodeUpdaterMill.init(Supplier)`. Every isolated
    mapping/incarnation run receives a fresh updater after a mill reset.

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
  - Notes: Testcase 1 executes the `stud` and `prof` mappings as a successful
    multi-pattern adaptation scenario.

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

- [x] R-009: Document and preserve the package architecture
  - Source/date: 15.06.2026
  - Details: Add `architecture.md` that explains the intention of each package
    in more detail and records the desired separation between adapter
    orchestration, handlers, matchers, validators, utilities, and updaters.
  - Implemented: [x]
  - Addressed: [x]
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

- [x] R-015: Replace the updater factory with an updater mill
  - Source/date: 23.06.2026
  - Details: Replace `CodeUpdaterFactory` / updater-factory usage with a
    mill-style lifecycle that exposes `init`, `getUpdater`, and `reset`.
    The mill should own updater initialization and provide the current updater
    to the adaptation workflow without leaking concrete Spoon construction into
    orchestration code.
  - Implemented: [x]
  - Addressed: [x]
  - Notes: `CodeUpdaterMill` owns one synchronized provider/current-updater
    lifecycle. Complete adaptations are serialized because MontiCore mills and
    symbol scopes are process-global; the updater lifecycle does not claim
    thread isolation. Each isolated pass and final cleanup obtains a fresh
    updater and resets it in a `finally` block. `init(Supplier)` configures an
    alternative provider, while `init()` restores Spoon.

- [x] R-016: Update the adaptation workflow
  - Source/date: 23.06.2026
  - Details: Rework the documented and implemented adaptation workflow so it
    reflects the last changes.
  - Implemented: [x]
  - Addressed: [x]
  - Notes: The documented and implemented workflow now includes preflight
    validation, safe mapping workspaces, staging, cleanup, and transactional
    publication.

- [x] R-017: Refactor classes larger than 500 lines
  - Source/date: 23.06.2026
  - Details: Refactor classes that exceed 500 lines, especially `CodeAdapter`,
    `AdaptationConflictDetector`, `BasicUpdateHandler`, and `SpoonUpdater`.
    Split responsibilities into logical categories, helper classes, or dedicated
    strategy objects.
  - Implemented: [x]
  - Addressed: [x]
  - Notes: All production Java classes are below 500 physical lines. The four
    remaining hotspots are coordinators backed by coarse-grained collaborators:
    handler symbol/member/type services, Spoon workspace/transformation/executable-repair/
    generation services, and one shared incarnation-context support class. All production
    sources compile against the project classpath. Focused regressions were
    added for path safety, logger state, package identity, nested types, Spoon
    resolution, and validation outcomes.

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

- [x] R-021: Evaluate association adaptation coverage and oracle strength
  - Source/date: 26.06.2026
  - Details: Check how well the adapter handles associations in
    cdconcretization-backed and manual adaptation modes.
  - Implemented: [x]
  - Addressed: [x]
  - Notes: Association cases run against the real adapter output. Association
    endpoint and role-field types are no longer made compilable by copying all
    CD types into every Java package; the stricter behavior is recorded in
    R-022.

- [x] R-022: Make generated-output compilation and package reconciliation strict
  - Source/date: 10.07.2026
  - Details: The concretization compatibility tests must compile the adapter's
    real output without creating broad stubs that hide stale reference names,
    wrong packages, or missing adapter-generated types. When adapted types are
    merged with concrete handwritten Java in another package, dependent adapted
    units must receive deterministic imports. Ambiguous package identities must
    be rejected instead of guessed.
  - Implemented: [x]
  - Addressed: [x]
  - Problem: The former oracle completed the concrete CD from the reference CD
    and generated every resulting model type in every package found in the
    compilation sources. For example, this invalid output could pass:

    ```java
    package adapted.banking;

    class Transaction {
      PrivateAccount source;
    }
    ```

    The real handwritten type was `concrete.banking.PrivateAccount`, but the
    oracle invented an additional `adapted.banking.PrivateAccount` stub. The
    Java compiler therefore did not reveal the missing import. A surviving
    reference name such as `ReferenceAccount` could be hidden in the same way
    when it was copied from the completed reference model.
  - Expected: Real handwritten Java and real adapter output are authoritative.
    An original concrete-CD type may be represented by a minimal test stub only
    when one concrete package is unambiguous. Types introduced only through 
    reference-CD completion are not stubbed.
    Therefore the example above must either become:

    ```java
    package adapted.banking;

    import concrete.banking.PrivateAccount;

    class Transaction {
      PrivateAccount source;
    }
    ```

    or fail compilation.
  - Package reconciliation: `AdaptedCodeMerger` already moved an adapted
    declaration into the package of a unique same-name handwritten concrete
    type. It now performs this as a two-pass operation: first determine all
    final type locations, then add JavaDSL import declarations to dependent
    adapted units before changing packages and merging. Generated simple-name
    placeholder imports are replaced with their qualified targets.
  - Conflict behavior: If concrete Java declares the same model type in more
    than one package, or an existing import uses the required simple name, 
    adaptation fails with `CodeAdaptationException`. No package
    is selected by path order or filename order.
  - Oracle behavior: Compilation sources and generated stubs are indexed by
    fully qualified type name. Stub declaration conflicts are rejected. The
    oracle has negative regressions for stale reference types and wrong-package
    concrete types, plus a positive regression for a legitimate original
    concrete-CD dependency supplied by an external generation step.
