package de.monticore.codeAdaption.evaluation;

import static de.monticore.cdconformance.CDConfParameter.ALLOW_ADDITIONAL_PARAMETERS;
import static de.monticore.cdconformance.CDConfParameter.INHERITANCE;
import static de.monticore.cdconformance.CDConfParameter.NAME_MAPPING;
import static de.monticore.cdconformance.CDConfParameter.STEREOTYPE_MAPPING;
import static de.monticore.cdconformance.CDConfParameter.STRICT_PARAMETER_ORDER;
import static de.monticore.codeAdaption.utils.AdapterParam.ANNOTATION_MATCHING;
import static de.monticore.codeAdaption.utils.AdapterParam.IGNORE_NON_MATCHED_TYPE;
import static de.monticore.codeAdaption.utils.AdapterParam.IGNORE_NON_MATCHED_TYPE_MEMBER;
import static de.monticore.codeAdaption.utils.AdapterParam.IGNORE_NON_MATCHED_VAR;
import static de.monticore.codeAdaption.utils.AdapterParam.NAME_MATCHING;
import static org.junit.jupiter.api.Assertions.*;

import de.monticore.codeAdaption.CodeAdapter;
import java.io.File;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** End-to-end evaluation of four interacting patterns in a fulfilment workflow. */
public class LargeFulfillmentSystemEvaluationTest extends EvaluationAbstractTest {

  private static final String CASE_NAME = "testcase_17_fulfillment_platform";

  private static final Set<String> EXPECTED_TYPES =
      Set.of(
          "AuditLog.java",
          "AuditTrailListener.java",
          "BookCarrierCommand.java",
          "CarrierGateway.java",
          "CarrierGatewayFactory.java",
          "CustomerNotificationListener.java",
          "CustomerNotifier.java",
          "DeliveryItem.java",
          "DeliveryOrder.java",
          "FastestRoutePolicy.java",
          "FulfillmentCommand.java",
          "FulfillmentCommandQueue.java",
          "FulfillmentCoordinator.java",
          "FulfillmentScenario.java",
          "InventoryLedger.java",
          "LegacyCarrierApi.java",
          "LegacyCarrierGatewayAdapter.java",
          "OperationsDashboardListener.java",
          "RecordShipmentCommand.java",
          "ReserveInventoryCommand.java",
          "RoutePlanner.java",
          "RoutePolicy.java",
          "ShipmentEventListener.java",
          "ShipmentEventPublisher.java",
          "ShipmentRecord.java",
          "ShipmentRepository.java",
          "SustainableRoutePolicy.java");

  @TempDir Path temporaryDirectory;

  @BeforeEach
  public void setup() {
    initMills();
    adapterParams =
        Set.of(
            NAME_MATCHING,
            ANNOTATION_MATCHING,
            IGNORE_NON_MATCHED_VAR,
            IGNORE_NON_MATCHED_TYPE,
            IGNORE_NON_MATCHED_TYPE_MEMBER);
    confParameters =
        Set.of(
            NAME_MAPPING,
            INHERITANCE,
            STEREOTYPE_MAPPING,
            STRICT_PARAMETER_ORDER,
            ALLOW_ADDITIONAL_PARAMETERS);
  }

  @Test
  @DisplayName("Evaluation Code Adapter Case Study 17 Fulfilment Platform")
  public void adaptsCompleteFulfillmentPlatform() {
    configureFixture();
    deleteRecursively(output);
    assertFixtureRequiresAdaptation();

    CodeAdapter adapter = new CodeAdapter(adapterParams, confParameters);
    assertDoesNotThrow(
        () ->
            adapter.adapt(
                referenceCD,
                concreteCD,
                Set.of("observer", "command", "strategy", "adapter"),
                refCodePath,
                conCodePath,
                output));

    assertEquals(EXPECTED_TYPES, generatedFileNames(output));
    assertNoAdapterMetadata(output);
    assertGeneratedJavaCompiles(generatedJavaFilesRecursively(output));

    assertObserverAdaptation();
    assertCommandAdaptation();
    assertStrategyAdaptation();
    assertAdapterAndFactoryAdaptation();
    assertSubsystemInteraction();
    assertObsoleteReferenceNamesAreAbsent();
    assertDoesNotThrow(this::assertScenarioBehavior);
  }

