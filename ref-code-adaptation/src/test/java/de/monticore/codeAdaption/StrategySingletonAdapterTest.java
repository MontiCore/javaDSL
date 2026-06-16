package de.monticore.codeAdaption;

import de.monticore.cdconformance.CDConfParameter;
import de.monticore.codeAdaption.utils.AdapterParam;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static de.monticore.cdconformance.CDConfParameter.*;
import static de.monticore.codeAdaption.utils.AdapterParam.*;
import static org.junit.jupiter.api.Assertions.*;

public class StrategySingletonAdapterTest extends AdapterAbstractTest {
  private final String resourcesPath =
      "src/test/resources/de/monticore/codeAdaption/evaluation/testcase_12_strategy_singleton_pattern/";
  private final File refCD = new File(resourcesPath + "Reference.cd");
  private final File concreteCD = new File(resourcesPath + "Concrete.cd");
  private final Path refCodePath = Path.of(resourcesPath + "concrete");
  private final Path adapterCodePath = Path.of(resourcesPath + "adapter");
  private final Path outputPath = Path.of("target/adapter/strategy_singleton_patterns");

  private Set<CDConfParameter> confParameters;
  private Set<AdapterParam> adapterParams;

  @BeforeEach
  public void setup() {
    initMills();
    confParameters = Set.of(NAME_MAPPING, INHERITANCE, STEREOTYPE_MAPPING, STRICT_PARAMETER_ORDER, ALLOW_ADDITIONAL_PARAMETERS);
    adapterParams = Set.of(NAME_MATCHING, ANNOTATION_MATCHING, IGNORE_NON_MATCHED_VAR, IGNORE_NON_MATCHED_TYPE);
  }

  @Test
  @DisplayName("Strategy + Singleton Patterns")
  public void testStrategySingletonAdaptation() {
    CodeAdapter adapter = new CodeAdapter(adapterParams, confParameters);

    assertDoesNotThrow(
        () ->
            adapter.adapt(
                refCD,
                concreteCD,
                Set.of("strategy", "singleton"),
                adapterCodePath,
                refCodePath,
                outputPath));
  }

  private String readFileContent(Path outputPath, String filename) {
    try {
      Path filePath = outputPath.resolve(filename);
      assertTrue(Files.exists(filePath));
      return Files.readString(filePath);
    } catch (IOException e) {
      fail("Failed to read file: " + filename);
      return "";
    }
  }
}

