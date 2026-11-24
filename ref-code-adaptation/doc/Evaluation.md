# Ref-Code-Adaptation Evaluation Cases

[Back to the overview](../README.md)

This document lists adaptation scenarios and model-level regressions under
[`src/test/resources/de/monticore/codeAdaption/evaluation`](../src/test/resources/de/monticore/codeAdaption/evaluation).
Adaptation cases contain reference/concrete CDs and relevant handwritten Java.
Conformance-only cases contain just the diagrams.

## Case Overview

| Case | Purpose | Inputs | Mappings | Expected behavior | Test coverage |
| --- | --- | --- | --- | --- | --- |
| `design_patterns` | Composite and Adapter examples imported from the evaluation-paper branch. | `Composition.cd` or `Adapter.cd`, `DesignPatterns.cd`, matching `hwc/` directory | `ci` and `re`, or `npg` | Adapts the paper's Composite example for two mappings and its Adapter example. | `DesignPatternTest.adaptsCompositePatternForBothMappings`, `DesignPatternTest.adaptsAdapterPattern` |
| `testcase_1` | University/user-role case with two mappings over shared concrete types. | `Reference.cd`, `Concrete.cd`, `reference/` | `stud`, `prof` | Adapts both mappings successfully. Association-role accesses belong to the owner on the opposite association side. | `CodeAdapterTestCase1`; model-conflict checks in `AdaptationConflictDetectorTest` |
| `testcase_2_cd4code` | User-role adaptation and TOP composition with external base classes. | `Reference.cd`, `Concrete.cd`, `hwc/` | `stud` | Adapts reference HWC to `Student`/`HiwiRole` without invoking a regular generator. Tests separately supply external bases to compile and exercise TOP output. | `CodeAdapterTestCase2a` |
| `testcase_6_builder_pattern` | Builder expansion for multiple target types. | `Reference.cd`, `Concrete.cd`, `adapter/`, `concrete/` | `buildPat` | Creates `PersonBuilder` and `TaskBuilder`, adapts setters/build methods and keeps concrete classes. | `BuilderPatternAdapterTest`; mapping checks in `IncarnationContextBuilderTest` |
| `testcase_7_controller_worker_observer` | Combined builder/observer adaptation where workers are observers. | `Reference.cd`, `Concrete.cd`, `adapter/`, `concrete/` | `buildPat`, `observer` | Adapts controller and worker builder/observer code while preserving concrete workers. | `PatternAdapterIntegrationTest.adaptsControllerWorkerPatterns` |
| `testcase_8_controller_worker_observer_reversed` | Controller/worker variant with reversed observer modelling. | `Reference.cd`, `Concrete.cd`, `adapter/`, `concrete/` | `buildPat`, `observer` | Adapts the combination with the reversed observer interface/class relation. | `PatternAdapterIntegrationTest.adaptsControllerWorkerPatterns` |
| `testcase_9_strategy_pattern` | Strategy adaptation. | `Reference.cd`, `Concrete.cd`, `adapter/`, `concrete/` | `strategy` | Adapts context and strategy types/methods to payment processing. | `PatternAdapterIntegrationTest.adaptsStrategyPattern` |
| `testcase_10_singleton_pattern` | Multiple concrete singleton classes. | `Reference.cd`, `Concrete.cd`, `adapter/`, `concrete/` | `singleton` | Adapts singleton behavior for `DatabaseConnection` and `Logger` and preserves application code. | `PatternAdapterIntegrationTest.adaptsSingletonPattern` |
| `testcase_11_template_method_pattern` | Template method adaptation with several processors. | `Reference.cd`, `Concrete.cd`, `adapter/`, `concrete/` | `template` | Adapts abstract template and concrete CSV/JSON/XML processor methods. | `PatternAdapterIntegrationTest.adaptsTemplateMethodPattern` |
| `testcase_12_strategy_singleton_pattern` | Combined singleton and strategy adaptation. | `Reference.cd`, `Concrete.cd`, `adapter/`, `concrete/` | `singleton`, `strategy` | Adapts configuration-manager singleton behavior and storage strategies. | `PatternAdapterIntegrationTest.adaptsStrategyAndSingletonTogether` |
| `testcase_13_template_observer_pattern` | Combined template method and observer adaptation. | `Reference.cd`, `Concrete.cd`, `adapter/`, `concrete/` | `template`, `observer` | Adapts pipeline template methods and observer notifications. | `PatternAdapterIntegrationTest.adaptsTemplateAndObserverTogether`; mapping checks in `IncarnationContextBuilderTest` |
| `testcase_14_adapter_factory_combined` | Combined adapter and factory pattern resource case. | `Reference.cd`, `Concrete.cd`, `adapter/`, `concrete/` | `adapter`, `factory` | Adapts a shipping port/carrier adapter with factory creation. | `CombinedPatternEvaluationTest` |
| `testcase_15_composite_decorator_combined` | Combined composite and decorator pattern resource case. | `Reference.cd`, `Concrete.cd`, `adapter/`, `concrete/` | `composite`, `decorator` | Adapts renderable components, groups, and decorators. | `CombinedPatternEvaluationTest` |
| `testcase_16_observer_command_combined` | Combined observer and command pattern resource case. | `Reference.cd`, `Concrete.cd`, `adapter/`, `concrete/` | `observer`, `command` | Adapts event bus observers that are also workflow commands and a command queue invoker. | `CombinedPatternEvaluationTest` |
| `testcase_17_fulfillment_platform` | Large fulfilment-platform evaluation required by R-011. | `Reference.cd`, `Concrete.cd`, `adapter/`, deliberately incomplete `concrete/` | `observer`, `command`, `strategy`, `adapter` | Derives all mappings manually, supplies missing pattern members through adapted reference HWC, compiles the 27-type combined system, and executes its end-to-end and queue failure/reuse scenarios. | `LargeFulfillmentSystemEvaluationTest` |
| `association_role_usage_mi` | Association roles with multiple incarnations. | `Reference.cd`, `Concrete.cd`, `adapter/`, `concrete/` | `ref` | Adapts role reads to `mathDepts`/`scienceDepts` and their concrete endpoint types. | `evaluation.AssociationPatternAdaptationTest.adaptsMultiIncarnationAssociationRoleUsage` |
| `association_bidirectional_role_write` | Role writes on both owners of a bidirectional association. | `Reference.cd`, `Concrete.cd`, `adapter/`, `concrete/` | `ref` | Adapts direct assignments to both concrete association roles. | `evaluation.AssociationPatternAdaptationTest.adaptsBidirectionalAssociationRoleWritesOnBothOwners` |
| `single_file_multi_pattern` | Two mappings adapt the same reference Java class. | `Reference.cd`, `Concrete.cd`, `reference/MyClass.java` | `pattern1`, `pattern2` | Manual and completion modes preserve both mapping-specific fields while emitting the shared renamed field and method once; the merged Java compiles. | `CodeAdapterPipelineRegressionTest.mergesIndependentMappingsOfTheSameReferenceClass` |
| `confCheckingCD` | Conformance with inherited role attributes and a shared superclass. | `Reference.cd`, `Concrete.cd`; no handwritten Java | `prof`, `stud` | Both mappings conform with stereotype/inheritance/association policies. Enabling `NAME_MAPPING` additionally maps the shared `Role` base and rejects its missing association incarnation. | `context.MappingConformanceServiceTest.inheritedRoleConformanceDependsOnNameMapping` |

