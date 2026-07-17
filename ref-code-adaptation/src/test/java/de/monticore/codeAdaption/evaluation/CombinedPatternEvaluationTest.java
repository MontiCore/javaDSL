package de.monticore.codeAdaption.evaluation;

import static de.monticore.cdconformance.CDConfParameter.ALLOW_ADDITIONAL_PARAMETERS;
import static de.monticore.cdconformance.CDConfParameter.INHERITANCE;
import static de.monticore.cdconformance.CDConfParameter.NAME_MAPPING;
import static de.monticore.cdconformance.CDConfParameter.STEREOTYPE_MAPPING;
import static de.monticore.cdconformance.CDConfParameter.STRICT_PARAMETER_ORDER;
import static de.monticore.codeAdaption.utils.AdapterParam.ANNOTATION_MATCHING;
import static de.monticore.codeAdaption.utils.AdapterParam.IGNORE_NON_MATCHED_TYPE;
import static de.monticore.codeAdaption.utils.AdapterParam.IGNORE_NON_MATCHED_VAR;
import static de.monticore.codeAdaption.utils.AdapterParam.NAME_MATCHING;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.monticore.codeAdaption.CodeAdapter;
import java.io.File;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Executable evaluation wrappers for combined-pattern resource cases. */
public class CombinedPatternEvaluationTest extends EvaluationAbstractTest {

  @BeforeEach
  public void setup() {
    initMills();
    adapterParams =
        Set.of(NAME_MATCHING, ANNOTATION_MATCHING, IGNORE_NON_MATCHED_VAR, IGNORE_NON_MATCHED_TYPE);
    confParameters =
        Set.of(
            NAME_MAPPING,
            INHERITANCE,
            STEREOTYPE_MAPPING,
            STRICT_PARAMETER_ORDER,
            ALLOW_ADDITIONAL_PARAMETERS);
  }

  @Test
  @DisplayName("Evaluation Code Adapter Case Study 14 Adapter + Factory")
  public void adapterFactoryCombinedCaseRuns() {
    runCombinedCase("testcase_14_adapter_factory_combined", Set.of("adapter", "factory"));
    assertEquals(
        Set.of("CarrierAdapter.java", "LegacyCarrier.java", "ShippingFactory.java", "ShippingPort.java"),
        generatedFileNames(output));
    assertNoAdapterMetadata(output);
    assertGeneratedJavaCompiles(output);

    String carrierAdapter = readFileContent(output, "CarrierAdapter.java");
    String shippingFactory = readFileContent(output, "ShippingFactory.java");
    String shippingPort = readFileContent(output, "ShippingPort.java");

    assertTrue(carrierAdapter.contains("public class CarrierAdapter implements ShippingPort"));
    assertTrue(carrierAdapter.contains("public CarrierAdapter(LegacyCarrier carrier)"));
    assertTrue(carrierAdapter.contains("public boolean ship(String label, boolean insured)"));
    assertTrue(carrierAdapter.contains("carrier.dispatch(label, insured)"));
    assertTrue(shippingFactory.contains("public ShippingPort createCarrier(LegacyCarrier carrier)"));
    assertTrue(shippingFactory.contains("return new CarrierAdapter(carrier);"));
    assertTrue(shippingPort.contains("boolean ship(String label, boolean insured);"));
    assertFalse(carrierAdapter.contains("specificRequest"));
    assertFalse(carrierAdapter.contains("boolean request("));
  }

