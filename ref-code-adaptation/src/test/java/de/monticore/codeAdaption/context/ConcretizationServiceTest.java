package de.monticore.codeAdaption.context;

import static de.monticore.cdconformance.CDConfParameter.ADAPTED_NAME_MAPPING;
import static de.monticore.cdconformance.CDConfParameter.ALLOW_ADDITIONAL_PARAMETERS;
import static de.monticore.cdconformance.CDConfParameter.ALLOW_CARD_RESTRICTION;
import static de.monticore.cdconformance.CDConfParameter.INHERITANCE;
import static de.monticore.cdconformance.CDConfParameter.METHOD_OVERLOADING;
import static de.monticore.cdconformance.CDConfParameter.NAME_MAPPING;
import static de.monticore.cdconformance.CDConfParameter.SRC_TARGET_ASSOC_MAPPING;
import static de.monticore.cdconformance.CDConfParameter.STEREOTYPE_MAPPING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdconformance.CDConfParameter;
import de.monticore.codeAdaption.AdapterAbstractTest;
import de.monticore.codeAdaption.utils.CDModelIndex;
import de.monticore.codeAdaption.utils.JavaLoader;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConcretizationServiceTest extends AdapterAbstractTest {

  private static final Path ROOT =
      Path.of("src/test/resources/de/monticore/codeAdaption/cdconcretization");

  private Set<CDConfParameter> parameters;

  @BeforeEach
  void setUp() {
    initMills();
    parameters =
        Set.of(
            STEREOTYPE_MAPPING,
            NAME_MAPPING,
            SRC_TARGET_ASSOC_MAPPING,
            INHERITANCE,
            ALLOW_CARD_RESTRICTION,
            METHOD_OVERLOADING,
            ADAPTED_NAME_MAPPING,
            ALLOW_ADDITIONAL_PARAMETERS);
  }

  @Test
  void completedInheritanceIsVisibleInTheRebuiltIndex() {
    ASTCDCompilationUnit completed =
        complete(
            "inheritance/AttributeInheritanceRef.cd",
            "inheritance/AttributeInheritanceConc.cd");
    CDModelIndex index = CDModelIndex.of(completed);

    assertTrue(index.hasType("Person"));
    assertEquals(Set.of("Person"), index.directParentNames("Teacher"));
  }

  @Test
  void upstreamCompletionDoesNotSynthesizeReverseForEachFields() {
    ASTCDCompilationUnit completed =
        complete(
            "evaluation/staticDelegator/attrWorkaround/StaticDelegatorRef.cd",
            "evaluation/staticDelegator/attrWorkaround/InstanceMethodExistsConc.cd");
    CDModelIndex index = CDModelIndex.of(completed);

    assertTrue(index.attribute("MyLanguageMill", "foo").isEmpty());
    assertTrue(index.attribute("MyLanguageMill", "_foo").isPresent());
  }

  @Test
  void completedEnumConstantIsVisibleInTheRebuiltIndex() {
    ASTCDCompilationUnit completed =
        complete("types/enums/EnumMemberMissingRef.cd", "types/enums/EnumMemberMissingConc.cd");
    CDModelIndex index = CDModelIndex.of(completed);

    assertEquals(
        Set.of("RED", "GREEN", "YELLOW", "BLUE", "PURPLE"),
        index.enums().get(0).getCDEnumConstantList().stream()
            .map(constant -> constant.getName())
            .collect(java.util.stream.Collectors.toSet()));
  }

  @Test
  void completedInterfaceIsVisibleInTheRebuiltIndex() {
    ASTCDCompilationUnit completed =
        complete("types/valid/TypeMissingRef.cd", "types/valid/TypeMissingConc.cd");
    CDModelIndex index = CDModelIndex.of(completed);

    assertTrue(index.directParentNames("Course").contains("Teachable"));
  }

  @Test
  void upstreamCompletionDoesNotSynthesizeMultiIncarnationAccountAlias() {
    ASTCDCompilationUnit completed =
        complete(
            "evaluation/banking/BankingRef.cd",
            "evaluation/banking/multiInc/BankingConc.cd");
    CDModelIndex index = CDModelIndex.of(completed);

    assertTrue(index.attribute("Transaction", "sourceAccount").isEmpty());
  }

  @Test
  void upstreamCompletionDoesNotSynthesizeCrossReferenceForEachMethod() {
    ASTCDCompilationUnit completed =
        complete(
            "evaluation/cross-references/MicroserviceRef.cd",
            "evaluation/cross-references/MicroserviceConc.cd");
    CDModelIndex index = CDModelIndex.of(completed);

    assertTrue(index.methods("UserService", "sendToOrderService").isEmpty());
  }

  private ASTCDCompilationUnit complete(String reference, String concrete) {
    ASTCDCompilationUnit referenceCD = JavaLoader.parseCD(ROOT.resolve(reference).toString());
    ASTCDCompilationUnit concreteCD = JavaLoader.parseCD(ROOT.resolve(concrete).toString());
    return new ConcretizationService(parameters)
        .completeConcreteCD(concreteCD, referenceCD, Set.of("ref"));
  }
}
