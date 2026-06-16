package de.monticore.codeAdaption.handler.multiIncarnation;

import static de.monticore.cdconformance.CDConfParameter.ALLOW_ADDITIONAL_PARAMETERS;
import static de.monticore.cdconformance.CDConfParameter.INHERITANCE;
import static de.monticore.cdconformance.CDConfParameter.NAME_MAPPING;
import static de.monticore.cdconformance.CDConfParameter.STEREOTYPE_MAPPING;
import static de.monticore.cdconformance.CDConfParameter.STRICT_PARAMETER_ORDER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.cdconcretization.ConcretizationCompleter;
import de.monticore.cdconformance.CDConfParameter;
import de.monticore.cdconformance.CDConformanceChecker;
import de.monticore.codeAdaption.AdapterAbstractTest;
import de.monticore.codeAdaption.handler.BasicUpdateHandler;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.cddiff.CDDiffUtil;
import java.io.File;
import java.util.ArrayList;
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
            .map(ResolvedIncarnationContext.ResolvedElement::getKey)
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
            .map(ResolvedIncarnationContext.ResolvedElement::getKey)
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
    assertEquals("PipelineObserver", context.findGroupingTypeForImplementer("LoggingObserver").orElseThrow());
    assertEquals("PipelineObserver", context.findGroupingTypeForImplementer("MetricsObserver").orElseThrow());
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
        context.getIncarnations(referenceCourse.getSymbol()).stream()
            .map(symbol -> symbol.getName())
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

    assertTrue(checkerOnly.getReferenceToIncarnations().isEmpty());
    assertTrue(manualFallback.getReferenceToIncarnations().size() > 0);
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

    Map<de.monticore.symboltable.ISymbol, List<de.monticore.symboltable.ISymbol>>
        referenceToIncarnations = new LinkedHashMap<>();
    referenceToIncarnations.put(
        observer.getSymbol(), new ArrayList<>(List.of(loggingObserver.getSymbol(), metricsObserver.getSymbol())));

    Map<de.monticore.symboltable.ISymbol, List<de.monticore.symboltable.ISymbol>>
        interfaceToImplementers = new LinkedHashMap<>();
    interfaceToImplementers.put(
        pipelineObserver.getSymbol(),
        new ArrayList<>(List.of(loggingObserver.getSymbol(), metricsObserver.getSymbol())));

    IncarnationContext context =
        new IncarnationContext("observer", referenceToIncarnations, interfaceToImplementers);
    context.setConcreteToGroupingType(
        Map.of("LoggingObserver", "PipelineObserver", "MetricsObserver", "PipelineObserver"));

    assertEquals(
        "PipelineObserver",
        new ExposingUpdateHandler(context, true).resolve(observer.getSymbol()).orElseThrow().getName());
    assertEquals(
        "LoggingObserver",
        new ExposingUpdateHandler(context, false).resolve(observer.getSymbol()).orElseThrow().getName());
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

  private static class ExposingUpdateHandler extends BasicUpdateHandler {
    ExposingUpdateHandler(
        IncarnationContext incarnationContext, boolean useCommonParentForMultipleIncarnations) {
      super(
          null,
          null,
          null,
          null,
          null,
          null,
          incarnationContext,
          useCommonParentForMultipleIncarnations);
    }

    Optional<de.monticore.symboltable.ISymbol> resolve(de.monticore.symboltable.ISymbol symbol) {
      return getSymbolFromContext(symbol);
    }
  }
}
