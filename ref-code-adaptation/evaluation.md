# Ref-Code-Adaptation Evaluation Cases

This document describes the numbered evaluation resources under
`src/test/resources/de/monticore/codeAdaption/evaluation`. Each case contains a
reference CD, a concrete CD, and either adapter handwritten code, concrete
handwritten code, or both.

## Case Overview

| Case | Purpose | Inputs | Mappings | Expected behavior | Test coverage |
| --- | --- | --- | --- | --- | --- |
| `design_patterns` | Composite and Adapter examples imported from the evaluation-paper branch. | `Composition.cd` or `Adapter.cd`, `DesignPatterns.cd`, matching `hwc/` directory | `ci` and `re`, or `npg` | Adapts the paper's Composite example for two mappings and its Adapter example. | `DesignPatternTest.adaptsCompositePatternForBothMappings`, `DesignPatternTest.adaptsAdapterPattern` |
| `testcase_1` | University/user-role case with two mappings over shared concrete types. | `Reference.cd`, `Concrete.cd`, `reference/` | `stud`, `prof` | Adapts both mappings successfully. The former association-role conflict is resolved by treating roles as generated fields owned by the opposite association side. | `CodeAdapterTestCase1` |
| `testcase_2_cd4code` | CD4Code user-role case with generated follow-up code. | `Reference.cd`, `Concrete.cd`, `hwc/` | `stud` | Adapts the reference handwritten code to `Student`/`HiwiRole` and then runs Java generation for the concrete CD. | `CodeAdapterTestCase2a` |
| `testcase_6_builder_pattern` | Builder pattern expansion for multiple concrete target types. | `Reference.cd`, `Concrete.cd`, `adapter/`, `concrete/` | `buildPat` | Creates `PersonBuilder` and `TaskBuilder`, adapts setters/build methods, and keeps existing concrete classes. | `BuilderPatternAdapterTest` |
| `testcase_7_controller_worker_observer` | Combined builder and observer adaptation where workers are observers. | `Reference.cd`, `Concrete.cd`, `adapter/`, `concrete/` | `buildPat`, `observer` | Generates/adapts controller and worker builder/observer code while preserving concrete worker classes. | `ControllerWorkerAdapterTest` |
| `testcase_8_controller_worker_observer_reversed` | Variant of controller/worker observer adaptation with reversed observer modelling. | `Reference.cd`, `Concrete.cd`, `adapter/`, `concrete/` | `buildPat`, `observer` | Adapts the same pattern combination when the observer interface/class relation is represented differently. | `ControllerWorkerAdapterReversedTest` |
| `testcase_9_strategy_pattern` | Strategy pattern adaptation. | `Reference.cd`, `Concrete.cd`, `adapter/`, `concrete/` | `strategy` | Adapts context and strategy types/methods to payment processing classes and strategies. | `StrategyAdapterTest` |
| `testcase_10_singleton_pattern` | Singleton pattern adaptation for multiple concrete singleton classes. | `Reference.cd`, `Concrete.cd`, `adapter/`, `concrete/` | `singleton` | Produces singleton-style code for `DatabaseConnection` and `Logger` and preserves the application code. | `SingletonAdapterTest` |
| `testcase_11_template_method_pattern` | Template method adaptation with several concrete processors. | `Reference.cd`, `Concrete.cd`, `adapter/`, `concrete/` | `template` | Adapts the abstract template and concrete processor methods for CSV, JSON, and XML processors. | `TemplateMethodAdapterTest` |
| `testcase_12_strategy_singleton_pattern` | Combined singleton and strategy adaptation. | `Reference.cd`, `Concrete.cd`, `adapter/`, `concrete/` | `singleton`, `strategy` | Adapts configuration manager singleton behavior and storage strategy variants. | `StrategySingletonAdapterTest` |
| `testcase_13_template_observer_pattern` | Combined template method and observer adaptation. | `Reference.cd`, `Concrete.cd`, `adapter/`, `concrete/` | `template`, `observer` | Adapts data pipeline template methods and observer notification/update code. | `TemplateObserverAdapterTest` |
| `testcase_14_adapter_factory_combined` | Combined adapter and factory pattern resource case. | `Reference.cd`, `Concrete.cd`, `adapter/`, `concrete/` | `adapter`, `factory` | Adapts a shipping port/carrier adapter with factory creation. | `CombinedPatternEvaluationTest` |
| `testcase_15_composite_decorator_combined` | Combined composite and decorator pattern resource case. | `Reference.cd`, `Concrete.cd`, `adapter/`, `concrete/` | `composite`, `decorator` | Adapts renderable components, groups, and decorators. | `CombinedPatternEvaluationTest` |
| `testcase_16_observer_command_combined` | Combined observer and command pattern resource case. | `Reference.cd`, `Concrete.cd`, `adapter/`, `concrete/` | `observer`, `command` | Adapts event bus observers that are also workflow commands and a command queue invoker. | `CombinedPatternEvaluationTest` |
| `testcase_17_fulfillment_platform` | Large fulfilment-platform evaluation required by R-011. | `Reference.cd`, `Concrete.cd`, `adapter/`, deliberately incomplete `concrete/` | `observer`, `command`, `strategy`, `adapter` | Derives all mappings manually, supplies missing pattern members through adapted reference HWC, compiles the 27-type combined system, and executes its end-to-end and queue failure/reuse scenarios. | `LargeFulfillmentSystemEvaluationTest` |

## Notes

- Cases 6 through 13 are covered by pattern-focused JUnit tests in the main
  `de.monticore.codeAdaption` test package.
- Cases 14 through 16 are covered by executable wrappers in
  `CombinedPatternEvaluationTest`.
- Case 17 is the large multi-pattern R-011 evaluation. Its dedicated test
  verifies that adaptation, rather than concrete-source copying, supplies the
  mapped behavior.

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
