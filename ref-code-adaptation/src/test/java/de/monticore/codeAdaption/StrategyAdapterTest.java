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

public class StrategyAdapterTest extends AdapterAbstractTest {
  private final String resourcesPath =
      "src/test/resources/de/monticore/codeAdaption/evaluation/testcase_9_strategy_pattern/";
  private final File refCD = new File(resourcesPath + "Reference.cd");
  private final File concreteCD = new File(resourcesPath + "Concrete.cd");
  private final Path refCodePath = Path.of(resourcesPath + "concrete");
  private final Path adapterCodePath = Path.of(resourcesPath + "adapter");
  private final Path outputPath = Path.of("target/adapter/strategy_pattern");

  private Set<CDConfParameter> confParameters;
  private Set<AdapterParam> adapterParams;

  @BeforeEach
  public void setup() {
    initMills();
    confParameters = Set.of(NAME_MAPPING, INHERITANCE, STEREOTYPE_MAPPING, STRICT_PARAMETER_ORDER);
    adapterParams = Set.of(NAME_MATCHING, ANNOTATION_MATCHING, IGNORE_NON_MATCHED_VAR, IGNORE_NON_MATCHED_TYPE);
  }

  @Test
  @DisplayName("Strategy Pattern")
  public void testStrategyAdaptation() {
    CodeAdapter adapter = new CodeAdapter(adapterParams, confParameters);

    assertDoesNotThrow(
        () ->
            adapter.adapt(
                refCD, concreteCD, Set.of("strategy"), adapterCodePath, refCodePath, outputPath));
  }
}