## Notes

- Case 6 is covered by `BuilderPatternAdapterTest`. Cases 7 through 13 use
  `PatternAdapterIntegrationTest` in `de.monticore.codeAdaption`; its
  parameterized controller/worker test covers both cases 7 and 8.
- Cases 14 through 16 are covered by executable wrappers in
  `CombinedPatternEvaluationTest`.
- Case 17 is the large multi-pattern R-011 evaluation. Its dedicated test
  verifies that adaptation, rather than concrete-source copying, supplies the
  mapped behavior.

The adapter never invokes a regular CD-to-Java generator in these scenarios.
Any generated external bases used to verify case 2 are supplied by test
support, separately from production adaptation.

`single_file_multi_pattern` checks that separate mapping passes start from the
same reference implementation and combine their results without losing
mapping-specific members or duplicating shared ones.

`confCheckingCD` is a conformance-only regression. Its shared `Role` superclass
supplies the inherited `name` field for `HiwiRole` and `TeachingRole`. The
parameterized test records acceptance without `NAME_MAPPING` and rejection
with it for both mappings; it does not assert that every policy set conforms.

## TOP Composition Cases

These cases are decided independently for every adapted type. In the examples,
the reference `Builder` pattern is adapted to `PersonBuilder`; the concrete CD
containing `Person` remains mandatory, while concrete handwritten Java is
optional.

| Case                                                                    | Expected composition | Tests that assert it |
|-------------------------------------------------------------------------| --- | --- |
| 1. Concrete HWC exists without a TOP superclass.                        | The copied HWC becomes `PersonBuilder extends PersonBuilderTOP`; the adapted implementation becomes `PersonBuilderTOP`. | `TopCodeComposerTest.createsTopCompanionWithoutMergingMembersOrRewritingPublicTypeReferences` |
| 2. No matching concrete HWC exists.                                     | The adapted implementation is emitted directly as `PersonBuilder`; no TOP layer is introduced by composition. | `TopCodeComposerTest.emitsAdaptedTypeDirectlyWhenConcreteHwcIsMissing` |
| 3. Concrete HWC already declares `extends PersonBuilderTOP`.            | The existing inheritance is retained and the adapted implementation supplies `PersonBuilderTOP`. Fluent public self types continue to compile. | `TopCodeComposerTest.preservesFluentPublicSelfTypeInGeneratedTopCode`, `BuilderPatternAdapterTest.topSeparationCompilesFluentBuilderWithConcreteHwc` |
| 4a. Reference code uses TOP and no matching concrete HWC exists.        | The adapted `PersonBuilder extends PersonBuilderTOP` is emitted unchanged; no TOPTOP layer is needed. | `TopCodeComposerTest.keepsExistingReferenceTopWhenNoConcreteHwcExists`, `CodeAdapterTestCase2a.adaptsAssociationRoleUsageWithoutRunningARegularGenerator` |
| 4b. Reference code uses TOP and concrete HWC also owns `PersonBuilder`. | Concrete HWC extends `PersonBuilderTOP`; adapted reference HWC becomes `PersonBuilderTOP extends PersonBuilderTOPTOP`. The external generator supplies the actual TOPTOP base. | `TopCodeComposerTest.shiftsReferenceTopToTopTopWhenConcreteHwcOwnsThePublicType`, `CodeAdapterTestCase2a.topApiAddsInheritanceAndEmitsAdaptedImplementationAsTop` |

Case 1 and case 3 have the same final two-class shape. They differ in whether
the adapter adds the HWC `extends PersonBuilderTOP` clause or merely retains the
clause already written by the developer. In case 4, the adapter creates the
adapted `PersonBuilderTOP` middle layer and only references—not generates—the
external `PersonBuilderTOPTOP` base.
