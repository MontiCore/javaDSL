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

public class SingletonAdapterTest extends AdapterAbstractTest {
  private final String resourcesPath =
      "src/test/resources/de/monticore/codeAdaption/evaluation/testcase_10_singleton_pattern/";
  private final File refCD = new File(resourcesPath + "Reference.cd");
  private final File concreteCD = new File(resourcesPath + "Concrete.cd");
  private final Path refCodePath = Path.of(resourcesPath + "concrete");
  private final Path adapterCodePath = Path.of(resourcesPath + "adapter");
  private final Path outputPath = Path.of("target/adapter/singleton_pattern");

  private Set<CDConfParameter> confParameters;
  private Set<AdapterParam> adapterParams;

  @BeforeEach
  public void setup() {
    initMills();
    confParameters = Set.of(NAME_MAPPING, INHERITANCE, STEREOTYPE_MAPPING, STRICT_PARAMETER_ORDER);
    adapterParams = Set.of(NAME_MATCHING, ANNOTATION_MATCHING, IGNORE_NON_MATCHED_VAR, IGNORE_NON_MATCHED_TYPE);
  }

  @Test
  @DisplayName("Singleton Pattern")
  public void testSingletonAdaptation() {
    CodeAdapter adapter = new CodeAdapter(adapterParams, confParameters);

    assertDoesNotThrow(
        () ->
            adapter.adapt(
                refCD,
                concreteCD,
                Set.of("singleton"),
                adapterCodePath,
                refCodePath,
                outputPath));
    assertFalse(generatedJavaFilesRecursively(outputPath).isEmpty());
    assertNoAdapterMetadata(outputPath);
    assertGeneratedJavaCompiles(generatedJavaFilesRecursively(outputPath));

    String databaseConnection = readFileContent(outputPath, "DatabaseConnection.java");
    String logger = readFileContent(outputPath, "Logger.java");
    assertTrue(databaseConnection.contains("class DatabaseConnection"));
    assertTrue(databaseConnection.contains("static DatabaseConnection instance"));
    assertTrue(databaseConnection.contains("DatabaseConnection getInstance()"));
    assertTrue(logger.contains("class Logger"));
    assertTrue(logger.contains("static Logger instance"));
    assertTrue(logger.contains("Logger getInstance()"));
  }
}

