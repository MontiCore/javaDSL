package de.monticore.codeAdaption;

import de.monticore.cdconformance.CDConfParameter;
import de.monticore.codeAdaption.utils.AdapterParam;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;

import static de.monticore.cdconformance.CDConfParameter.*;
import static de.monticore.codeAdaption.utils.AdapterParam.*;
import static org.junit.jupiter.api.Assertions.*;

public class TemplateObserverAdapterTest extends AdapterAbstractTest {
  private final String resourcesPath =
      "src/test/resources/de/monticore/codeAdaption/evaluation/testcase_13_template_observer_pattern/";
  private final File refCD = new File(resourcesPath + "Reference.cd");
  private final File concreteCD = new File(resourcesPath + "Concrete.cd");
  private final Path refCodePath = Path.of(resourcesPath + "concrete");
  private final Path adapterCodePath = Path.of(resourcesPath + "adapter");
  private final Path outputPath = Path.of("target/adapter/template_observer_patterns");

  private Set<CDConfParameter> confParameters;
  private Set<AdapterParam> adapterParams;

  @BeforeEach
  public void setup() {
    initMills();
    confParameters = Set.of(NAME_MAPPING, INHERITANCE, STEREOTYPE_MAPPING, STRICT_PARAMETER_ORDER, ALLOW_ADDITIONAL_PARAMETERS);
    adapterParams = Set.of(NAME_MATCHING, ANNOTATION_MATCHING, IGNORE_NON_MATCHED_VAR, IGNORE_NON_MATCHED_TYPE);
  }

  @Test
  @DisplayName("Template + Observer Pattern")
  public void testTemplateObserverAdaptation() {
    CodeAdapter adapter = new CodeAdapter(adapterParams, confParameters);

    assertDoesNotThrow(() -> {
      adapter.adapt(refCD, concreteCD, Set.of("template", "observer"), adapterCodePath, refCodePath, outputPath);
    });

    String dataPipeline = readFileContent(outputPath, "DataPipeline.java");
    String pipelineObserver = readFileContent(outputPath, "PipelineObserver.java");
    String loggingObserver = readFileContent(outputPath, "LoggingObserver.java");
    String metricsObserver = readFileContent(outputPath, "MetricsObserver.java");

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

  private int count(String content, String needle) {
    return content.split(java.util.regex.Pattern.quote(needle), -1).length - 1;
  }
}

