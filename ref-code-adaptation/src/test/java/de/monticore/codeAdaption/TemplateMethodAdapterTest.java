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

public class TemplateMethodAdapterTest extends AdapterAbstractTest {
  private final String resourcesPath =
      "src/test/resources/de/monticore/codeAdaption/evaluation/testcase_11_template_method_pattern/";
  private final File refCD = new File(resourcesPath + "Reference.cd");
  private final File concreteCD = new File(resourcesPath + "Concrete.cd");
  private final Path refCodePath = Path.of(resourcesPath + "concrete");
  private final Path adapterCodePath = Path.of(resourcesPath + "adapter");
  private final Path outputPath = Path.of("target/adapter/template_pattern");

  private Set<CDConfParameter> confParameters;
  private Set<AdapterParam> adapterParams;

  @BeforeEach
  public void setup() {
    initMills();
    confParameters = Set.of(NAME_MAPPING, INHERITANCE, STEREOTYPE_MAPPING, STRICT_PARAMETER_ORDER);
    adapterParams = Set.of(NAME_MATCHING, ANNOTATION_MATCHING, IGNORE_NON_MATCHED_VAR, IGNORE_NON_MATCHED_TYPE);
  }

  @Test
  @DisplayName("Template Pattern")
  public void testTemplateMethodAdaptation() {
    CodeAdapter adapter = new CodeAdapter(adapterParams, confParameters);

    assertDoesNotThrow(() -> {
      adapter.adapt(refCD, concreteCD, Set.of("template"), adapterCodePath, refCodePath, outputPath);
    });

    String dataProcessor = readFileContent(outputPath, "DataProcessor.java");
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
}