  /**
   * Guards the evaluation against becoming a copy-through test. Each mapped pattern deliberately
   * starts with at least one missing concrete member that only adapted reference code can supply.
   */
  private void assertFixtureRequiresAdaptation() {
    assertFalse(
        readFileContent(conCodePath, "ShipmentEventPublisher.java")
            .contains("List<ShipmentEventListener> listeners"));
    assertFalse(
        readFileContent(conCodePath, "ShipmentEventPublisher.java")
            .contains("void publishShipment("));
    assertFalse(
        readFileContent(conCodePath, "FulfillmentCommandQueue.java")
            .contains("List<FulfillmentCommand> commands"));
    assertFalse(
        readFileContent(conCodePath, "RoutePlanner.java").contains("private RoutePolicy policy;"));
    assertFalse(
        readFileContent(conCodePath, "LegacyCarrierGatewayAdapter.java")
            .contains("createConsignment("));
  }

  private void configureFixture() {
    referenceCD = new File(resourcesPath + CASE_NAME + "/Reference.cd");
    concreteCD = new File(resourcesPath + CASE_NAME + "/Concrete.cd");
    refCodePath = Path.of(resourcesPath + CASE_NAME + "/adapter");
    conCodePath = Path.of(resourcesPath + CASE_NAME + "/concrete");
    output = Path.of("target/codeAdapter/evaluation/" + CASE_NAME);
  }

  private void assertObserverAdaptation() {
    String listener = readFileContent(output, "ShipmentEventListener.java");
    String publisher = readFileContent(output, "ShipmentEventPublisher.java");
    String auditListener = readFileContent(output, "AuditTrailListener.java");

    assertTrue(listener.contains("public interface ShipmentEventListener"));
    assertTrue(listener.contains("void onShipmentEvent(ShipmentRecord shipment);"));
    assertTrue(publisher.contains("List<ShipmentEventListener> listeners"));
    assertTrue(publisher.contains("void subscribe(ShipmentEventListener listener)"));
    assertTrue(publisher.contains("void publishShipment(ShipmentRecord shipment)"));
    assertTrue(publisher.contains("observer.onShipmentEvent(shipment)"));
    assertTrue(auditListener.contains("implements ShipmentEventListener"));
  }

  private void assertCommandAdaptation() {
    String command = readFileContent(output, "FulfillmentCommand.java");
    String queue = readFileContent(output, "FulfillmentCommandQueue.java");
    String recordCommand = readFileContent(output, "RecordShipmentCommand.java");

    assertTrue(command.contains("public interface FulfillmentCommand"));
    assertTrue(command.contains("boolean run();"));
    assertTrue(queue.contains("List<FulfillmentCommand> commands"));
    assertTrue(queue.contains("void schedule(FulfillmentCommand command)"));
    assertTrue(queue.contains("boolean drain()"));
    assertTrue(queue.contains("command.run()"));
    assertTrue(recordCommand.contains("publisher.publishShipment(shipment)"));
  }

  private void assertStrategyAdaptation() {
    String policy = readFileContent(output, "RoutePolicy.java");
    String planner = readFileContent(output, "RoutePlanner.java");

    assertTrue(policy.contains("public interface RoutePolicy"));
    assertTrue(policy.contains("String selectRoute(DeliveryOrder order);"));
    assertTrue(planner.contains("RoutePolicy policy"));
    assertTrue(planner.contains("void usePolicy(RoutePolicy policy)"));
    assertTrue(planner.contains("String planRoute(DeliveryOrder order)"));
    assertTrue(planner.contains("policy.selectRoute(order)"));
  }

  private void assertAdapterAndFactoryAdaptation() {
    String gateway = readFileContent(output, "CarrierGateway.java");
    String carrierAdapter = readFileContent(output, "LegacyCarrierGatewayAdapter.java");
    String factory = readFileContent(output, "CarrierGatewayFactory.java");

    assertTrue(gateway.contains("public interface CarrierGateway"));
    assertTrue(
        gateway.contains("String createConsignment(DeliveryOrder order, String route);"));
    assertTrue(carrierAdapter.contains("implements CarrierGateway"));
    assertTrue(carrierAdapter.contains("LegacyCarrierApi legacyCarrier"));
    assertTrue(
        carrierAdapter.contains("legacyCarrier.createLegacyConsignment(order, route)"));
    assertTrue(factory.contains("CarrierGateway createGateway(LegacyCarrierApi legacyCarrier)"));
    assertTrue(factory.contains("return new LegacyCarrierGatewayAdapter(legacyCarrier);"));
  }

