package de.monticore.codeAdaption;

import static de.monticore.cdconformance.CDConfParameter.ADAPTED_NAME_MAPPING;
import static de.monticore.cdconformance.CDConfParameter.ALLOW_ADDITIONAL_PARAMETERS;
import static de.monticore.cdconformance.CDConfParameter.ALLOW_CARD_RESTRICTION;
import static de.monticore.cdconformance.CDConfParameter.INHERITANCE;
import static de.monticore.cdconformance.CDConfParameter.METHOD_OVERLOADING;
import static de.monticore.cdconformance.CDConfParameter.NAME_MAPPING;
import static de.monticore.cdconformance.CDConfParameter.SRC_TARGET_ASSOC_MAPPING;
import static de.monticore.cdconformance.CDConfParameter.STEREOTYPE_MAPPING;
import static de.monticore.codeAdaption.utils.AdapterParam.ANNOTATION_MATCHING;
import static de.monticore.codeAdaption.utils.AdapterParam.NAME_MATCHING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConcretizationHelperDependencyTest extends AdapterAbstractTest {

  private static final Path FIXTURE =
      Path.of("src/test/resources/de/monticore/codeAdaption/concretizationHelpers");

  @TempDir Path temporaryDirectory;

  @BeforeEach
  void setUp() {
    initMills();
  }

  @Test
  void retainsAndAdaptsTransitiveJavaOnlyHelpers() {
    File referenceCD = FIXTURE.resolve("Reference.cd").toFile();
    File concreteCD = FIXTURE.resolve("Concrete.cd").toFile();
    Path referenceCode = FIXTURE.resolve("adapter");
    Path concreteCode = FIXTURE.resolve("concrete");
    Path output = temporaryDirectory.resolve("output");
    CodeAdapter adapter =
        new CodeAdapter(
            Set.of(NAME_MATCHING, ANNOTATION_MATCHING),
            Set.of(
                STEREOTYPE_MAPPING,
                NAME_MAPPING,
                SRC_TARGET_ASSOC_MAPPING,
                INHERITANCE,
                ALLOW_CARD_RESTRICTION,
                METHOD_OVERLOADING,
                ADAPTED_NAME_MAPPING,
                ALLOW_ADDITIONAL_PARAMETERS));

    adapter.adapt(
        referenceCD,
        concreteCD,
        Set.of("ref"),
        referenceCode,
        concreteCode,
        output,
        true,
        true);

    assertTrue(Files.isRegularFile(output.resolve("Concrete/ConcreteService.java")));
    assertTrue(Files.isRegularFile(output.resolve("Concrete/ConcreteServiceTOP.java")));
    assertTrue(Files.isRegularFile(output.resolve("Concrete/ServiceSupport.java")));
    assertTrue(Files.isRegularFile(output.resolve("Concrete/SupportValue.java")));
    assertFalse(Files.exists(output.resolve("Concrete/UnrelatedHelper.java")));

    String service = readFileContent(output, "ConcreteService.java");
    String support = readFileContent(output, "ServiceSupport.java");
    String value = readFileContent(output, "SupportValue.java");
    assertTrue(service.contains("ServiceSupport support"));
    assertTrue(service.contains("ConcreteService decorate()"));
    assertTrue(support.contains("ConcreteService decorate(ConcreteService service)"));
    assertTrue(value.contains("ConcreteService apply(ConcreteService service)"));
    assertFalse(support.contains("Service decorate(Service service)"));
    assertFalse(value.contains("Service apply(Service service)"));

    assertNoAdapterMetadata(output);
    assertGeneratedJavaCompiles(output);
  }

  @Test
  void divergentHelperVariantsFailWithoutPublishingPartialOutput() throws Exception {
    Path divergentFixture = FIXTURE.resolve("divergent");
    Path output = temporaryDirectory.resolve("existing-output");
    Files.createDirectories(output);
    Path marker = output.resolve("preserved.txt");
    Files.writeString(marker, "previous successful output");
    Path referenceService = divergentFixture.resolve("adapter/Service.java");
    String referenceBefore = Files.readString(referenceService);

    CodeAdapter adapter =
        new CodeAdapter(
            Set.of(NAME_MATCHING, ANNOTATION_MATCHING),
            Set.of(
                STEREOTYPE_MAPPING,
                NAME_MAPPING,
                SRC_TARGET_ASSOC_MAPPING,
                INHERITANCE,
                ALLOW_CARD_RESTRICTION,
                METHOD_OVERLOADING,
                ADAPTED_NAME_MAPPING,
                ALLOW_ADDITIONAL_PARAMETERS));

    CodeAdaptationException exception =
        assertThrows(
            CodeAdaptationException.class,
            () ->
                adapter.adapt(
                    divergentFixture.resolve("Reference.cd").toFile(),
                    divergentFixture.resolve("Concrete.cd").toFile(),
                    Set.of("ref"),
                    divergentFixture.resolve("adapter"),
                    divergentFixture.resolve("concrete"),
                    output,
                    true,
                    true));

    assertTrue(exception.getMessage().contains("Concrete.ServiceSupport"));
    assertTrue(exception.getMessage().contains("divergent outputs"));
    assertEquals("previous successful output", Files.readString(marker));
    assertFalse(Files.exists(output.resolve("Concrete/PrimaryService.java")));
    assertFalse(Files.exists(output.resolve("Concrete/BackupService.java")));
    assertEquals(referenceBefore, Files.readString(referenceService));
  }
}
