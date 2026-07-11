package de.monticore.codeAdaption.handler.multiIncarnation;

import static de.monticore.cdconformance.CDConfParameter.ALLOW_ADDITIONAL_PARAMETERS;
import static de.monticore.cdconformance.CDConfParameter.INHERITANCE;
import static de.monticore.cdconformance.CDConfParameter.NAME_MAPPING;
import static de.monticore.cdconformance.CDConfParameter.STEREOTYPE_MAPPING;
import static de.monticore.cdconformance.CDConfParameter.STRICT_PARAMETER_ORDER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.cdconcretization.ConcretizationCompleter;
import de.monticore.cdconformance.CDConfParameter;
import de.monticore.cdconformance.CDConformanceChecker;
import de.monticore.codeAdaption.AdapterAbstractTest;
import de.monticore.codeAdaption.handler.BasicUpdateHandlerTestAccess;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.codeAdaption.utils.CDModelIndex;
import de.monticore.cddiff.CDDiffUtil;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class IncarnationContextBuilderTest extends AdapterAbstractTest {
  private Set<CDConfParameter> confParameters;

  @BeforeEach
  public void setup() {
    initMills();
    confParameters =
        Set.of(
            NAME_MAPPING,
            INHERITANCE,
            STEREOTYPE_MAPPING,
            STRICT_PARAMETER_ORDER,
            ALLOW_ADDITIONAL_PARAMETERS);
  }

  @Test
  @DisplayName("Builds owner-aware field mappings from stereotypes")
  public void buildsOwnerAwareFieldMappings() {
    ASTCDCompilationUnit refCD =
        JavaLoader.loadCD(
            new File(
                "src/test/resources/de/monticore/codeAdaption/evaluation/testcase_6_builder_pattern/Reference.cd"));
    ASTCDCompilationUnit conCD =
        JavaLoader.loadCD(
            new File(
                "src/test/resources/de/monticore/codeAdaption/evaluation/testcase_6_builder_pattern/Concrete.cd"));

    IncarnationContext context = build(refCD, conCD, "buildPat");
    StableElementKey referenceField = StableElementKey.field("Builder", "attribute", "any");

    List<StableElementKey> targets =
        context.getIncarnations(referenceField).stream()
            .map(IncarnationContext.MappedElement::key)
            .collect(Collectors.toList());

    assertTrue(targets.contains(StableElementKey.field("Person", "name", "String")));
    assertTrue(targets.contains(StableElementKey.field("Person", "age", "int")));
    assertTrue(targets.contains(StableElementKey.field("Task", "status", "String")));
    assertEquals(10, targets.size());
  }

  @Test
  @DisplayName("Builds signature-aware method mappings and grouping types")
  public void buildsSignatureAwareMethodMappingsAndGroupingTypes() {
    ASTCDCompilationUnit refCD =
        JavaLoader.loadCD(
            new File(
                "src/test/resources/de/monticore/codeAdaption/evaluation/testcase_13_template_observer_pattern/Reference.cd"));
    ASTCDCompilationUnit conCD =
        JavaLoader.loadCD(
            new File(
                "src/test/resources/de/monticore/codeAdaption/evaluation/testcase_13_template_observer_pattern/Concrete.cd"));

    IncarnationContext context = build(refCD, conCD, "observer");
    StableElementKey referenceMethod = StableElementKey.method("Observer", "update", List.of(), "void");

    List<StableElementKey> targets =
        context.getIncarnations(referenceMethod).stream()
            .map(IncarnationContext.MappedElement::key)
            .collect(Collectors.toList());

    assertTrue(
        targets.contains(
            StableElementKey.method("PipelineObserver", "onPipelineStage", List.of("String"), "void")));
    assertTrue(
        targets.contains(
            StableElementKey.method("LoggingObserver", "onPipelineStage", List.of("String"), "void")));
    assertTrue(
        targets.contains(
            StableElementKey.method("MetricsObserver", "onPipelineStage", List.of("String"), "void")));
    assertEquals(
        "PipelineObserver",
        context
            .getGroupingFor(StableElementKey.type("LoggingObserver"))
            .orElseThrow()
            .key()
            .getName());
    assertEquals(
        "PipelineObserver",
        context
            .getGroupingFor(StableElementKey.type("MetricsObserver"))
            .orElseThrow()
            .key()
            .getName());
  }

  @Test
  @DisplayName("Can build mappings from concretization plus conformance without stereotype overlay")
  public void buildsCheckerOnlyContextAfterConcretization() throws Exception {
    ASTCDCompilationUnit refCD =
        JavaLoader.loadCD(
            new File(
                "src/test/resources/de/monticore/codeAdaption/cdconcretization/types/valid/TypeMissingRef.cd"));
    ASTCDCompilationUnit conCD =
        JavaLoader.loadCD(
            new File(
                "src/test/resources/de/monticore/codeAdaption/cdconcretization/types/valid/TypeMissingConc.cd"));

    ConcretizationCompleter completer = new ConcretizationCompleter(confParameters);
    completer.completeCD(conCD, refCD, "ref");

    CDConformanceChecker checker = new CDConformanceChecker(confParameters);
    assertTrue(checker.checkConformance(conCD, refCD, "ref"));

    IncarnationContext context =
        new IncarnationContextBuilder(checker, refCD, conCD).buildContextForMapping("ref", false);

    ASTCDType referenceCourse = findType(refCD, "Course");
    List<String> incarnations =
        context.getIncarnations(StableElementKey.type(referenceCourse)).stream()
            .map(element -> element.key().getName())
            .collect(Collectors.toList());

    assertEquals(List.of("Course"), incarnations);
  }

  @Test
  @DisplayName("Can disable manual stereotype overlay when checker mappings are absent")
  public void canDisableManualStereotypeOverlay() {
    ASTCDCompilationUnit refCD =
        JavaLoader.loadCD(
            new File(
                "src/test/resources/de/monticore/codeAdaption/evaluation/testcase_13_template_observer_pattern/Reference.cd"));
    ASTCDCompilationUnit conCD =
        JavaLoader.loadCD(
            new File(
                "src/test/resources/de/monticore/codeAdaption/evaluation/testcase_13_template_observer_pattern/Concrete.cd"));
    CDConformanceChecker checker = new CDConformanceChecker(confParameters);

    IncarnationContext checkerOnly =
        new IncarnationContextBuilder(checker, refCD, conCD).buildContextForMapping("observer", false);
    IncarnationContext manualFallback =
        new IncarnationContextBuilder(checker, refCD, conCD).buildContextForMapping("observer", true);

    assertTrue(checkerOnly.getMappings().isEmpty());
    assertFalse(manualFallback.getMappings().isEmpty());
  }

  @Test
  @DisplayName("Common-parent switch controls whether concrete incarnations collapse to a grouping type")
  public void commonParentSwitchControlsGroupingResolution() {
    ASTCDCompilationUnit refCD =
        JavaLoader.loadCD(
            new File(
                "src/test/resources/de/monticore/codeAdaption/evaluation/testcase_13_template_observer_pattern/Reference.cd"));
    ASTCDCompilationUnit conCD =
        JavaLoader.loadCD(
            new File(
                "src/test/resources/de/monticore/codeAdaption/evaluation/testcase_13_template_observer_pattern/Concrete.cd"));

    ASTCDType observer = findType(refCD, "Observer");
    ASTCDType pipelineObserver = findType(conCD, "PipelineObserver");
    ASTCDType loggingObserver = findType(conCD, "LoggingObserver");
    ASTCDType metricsObserver = findType(conCD, "MetricsObserver");

    Map<StableElementKey, List<IncarnationContext.MappedElement>> mappings =
        new LinkedHashMap<>();
    mappings.put(
        StableElementKey.type(observer),
        List.of(
            new IncarnationContext.MappedElement(
                StableElementKey.type(loggingObserver), loggingObserver.getSymbol()),
            new IncarnationContext.MappedElement(
                StableElementKey.type(metricsObserver), metricsObserver.getSymbol())));

    IncarnationContext.MappedElement grouping =
        new IncarnationContext.MappedElement(
            StableElementKey.type(pipelineObserver), pipelineObserver.getSymbol());

    IncarnationContext context =
        new IncarnationContext(
            "observer",
            mappings,
            Map.of(
                StableElementKey.type(loggingObserver), grouping,
                StableElementKey.type(metricsObserver), grouping));

    assertEquals(
        "PipelineObserver",
        resolve(context, null, null, Map.of(), true, observer.getSymbol()).orElseThrow().getName());
    assertEquals(
        "LoggingObserver",
        resolve(context, null, null, Map.of(), false, observer.getSymbol()).orElseThrow().getName());
  }

  @Test
  @DisplayName("Stable keys resolve mappings across separately parsed equivalent CDs")
  public void resolvesMappingsAcrossSeparateParses() {
    File referenceFile =
        new File(
            "src/test/resources/de/monticore/codeAdaption/evaluation/testcase_6_builder_pattern/Reference.cd");
    File concreteFile =
        new File(
            "src/test/resources/de/monticore/codeAdaption/evaluation/testcase_6_builder_pattern/Concrete.cd");
    ASTCDCompilationUnit firstReference = JavaLoader.loadCD(referenceFile);
    ASTCDCompilationUnit firstConcrete = JavaLoader.loadCD(concreteFile);
    IncarnationContext context = build(firstReference, firstConcrete, "buildPat");

    ASTCDCompilationUnit secondReference = JavaLoader.loadCD(referenceFile);
    CDModelIndex secondIndex = CDModelIndex.of(secondReference);
    ASTCDType secondBuilder = findType(secondReference, "Builder");
    StableElementKey typeKey =
        StableElementKey.fromSymbol(secondBuilder.getSymbol(), secondIndex).orElseThrow();
    StableElementKey fieldKey =
        StableElementKey.fromSymbol(
                secondBuilder.getCDAttributeList().get(0).getSymbol(), secondIndex)
            .orElseThrow();
    StableElementKey methodKey =
        StableElementKey.fromSymbol(
                secondBuilder.getCDMethodList().get(0).getSymbol(), secondIndex)
            .orElseThrow();

    assertFalse(context.getIncarnations(typeKey).isEmpty());
    assertEquals(10, context.getIncarnations(fieldKey).size());
    assertFalse(context.getIncarnations(methodKey).isEmpty());
    assertThrows(
        UnsupportedOperationException.class,
        () -> context.getMappings().put(StableElementKey.type("Other"), List.of()));
    assertThrows(
        UnsupportedOperationException.class,
        () -> context.getIncarnations(fieldKey).clear());
  }

  @Test
  @DisplayName("Pass selection resolves types and members by stable key across separate parses")
  public void passSelectionUsesStableKeysAndSelectedOwner() {
    File referenceFile =
        new File(
            "src/test/resources/de/monticore/codeAdaption/evaluation/testcase_6_builder_pattern/Reference.cd");
    File concreteFile =
        new File(
            "src/test/resources/de/monticore/codeAdaption/evaluation/testcase_6_builder_pattern/Concrete.cd");
    ASTCDCompilationUnit contextReference = JavaLoader.loadCD(referenceFile);
    ASTCDCompilationUnit concrete = JavaLoader.loadCD(concreteFile);
    IncarnationContext context = build(contextReference, concrete, "buildPat");

    IncarnationContext.MappedElement selectedTask =
        context.getIncarnations(StableElementKey.type("Builder")).stream()
            .filter(element -> element.key().equals(StableElementKey.type("Task")))
            .findFirst()
            .orElseThrow();
    ASTCDCompilationUnit separatelyParsedReference = JavaLoader.loadCD(referenceFile);
    ASTCDType builder = findType(separatelyParsedReference, "Builder");
    Map<StableElementKey, IncarnationContext.MappedElement> selection =
        Map.of(StableElementKey.type("Builder"), selectedTask);
    assertEquals(
        "Task",
        resolve(context, separatelyParsedReference, concrete, selection, false, builder.getSymbol())
            .orElseThrow()
            .getName());
    var selectedField =
        resolve(
                context,
                separatelyParsedReference,
                concrete,
                selection,
                false,
                builder.getCDAttributeList().get(0).getSymbol())
            .orElseThrow();
    assertEquals(
        "Task",
        StableElementKey.fromSymbol(selectedField, CDModelIndex.of(concrete))
            .orElseThrow()
            .getOwnerType()
            .orElseThrow());
  }

  private IncarnationContext build(ASTCDCompilationUnit refCD, ASTCDCompilationUnit conCD, String mapping) {
    CDConformanceChecker checker = new CDConformanceChecker(confParameters);
    try {
      checker.checkConformance(conCD, refCD, mapping);
    } catch (Throwable ignored) {
      // The context builder must still overlay stereotypes when conformance is partial.
    }
    return new IncarnationContextBuilder(checker, refCD, conCD).buildContextForMapping(mapping);
  }

  private ASTCDType findType(ASTCDCompilationUnit cd, String name) {
    return CDDiffUtil.getAllCDTypes(cd).stream()
        .filter(type -> type.getName().equals(name))
        .findFirst()
        .orElseThrow();
  }

  private static Optional<de.monticore.symboltable.ISymbol> resolve(
      IncarnationContext incarnationContext,
      ASTCDCompilationUnit reference,
      ASTCDCompilationUnit concrete,
      Map<StableElementKey, IncarnationContext.MappedElement> selection,
      boolean useCommonParentForMultipleIncarnations,
      de.monticore.symboltable.ISymbol symbol) {
    return BasicUpdateHandlerTestAccess.resolve(
        incarnationContext,
        reference,
        concrete,
        selection,
        useCommonParentForMultipleIncarnations,
        symbol);
  }
}
