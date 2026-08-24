package de.monticore.codeAdaption;

import static de.monticore.cdconformance.CDConfParameter.ALLOW_ADDITIONAL_PARAMETERS;
import static de.monticore.cdconformance.CDConfParameter.INHERITANCE;
import static de.monticore.cdconformance.CDConfParameter.NAME_MAPPING;
import static de.monticore.cdconformance.CDConfParameter.STEREOTYPE_MAPPING;
import static de.monticore.cdconformance.CDConfParameter.STRICT_PARAMETER_ORDER;
import static de.monticore.codeAdaption.utils.AdapterParam.ANNOTATION_MATCHING;
import static de.monticore.codeAdaption.utils.AdapterParam.IGNORE_NON_MATCHED_TYPE;
import static de.monticore.codeAdaption.utils.AdapterParam.IGNORE_NON_MATCHED_VAR;
import static de.monticore.codeAdaption.utils.AdapterParam.NAME_MATCHING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.monticore.cdconformance.CDConfParameter;
import de.monticore.codeAdaption.utils.AdapterParam;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PatternAdapterIntegrationTest extends AdapterAbstractTest {

  private static final Path FIXTURE_ROOT =
      Path.of("src/test/resources/de/monticore/codeAdaption/evaluation");
  private static final Set<CDConfParameter> DEFAULT_CONFORMANCE_PARAMETERS =
      Set.of(NAME_MAPPING, INHERITANCE, STEREOTYPE_MAPPING, STRICT_PARAMETER_ORDER);
  private static final Set<AdapterParam> DEFAULT_ADAPTER_PARAMETERS =
      Set.of(
          NAME_MATCHING,
          ANNOTATION_MATCHING,
          IGNORE_NON_MATCHED_VAR,
          IGNORE_NON_MATCHED_TYPE);

  @TempDir Path temporaryDirectory;

  @BeforeEach
  void setUp() {
    initMills();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("controllerWorkerCases")
  void adaptsControllerWorkerPatterns(
      String description, String fixtureName, boolean verifyControllerContract) {
    Path output = adapt(fixtureName, Set.of("buildPat", "observer"), Set.of());

    Set<String> generatedFiles = generatedFileNames(output);
    assertTrue(
        generatedFiles.containsAll(
            Set.of(
                "ControllerBuilder.java",
                "WorkerABuilder.java",
                "WorkerBBuilder.java",
                "Controller.java",
                "WorkerA.java",
                "WorkerB.java")));

    String controllerBuilder = readFileContent(output, "ControllerBuilder.java");
    String workerABuilder = readFileContent(output, "WorkerABuilder.java");
    String workerBBuilder = readFileContent(output, "WorkerBBuilder.java");

    assertTrue(controllerBuilder.contains("public class ControllerBuilder"));
    assertTrue(controllerBuilder.contains("public Controller build()"));
    assertContainsEither(controllerBuilder, "return this;", "return this ;");
    assertContainsEither(controllerBuilder, "String idField", "String idfield");
    assertContainsEither(controllerBuilder, "String statusField", "String statusfield");

    assertTrue(workerABuilder.contains("public class WorkerABuilder"));
    assertTrue(workerABuilder.contains("public WorkerA build()"));
    assertContainsEither(workerABuilder, "String nameField", "String namefield");

    assertTrue(workerBBuilder.contains("public class WorkerBBuilder"));
    assertTrue(workerBBuilder.contains("public WorkerB build()"));
    assertContainsEither(workerBBuilder, "String nameField", "String namefield");

    String workerA = readFileContent(output, "WorkerA.java");
    String workerB = readFileContent(output, "WorkerB.java");
    assertContainsEither(workerA, "public void update(", "void update(");
    assertContainsEither(workerB, "public void update(", "void update(");

    if (verifyControllerContract) {
      String controller = readFileContent(output, "Controller.java");
      assertTrue(controller.contains("private String id"));
      assertTrue(controller.contains("private String status"));
      assertTrue(controller.contains("public Controller(String id, String status)"));
      assertTrue(controller.contains("String getId()"));
      assertTrue(controller.contains("String getStatus()"));
      assertTrue(controller.contains("List<WorkerInterface> observers"));
      assertTrue(controller.contains("boolean subscribe(WorkerInterface"));
      assertTrue(controller.contains("boolean unsubscribe(WorkerInterface"));
      assertTrue(controller.contains("void notifyAll()"));
      assertFalse(generatedFiles.contains("OSubject.java"));
      assertFalse(generatedFiles.contains("OObserver.java"));
      assertFalse(generatedFiles.contains("BuilderBuilder.java"));
    }
  }

  @Test
  @DisplayName("Strategy Pattern")
  void adaptsStrategyPattern() {
    Path output = adapt("testcase_9_strategy_pattern", Set.of("strategy"), Set.of());

    assertNoAdapterMetadata(output);
    assertGeneratedJavaCompiles(output);

    String strategy = readFileContent(output, "PaymentStrategy.java");
    String processor = readFileContent(output, "PaymentProcessor.java");
    assertTrue(strategy.contains("interface PaymentStrategy"));
    assertTrue(strategy.contains("void pay(double amount)"));
    assertTrue(processor.contains("class PaymentProcessor"));
    assertTrue(processor.contains("void processPayment(double amount)"));
  }

  @Test
  @DisplayName("Singleton Pattern")
  void adaptsSingletonPattern() {
    Path output = adapt("testcase_10_singleton_pattern", Set.of("singleton"), Set.of());

    assertNoAdapterMetadata(output);
    assertGeneratedJavaCompiles(output);

    String databaseConnection = readFileContent(output, "DatabaseConnection.java");
    String logger = readFileContent(output, "Logger.java");
    assertTrue(databaseConnection.contains("class DatabaseConnection"));
    assertTrue(databaseConnection.contains("static DatabaseConnection instance"));
    assertTrue(databaseConnection.contains("DatabaseConnection getInstance()"));
    assertTrue(logger.contains("class Logger"));
    assertTrue(logger.contains("static Logger instance"));
    assertTrue(logger.contains("Logger getInstance()"));
  }

  @Test
  @DisplayName("Template Method Pattern")
  void adaptsTemplateMethodPattern() {
    Path output = adapt("testcase_11_template_method_pattern", Set.of("template"), Set.of());

    String dataProcessor = readFileContent(output, "DataProcessor.java");
    assertTrue(dataProcessor.contains("private String source"));
    assertTrue(dataProcessor.contains("private String destination"));
    assertTrue(dataProcessor.contains("public DataProcessor(String source, String destination)"));
    assertTrue(dataProcessor.contains("public abstract void readData()"));
    assertTrue(dataProcessor.contains("public abstract void transformData()"));
    assertTrue(dataProcessor.contains("public void validateData()"));
    assertTrue(dataProcessor.contains("public void templateMethod()"));
    assertTrue(dataProcessor.contains("readData();"));
    assertTrue(dataProcessor.contains("transformData();"));
    assertTrue(dataProcessor.contains("validateData();"));
    assertFalse(dataProcessor.contains("primitiveOperation1"));
    assertFalse(dataProcessor.contains("primitiveOperation2"));
    assertFalse(dataProcessor.contains("void hook()"));
  }

  @Test
  @DisplayName("Strategy + Singleton Patterns")
  void adaptsStrategyAndSingletonTogether() {
    Path output =
        adapt(
            "testcase_12_strategy_singleton_pattern",
            Set.of("strategy", "singleton"),
            Set.of(ALLOW_ADDITIONAL_PARAMETERS));

    assertNoAdapterMetadata(output);
    assertGeneratedJavaCompiles(output);

    String manager = readFileContent(output, "ConfigurationManager.java");
    assertTrue(manager.contains("static ConfigurationManager instance"));
    assertTrue(manager.contains("ConfigurationManager getInstance()"));
    assertTrue(manager.contains("ConfigStorageStrategy storageStrategy"));
    assertTrue(manager.contains("setStorageStrategy(ConfigStorageStrategy"));
    assertTrue(manager.contains("saveConfig(String key, String value)"));
    assertTrue(manager.contains("loadConfig(String key)"));
  }

  @Test
  @DisplayName("Template + Observer Patterns")
  void adaptsTemplateAndObserverTogether() {
    Path output =
        adapt(
            "testcase_13_template_observer_pattern",
            Set.of("template", "observer"),
            Set.of(ALLOW_ADDITIONAL_PARAMETERS));

    String dataPipeline = readFileContent(output, "DataPipeline.java");
    String pipelineObserver = readFileContent(output, "PipelineObserver.java");
    String loggingObserver = readFileContent(output, "LoggingObserver.java");
    String metricsObserver = readFileContent(output, "MetricsObserver.java");

    assertTrue(dataPipeline.contains("List<PipelineObserver> observers"));
    assertFalse(dataPipeline.contains("List<LoggingObserver> observers"));
    assertFalse(dataPipeline.contains("List<MetricsObserver> observers"));
    assertEquals(1, count(dataPipeline, "boolean subscribe(PipelineObserver"));
    assertEquals(1, count(dataPipeline, "boolean unsubscribe(PipelineObserver"));
    assertTrue(dataPipeline.contains("observer.onPipelineStage(null);"));
    assertFalse(dataPipeline.contains("observer.update();"));
    assertFalse(dataPipeline.contains("primitiveOperation1"));
    assertFalse(dataPipeline.contains("primitiveOperation2"));

    assertFalse(pipelineObserver.contains("void update("));
    assertFalse(loggingObserver.contains("void update("));
    assertFalse(metricsObserver.contains("void update("));
  }

  private Path adapt(
      String fixtureName,
      Set<String> mappings,
      Set<CDConfParameter> additionalConformanceParameters) {
    Path fixture = FIXTURE_ROOT.resolve(fixtureName);
    Path output = temporaryDirectory.resolve(fixtureName);
    Set<CDConfParameter> conformanceParameters =
        new LinkedHashSet<>(DEFAULT_CONFORMANCE_PARAMETERS);
    conformanceParameters.addAll(additionalConformanceParameters);

    new CodeAdapter(DEFAULT_ADAPTER_PARAMETERS, conformanceParameters)
        .adapt(
            fixture.resolve("Reference.cd").toFile(),
            fixture.resolve("Concrete.cd").toFile(),
            mappings,
            fixture.resolve("adapter"),
            fixture.resolve("concrete"),
            output);
    return output;
  }

  private static Stream<Arguments> controllerWorkerCases() {
    return Stream.of(
        Arguments.of(
            "controller-to-workers", "testcase_7_controller_worker_observer", true),
        Arguments.of(
            "workers-to-controller",
            "testcase_8_controller_worker_observer_reversed",
            false));
  }

  private static void assertContainsEither(String content, String first, String second) {
    assertTrue(content.contains(first) || content.contains(second), content);
  }

  private static int count(String content, String needle) {
    return content.split(java.util.regex.Pattern.quote(needle), -1).length - 1;
  }
}
