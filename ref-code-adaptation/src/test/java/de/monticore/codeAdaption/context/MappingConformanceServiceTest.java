package de.monticore.codeAdaption.context;

import static de.monticore.cdconformance.CDConfParameter.INHERITANCE;
import static de.monticore.cdconformance.CDConfParameter.NAME_MAPPING;
import static de.monticore.cdconformance.CDConfParameter.SRC_TARGET_ASSOC_MAPPING;
import static de.monticore.cdconformance.CDConfParameter.STEREOTYPE_MAPPING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdconformance.CDConfParameter;
import de.monticore.codeAdaption.AdapterAbstractTest;
import de.monticore.codeAdaption.utils.CDModelIndex;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.se_rwth.commons.logging.Log;
import de.se_rwth.commons.logging.LogStub;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MappingConformanceServiceTest extends AdapterAbstractTest {

  @TempDir Path tempDir;

  @BeforeEach
  void setUp() {
    initMills();
    LogStub.init();
  }

  @Test
  void failedCheckRestoresFailQuickWithoutLeakingErrors() throws IOException {
    ASTCDCompilationUnit reference =
        parse("Reference.cd", "classdiagram Reference { class Required { int value; } }");
    ASTCDCompilationUnit concrete =
        parse("Concrete.cd", "classdiagram Concrete { class Unrelated; }");
    MappingConformanceService service = new MappingConformanceService(Set.of(NAME_MAPPING));
    long errorsBefore = Log.getErrorCount();
    Log.enableFailQuick(true);

    boolean valid = service.checkOrFalse(service.newChecker(), concrete, reference, "ref");

    assertFalse(valid);
    assertTrue(Log.isFailQuickEnabled());
    assertEquals(errorsBefore, Log.getErrorCount());
  }

  @Test
  void manualFallbackDoesNotRetainFailedChecker() throws IOException {
    ASTCDCompilationUnit reference =
        parse("Reference.cd", "classdiagram Reference { class Required { int value; } }");
    ASTCDCompilationUnit concrete =
        parse("Concrete.cd", "classdiagram Concrete { class Unrelated; }");
    MappingConformanceService service = new MappingConformanceService(Set.of(NAME_MAPPING));

    AdaptationContextFactory.AdaptationContextResult result =
        new AdaptationContextFactory(Set.of(NAME_MAPPING), service)
            .buildResults(
                CDModelIndex.of(reference), CDModelIndex.of(concrete), Set.of("ref"), true)
            .get("ref");

    assertFalse(result.conformanceValid());
    assertEquals(null, result.checker());
  }

  @ParameterizedTest(name = "mapping = {0}, name matching = {1}")
  @CsvSource({"prof, false", "prof, true", "stud, false", "stud, true"})
  void inheritedRoleConformanceDependsOnNameMapping(String mapping, boolean nameMatching) {
    Path fixture =
        Path.of("src/test/resources/de/monticore/codeAdaption/evaluation/confCheckingCD");
    ASTCDCompilationUnit reference = JavaLoader.parseCD(fixture.resolve("Reference.cd").toString());
    ASTCDCompilationUnit concrete = JavaLoader.parseCD(fixture.resolve("Concrete.cd").toString());
    Set<CDConfParameter> parameters =
        EnumSet.of(INHERITANCE, STEREOTYPE_MAPPING, SRC_TARGET_ASSOC_MAPPING);
    if (nameMatching) {
      parameters.add(NAME_MAPPING);
    }
    MappingConformanceService service = new MappingConformanceService(parameters);

    assertEquals(
        !nameMatching,
        service.checkOrFalse(service.newChecker(), concrete, reference, mapping),
        "Name matching also maps the shared Role base, which lacks the required association");
  }

  private ASTCDCompilationUnit parse(String name, String source) throws IOException {
    Path path = tempDir.resolve(name);
    Files.writeString(path, source);
    return JavaLoader.parseCD(path.toString());
  }
}
