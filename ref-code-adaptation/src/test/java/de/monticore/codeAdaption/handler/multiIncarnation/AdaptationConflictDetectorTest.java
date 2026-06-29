package de.monticore.codeAdaption.handler.multiIncarnation;

import static de.monticore.cdconformance.CDConfParameter.INHERITANCE;
import static de.monticore.cdconformance.CDConfParameter.NAME_MAPPING;
import static de.monticore.cdconformance.CDConfParameter.STEREOTYPE_MAPPING;
import static de.monticore.codeAdaption.utils.AdapterParam.ANNOTATION_MATCHING;
import static de.monticore.codeAdaption.utils.AdapterParam.IGNORE_NON_MATCHED_TYPE;
import static de.monticore.codeAdaption.utils.AdapterParam.IGNORE_NON_MATCHED_VAR;
import static de.monticore.codeAdaption.utils.AdapterParam.NAME_MATCHING;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdconformance.CDConfParameter;
import de.monticore.codeAdaption.AdapterAbstractTest;
import de.monticore.codeAdaption.CodeAdaptationException;
import de.monticore.codeAdaption.CodeAdapter;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.se_rwth.commons.logging.LogStub;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AdaptationConflictDetectorTest extends AdapterAbstractTest {
  private static final Path ROOT =
      Path.of("src/test/resources/de/monticore/codeAdaption/conflicts");
  private static final Path EVALUATION_ROOT =
      Path.of("src/test/resources/de/monticore/codeAdaption/evaluation");
  private static final Set<String> MAPPINGS = Set.of("ref");

  private Set<CDConfParameter> confParams;

  @BeforeEach
  public void setup() {
    LogStub.init();
    initMills();
    confParams = Set.of(STEREOTYPE_MAPPING, NAME_MAPPING, INHERITANCE);
  }

  @Test
  public void detectsAmbiguousOverloadedMethodStereotype() {
    assertConflict("AmbiguousOverloadRef.cd", "AmbiguousOverloadConc.cd", "ambiguous overloaded");
  }

  @Test
  public void detectsTypeKindMismatch() {
    assertConflict("TypeKindRef.cd", "TypeKindConc.cd", "type-kind mismatch");
  }

  @Test
  public void detectsInheritedFieldConflict() {
    assertConflict("FieldConflictRef.cd", "FieldConflictConc.cd", "inherited field conflict");
  }

  @Test
  public void detectsDuplicateMethodReturnConflict() {
    assertConflict("MethodConflictRef.cd", "MethodConflictConc.cd", "duplicate method");
  }

  @Test
  public void detectsEnumOrderConflict() {
    assertConflict("EnumOrderRef.cd", "EnumOrderConc.cd", "enum order conflict");
  }

  @Test
  public void detectsAssociationCardinalityConflict() {
    assertConflict("AssociationCardinalityRef.cd", "AssociationCardinalityConc.cd", "cardinality");
  }

  @Test
  public void detectsAmbiguousAssociationDirection() {
    assertConflict(
        "AssociationAmbiguousDirectionRef.cd",
        "AssociationAmbiguousDirectionConc.cd",
        "ambiguous association direction");
  }

  @Test
  public void detectsAssociationRoleFieldConflict() {
    assertConflict(
        "AssociationRoleConflictRef.cd",
        "AssociationRoleConflictConc.cd",
        "association role field conflict");
  }

  @Test
  public void detectsUnderspecifiedAnyWithoutIncarnation() {
    assertConflict("AnyRef.cd", "AnyConc.cd", "underspecified attribute type");
  }

  @Test
  public void acceptsDeterministicExplicitMappings() {
    assertDoesNotThrow(() -> validate("ValidManualRef.cd", "ValidManualConc.cd"));
  }

  @Test
  public void acceptsEvaluationTestcase1ObserverChainRoles() {
    ASTCDCompilationUnit refCD =
        JavaLoader.parseCD(EVALUATION_ROOT.resolve("testcase_1/Reference.cd").toString());
    ASTCDCompilationUnit conCD =
        JavaLoader.parseCD(EVALUATION_ROOT.resolve("testcase_1/Concrete.cd").toString());
    Set<String> mappings = Set.of("stud", "prof");
    Map<String, IncarnationContext> contexts = new java.util.LinkedHashMap<>();
    for (String mapping : mappings) {
      contexts.put(mapping, new ManualIncarnationContextBuilder(refCD, conCD, confParams).buildContextForMapping(mapping));
    }

    assertDoesNotThrow(
        () -> AdaptationConflictDetector.validate(refCD, conCD, mappings, contexts, confParams));
  }

  @Test
  public void derivesAttributeForEachMappings() {
    IncarnationContext context = context("ForEachAttributeRef.cd", "ForEachAttributeConc.cd");

    assertEquals(
        2,
        context
            .getIncarnations(StableElementKey.field("Builder", "attribute", "any"))
            .size());
  }

  @Test
  public void derivesMethodForEachMappingsFromAttributeIncarnations() {
    IncarnationContext context = context("ForEachMethodRef.cd", "ForEachMethodConc.cd");

    assertEquals(
        2,
        context
            .getIncarnations(StableElementKey.method("DataClass", "getAttribute", List.of(), "any"))
            .size());
  }

  @Test
  public void derivesExactMethodForEachMappingsFromAttributeIncarnations() {
    IncarnationContext context =
        context("ForEachMethodExactRef.cd", "ForEachMethodExactConc.cd");

    assertEquals(
        2,
        context
            .getIncarnations(StableElementKey.method("Builder", "copy", List.of(), "any"))
            .size());
  }

  @Test
  public void doesNotDeriveMethodForEachMappingsFromAccidentalSubstrings() {
    IncarnationContext context =
        context("ForEachMethodSubstringRef.cd", "ForEachMethodSubstringConc.cd");

    assertEquals(
        0,
        context
            .getIncarnations(
                StableElementKey.method("DataClass", "getAttributable", List.of(), "any"))
            .size());
  }

  @Test
  public void derivesTypeForEachMappings() {
    IncarnationContext context = context("ForEachTypeRef.cd", "ForEachTypeConc.cd");

    assertEquals(2, context.getIncarnations(StableElementKey.type("DataClassBuilder")).size());
  }

  @Test
  public void detectsMissingForEachMappingBeforeCleaningExistingOutput() throws IOException {
    Path output = Path.of("target/codeAdapter/forEachConflictPreserve");
    Files.createDirectories(output);
    Path marker = output.resolve("marker.txt");
    Files.writeString(marker, "keep");

    CodeAdapter adapter =
        new CodeAdapter(
            Set.of(NAME_MATCHING, ANNOTATION_MATCHING, IGNORE_NON_MATCHED_VAR, IGNORE_NON_MATCHED_TYPE),
            confParams);

    CodeAdaptationException exception =
        assertThrows(
            CodeAdaptationException.class,
            () ->
                adapter.adapt(
                    ROOT.resolve("ForEachMissingRef.cd").toFile(),
                    ROOT.resolve("ForEachMissingConc.cd").toFile(),
                    MAPPINGS,
                    ROOT.resolve("adapter"),
                    ROOT.resolve("concrete"),
                    output,
                    false,
                    true));

    assertTrue(exception.getMessage().contains("missing forEach incarnation"));
    assertTrue(Files.exists(marker), "Conflict detection must run before output cleanup");
  }

  @Test
  public void codeAdapterFailsBeforeCleaningExistingOutput() throws IOException {
    Path output = Path.of("target/codeAdapter/conflictDetectorPreserve");
    Files.createDirectories(output);
    Path marker = output.resolve("marker.txt");
    Files.writeString(marker, "keep");

    CodeAdapter adapter =
        new CodeAdapter(
            Set.of(NAME_MATCHING, ANNOTATION_MATCHING, IGNORE_NON_MATCHED_VAR, IGNORE_NON_MATCHED_TYPE),
            confParams);

    assertThrows(
        CodeAdaptationException.class,
        () ->
            adapter.adapt(
                ROOT.resolve("AnyRef.cd").toFile(),
                ROOT.resolve("AnyConc.cd").toFile(),
                MAPPINGS,
                ROOT.resolve("adapter"),
                ROOT.resolve("concrete"),
                output,
                false,
                true));

    assertTrue(Files.exists(marker), "Conflict detection must run before output cleanup");
  }

  private void assertConflict(String reference, String concrete, String expectedMessagePart) {
    CodeAdaptationException exception =
        assertThrows(CodeAdaptationException.class, () -> validate(reference, concrete));
    assertTrue(
        exception.getMessage().contains(expectedMessagePart),
        () -> "Expected message to contain '" + expectedMessagePart + "' but was: "
            + exception.getMessage());
  }

  private void validate(String reference, String concrete) {
    ASTCDCompilationUnit refCD = JavaLoader.parseCD(ROOT.resolve(reference).toString());
    ASTCDCompilationUnit conCD = JavaLoader.parseCD(ROOT.resolve(concrete).toString());
    IncarnationContext context = context(refCD, conCD);
    AdaptationConflictDetector.validate(refCD, conCD, MAPPINGS, Map.of("ref", context), confParams);
  }

  private IncarnationContext context(String reference, String concrete) {
    ASTCDCompilationUnit refCD = JavaLoader.parseCD(ROOT.resolve(reference).toString());
    ASTCDCompilationUnit conCD = JavaLoader.parseCD(ROOT.resolve(concrete).toString());
    return context(refCD, conCD);
  }

  private IncarnationContext context(ASTCDCompilationUnit refCD, ASTCDCompilationUnit conCD) {
    ManualIncarnationContextBuilder builder =
        new ManualIncarnationContextBuilder(refCD, conCD, confParams);
    return builder.buildContextForMapping("ref");
  }
}