  @Test
  @DisplayName("Evaluation Code Adapter Case Study 15 Composite + Decorator")
  public void compositeDecoratorCombinedCaseRuns() {
    runCombinedCase("testcase_15_composite_decorator_combined", Set.of("composite", "decorator"));
    assertEquals(
        Set.of(
            "BorderDecorator.java",
            "GroupNode.java",
            "ImageNode.java",
            "Renderable.java",
            "RenderDecorator.java",
            "ShadowDecorator.java",
            "TextNode.java"),
        generatedFileNames(output));
    assertNoAdapterMetadata(output);
    assertGeneratedJavaCompiles(output);

    String renderable = readFileContent(output, "Renderable.java");
    String groupNode = readFileContent(output, "GroupNode.java");
    String renderDecorator = readFileContent(output, "RenderDecorator.java");
    String borderDecorator = readFileContent(output, "BorderDecorator.java");
    String shadowDecorator = readFileContent(output, "ShadowDecorator.java");

    assertTrue(renderable.contains("public interface Renderable"));
    assertTrue(renderable.contains("String render(int depth);"));
    assertTrue(groupNode.contains("public class GroupNode implements Renderable"));
    assertTrue(groupNode.contains("List<Renderable> nodes"));
    assertTrue(groupNode.contains("public void attach(Renderable node)"));
    assertTrue(groupNode.contains("public void detach(Renderable node)"));
    assertTrue(groupNode.contains("node.render(depth + 1)"));
    assertTrue(renderDecorator.contains("public abstract class RenderDecorator implements Renderable"));
    assertTrue(renderDecorator.contains("Renderable wrapped"));
    assertTrue(renderDecorator.contains("return wrapped.render(depth);"));
    assertTrue(borderDecorator.contains("public class BorderDecorator extends RenderDecorator"));
    assertTrue(shadowDecorator.contains("public class ShadowDecorator extends RenderDecorator"));
    assertFalse(borderDecorator.contains("abstract class BorderDecorator"));
    assertFalse(shadowDecorator.contains("abstract class ShadowDecorator"));
    assertFalse(groupNode.contains("Component"));
    assertFalse(groupNode.contains("operation("));
  }

  @Test
  @DisplayName("Evaluation Code Adapter Case Study 16 Observer + Command")
  public void observerCommandCombinedCaseRuns() {
    runCombinedCase("testcase_16_observer_command_combined", Set.of("observer", "command"));
    assertEquals(
        Set.of(
            "CommandQueue.java",
            "EmailCommand.java",
            "EventBus.java",
            "SmsCommand.java",
            "WorkflowCommand.java"),
        generatedFileNames(output));
    assertNoAdapterMetadata(output);
    assertGeneratedJavaCompiles(output);

    String eventBus = readFileContent(output, "EventBus.java");
    String commandQueue = readFileContent(output, "CommandQueue.java");
    String workflowCommand = readFileContent(output, "WorkflowCommand.java");
    String emailCommand = readFileContent(output, "EmailCommand.java");
    String smsCommand = readFileContent(output, "SmsCommand.java");

    assertTrue(workflowCommand.contains("public interface WorkflowCommand"));
    assertTrue(workflowCommand.contains("void run(String event, boolean async);"));
    assertTrue(emailCommand.contains("public class EmailCommand implements WorkflowCommand"));
    assertTrue(smsCommand.contains("public class SmsCommand implements WorkflowCommand"));
    assertTrue(eventBus.contains("List<WorkflowCommand> listeners"));
    assertTrue(eventBus.contains("public void subscribe(WorkflowCommand command)"));
    assertTrue(eventBus.contains("public void unsubscribe(WorkflowCommand command)"));
    assertTrue(eventBus.contains("public void publish(String event)"));
    assertTrue(eventBus.contains("listener.run(event, false)"));
    assertTrue(commandQueue.contains("List<WorkflowCommand> backlog"));
    assertTrue(commandQueue.contains("public void schedule(WorkflowCommand command)"));
    assertTrue(commandQueue.contains("public void drain(String event)"));
    assertTrue(commandQueue.contains("command.run(event, false)"));
    assertFalse(eventBus.contains("void update("));
    assertFalse(eventBus.contains("void notifyAll("));
    assertFalse(commandQueue.contains("execute("));
    assertFalse(commandQueue.contains("runAll("));
  }

  private void runCombinedCase(String caseName, Set<String> mappings) {
    referenceCD = new File(resourcesPath + caseName + "/Reference.cd");
    concreteCD = new File(resourcesPath + caseName + "/Concrete.cd");
    refCodePath = Path.of(resourcesPath + caseName + "/adapter");
    conCodePath = Path.of(resourcesPath + caseName + "/concrete");
    output = Path.of("target/codeAdapter/evaluation/" + caseName);

    CodeAdapter adapter = new CodeAdapter(adapterParams, confParameters);
    assertDoesNotThrow(
        () -> adapter.adapt(referenceCD, concreteCD, mappings, refCodePath, conCodePath, output));
  }
}
