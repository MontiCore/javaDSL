package de.monticore.codeAdaption;

import de.monticore.cdconformance.CDConfParameter;
import de.monticore.codeAdaption.utils.AdapterParam;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static de.monticore.cdconformance.CDConfParameter.*;
import static de.monticore.codeAdaption.utils.AdapterParam.*;
import static org.junit.jupiter.api.Assertions.*;

public class ControllerWorkerAdapterReversedTest extends AdapterAbstractTest {
  private final String resourcesPath =
      "src/test/resources/de/monticore/codeAdaption/evaluation/testcase_8_controller_worker_observer_reversed/";
  private final File refCD = new File(resourcesPath + "Reference.cd");
  private final File concreteCD = new File(resourcesPath + "Concrete.cd");
  private final Path refCodePath = Path.of(resourcesPath + "concrete");
  private final Path adapterCodePath = Path.of(resourcesPath + "adapter");
  private final Path outputPath = Path.of("target/adapter/controller_worker_observer_reversed");

  private Set<CDConfParameter> confParameters;
  private Set<AdapterParam> adapterParams;

  @BeforeEach
  public void setup() {
    initMills();
    confParameters = Set.of(NAME_MAPPING, INHERITANCE, STEREOTYPE_MAPPING, STRICT_PARAMETER_ORDER);
    adapterParams = Set.of(NAME_MATCHING, ANNOTATION_MATCHING, IGNORE_NON_MATCHED_VAR, IGNORE_NON_MATCHED_TYPE);
  }

  @Test
  @DisplayName("Builder + Observer Patterns: Adapt Controller and Workers")
  public void testControllerWorkerAdaptation() {
    CodeAdapter adapter = new CodeAdapter(adapterParams, confParameters);

    assertDoesNotThrow(() -> {
      adapter.adapt(refCD, concreteCD, Set.of("buildPat", "observer"), adapterCodePath, refCodePath, outputPath);
    });

    Set<String> generatedFiles = generatedFileNames(outputPath);
    assertTrue(generatedFiles.contains("ControllerBuilder.java"));
    assertTrue(generatedFiles.contains("WorkerABuilder.java"));
    assertTrue(generatedFiles.contains("WorkerBBuilder.java"));
    assertTrue(generatedFiles.contains("Controller.java"));
    assertTrue(generatedFiles.contains("WorkerA.java"));
    assertTrue(generatedFiles.contains("WorkerB.java"));

    String controllerBuilderContent = readFileContent(outputPath, "ControllerBuilder.java");
    String workerABuilderContent = readFileContent(outputPath, "WorkerABuilder.java");
    String workerBBuilderContent = readFileContent(outputPath, "WorkerBBuilder.java");

    assertTrue(controllerBuilderContent.contains("public class ControllerBuilder"));
    assertTrue(controllerBuilderContent.contains("public Controller build()"));
    assertTrue(controllerBuilderContent.contains("return this;") || controllerBuilderContent.contains("return this ;"));
    assertTrue(controllerBuilderContent.contains("String idField") || controllerBuilderContent.contains("String idfield"));
    assertTrue(controllerBuilderContent.contains("String statusField") || controllerBuilderContent.contains("String statusfield"));

    assertTrue(workerABuilderContent.contains("public class WorkerABuilder"));
    assertTrue(workerABuilderContent.contains("public WorkerA build()"));
    assertTrue(workerABuilderContent.contains("String nameField") || workerABuilderContent.contains("String namefield"));

    assertTrue(workerBBuilderContent.contains("public class WorkerBBuilder"));
    assertTrue(workerBBuilderContent.contains("public WorkerB build()"));
    assertTrue(workerBBuilderContent.contains("String nameField") || workerBBuilderContent.contains("String namefield"));

    String workerAContent = readFileContent(outputPath, "WorkerA.java");
    String workerBContent = readFileContent(outputPath, "WorkerB.java");

    assertTrue(workerAContent.contains("public void update(") || workerAContent.contains("void update("));
    assertTrue(workerBContent.contains("public void update(") || workerBContent.contains("void update("));
  }
}