  private void assertSubsystemInteraction() {
    String coordinator = readFileContent(output, "FulfillmentCoordinator.java");
    String scenario = readFileContent(output, "FulfillmentScenario.java");

    assertTrue(coordinator.contains("routePlanner.planRoute(order)"));
    assertTrue(coordinator.contains("new BookCarrierCommand(carrierGateway, order, route)"));
    assertTrue(coordinator.contains("commandQueue.schedule(new ReserveInventoryCommand"));
    assertTrue(coordinator.contains("new RecordShipmentCommand"));
    assertTrue(coordinator.contains("commandQueue.drain()"));
    assertTrue(coordinator.contains("ShipmentEventPublisher eventPublisher"));

    assertTrue(scenario.contains("public static boolean run()"));
    assertTrue(scenario.contains("publisher.subscribe(new AuditTrailListener"));
    assertTrue(scenario.contains("new CarrierGatewayFactory().createGateway"));
    assertTrue(scenario.contains("coordinator.fulfill(order)"));
    assertTrue(scenario.contains("inventory.reservedUnits"));
    assertTrue(scenario.contains("shipments.containsOrder"));
    assertTrue(scenario.contains("auditLog.containsOrder"));
    assertTrue(scenario.contains("notifier.notified"));
  }

  private void assertObsoleteReferenceNamesAreAbsent() {
    String allSources =
        String.join(
            "\n",
            generatedJavaFilesRecursively(output).stream()
                .map(path -> readFileContent(output, path.getFileName().toString()))
                .toList());

    for (String obsoleteDeclaration :
        Set.of(
            "interface Observer",
            "class Subject",
            "interface Command {",
            "class Invoker",
            "interface Strategy {",
            "class Context",
            "interface Target",
            "class Adaptee",
            "class Adapter")) {
      assertFalse(
          allSources.contains(obsoleteDeclaration),
          () -> "Obsolete reference declaration remains: " + obsoleteDeclaration);
    }
    for (String obsoleteCall :
        Set.of(
            ".update(",
            "notifyObservers(",
            ".execute(",
            "runAll(",
            "specificRequest(",
            ".request(")) {
      assertFalse(
          allSources.contains(obsoleteCall),
          () -> "Obsolete reference call remains: " + obsoleteCall);
    }
  }

  /** Compiles the generated platform and invokes its two subsystem-level behavioral oracles. */
  private void assertScenarioBehavior() throws Exception {
    List<Path> javaFiles = generatedJavaFilesRecursively(output);
    Path classes = temporaryDirectory.resolve("fulfillment-classes");
    Files.createDirectories(classes);
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    assertNotNull(compiler, "Tests must run on a JDK");
    StringWriter compilerOutput = new StringWriter();
    try (StandardJavaFileManager fileManager =
        compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8)) {
      Boolean compiled =
          compiler
              .getTask(
                  compilerOutput,
                  fileManager,
                  null,
                  List.of(
                      "-d",
                      classes.toString(),
                      "-classpath",
                      System.getProperty("java.class.path")),
                  null,
                  fileManager.getJavaFileObjectsFromPaths(javaFiles))
              .call();
      assertEquals(
          Boolean.TRUE,
          compiled,
          () -> "Generated fulfilment platform does not compile:\n" + compilerOutput);
    }

    try (URLClassLoader loader =
        new URLClassLoader(new URL[] {classes.toUri().toURL()}, getClass().getClassLoader())) {
      Class<?> scenario =
          Class.forName(
              "de.monticore.codeAdaption.evaluation.testcase_17_fulfillment_platform.concrete"
                  + ".FulfillmentScenario",
              true,
              loader);
      assertEquals(Boolean.TRUE, scenario.getMethod("run").invoke(null));
      assertEquals(Boolean.TRUE, scenario.getMethod("runQueueFailureAndReuse").invoke(null));
    }
  }
}
