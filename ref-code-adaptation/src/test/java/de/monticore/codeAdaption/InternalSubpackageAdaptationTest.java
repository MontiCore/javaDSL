package de.monticore.codeAdaption;

import static de.monticore.cdconformance.CDConfParameter.*;
import static de.monticore.codeAdaption.utils.AdapterParam.ANNOTATION_MATCHING;
import static de.monticore.codeAdaption.utils.AdapterParam.IGNORE_NON_MATCHED_VAR;
import static de.monticore.codeAdaption.utils.AdapterParam.NAME_MATCHING;
import static org.junit.jupiter.api.Assertions.*;

import de.monticore.cdconformance.CDConfParameter;
import de.monticore.cdconformance.CDConformanceChecker;
import de.monticore.codeAdaption.context.ConcretizationService;
import de.monticore.codeAdaption.utils.CDModelIndex;
import de.monticore.codeAdaption.utils.JavaLoader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Internal CD/Java package coverage, including the qualified-signature completion boundary. */
class InternalSubpackageAdaptationTest extends AdapterAbstractTest {
  private static final Path FIXTURE =
      Path.of("src/test/resources/de/monticore/codeAdaption/internalSubpackages");
  private static final Set<CDConfParameter> PARAMETERS =
      Set.of(
          STEREOTYPE_MAPPING, NAME_MAPPING, ADAPTED_NAME_MAPPING,
          SRC_TARGET_ASSOC_MAPPING, INHERITANCE, METHOD_OVERLOADING);

  @TempDir Path temporaryDirectory;

  @BeforeEach
  void setUp() {
    initMills();
  }

  @Test
  void completerCompletesMembersInsideInternalPackages() {
    var reference = JavaLoader.parseCD(FIXTURE.resolve("Reference.cd").toString());
    var concrete = JavaLoader.parseCD(FIXTURE.resolve("Concrete.cd").toString());
    var original = concrete.deepClone();

    var completed =
        new ConcretizationService(PARAMETERS).completeConcreteCD(concrete, reference, Set.of("ref"));

    var index = CDModelIndex.of(completed);
    assertEquals(Set.of("Customer", "CustomerService"), index.typeNames());
    assertTrue(index.attribute("Customer", "id").isPresent());
    assertEquals(
        "int", index.methods("CustomerService", "value").get(0).getMCReturnType().printType());
    assertTrue(index.type("Customer").orElseThrow().getSymbol().getFullName().contains(".model."));
    assertTrue(
        index.type("CustomerService").orElseThrow().getSymbol().getFullName().contains(".service."));
    assertTrue(new CDConformanceChecker(PARAMETERS).checkConformance(completed, reference, "ref"));
    assertTrue(concrete.deepEquals(original), "Completion must preserve the supplied concrete AST");
  }

  @Test
  void currentCompleterRejectsAQualifiedCrossPackageMethodSignature() {
    var reference =
        JavaLoader.parseCD(FIXTURE.resolve("ReferenceWithCrossPackageSignature.cd").toString());
    var concrete = JavaLoader.parseCD(FIXTURE.resolve("Concrete.cd").toString());
    var original = concrete.deepClone();

    var failure =
        assertThrows(
            IllegalStateException.class,
            () ->
                new ConcretizationService(PARAMETERS)
                    .completeConcreteCD(concrete, reference, Set.of("ref")));

    assertNotNull(failure.getCause());
    assertEquals("Completion result is not conform", failure.getCause().getMessage());
    assertTrue(concrete.deepEquals(original));
  }

  @ParameterizedTest(name = "TOP separation = {0}")
  @ValueSource(booleans = {false, true})
  void adaptsInternalJavaSubpackagesAndRelocatesTheirCrossPackageReferences(boolean topSeparation)
      throws Exception {
    Path output = temporaryDirectory.resolve("adapted");
    String referenceBefore = Files.readString(FIXTURE.resolve("Reference.cd"));
    String concreteBefore = Files.readString(FIXTURE.resolve("Concrete.cd"));
    String helperBefore =
        Files.readString(FIXTURE.resolve("reference/reference/service/internal/EntitySupport.java"));

    CodeAdapter adapter =
        new CodeAdapter(Set.of(NAME_MATCHING, ANNOTATION_MATCHING, IGNORE_NON_MATCHED_VAR), PARAMETERS);
    if (topSeparation) {
      adapter.adaptWithTopSeparation(
          FIXTURE.resolve("Reference.cd").toFile(),
          FIXTURE.resolve("Concrete.cd").toFile(),
          Set.of("ref"),
          FIXTURE.resolve("reference"),
          FIXTURE.resolve("concrete"),
          output,
          true,
          true);
    } else {
      adapter.adapt(
          FIXTURE.resolve("Reference.cd").toFile(),
          FIXTURE.resolve("Concrete.cd").toFile(),
          Set.of("ref"),
          FIXTURE.resolve("reference"),
          FIXTURE.resolve("concrete"),
          output,
          true,
          true);
    }

    assertTrue(Files.isRegularFile(output.resolve("application/model/Customer.java")));
    assertTrue(Files.isRegularFile(output.resolve("application/service/CustomerService.java")));
    assertTrue(Files.isRegularFile(output.resolve("reference/service/internal/EntitySupport.java")));
    assertFalse(generatedFileNames(output).contains("UnrelatedSupport.java"));
    assertFalse(generatedFileNames(output).contains("Entity.java"));
    assertFalse(generatedFileNames(output).contains("Service.java"));
    String support = readFileContent(output, "EntitySupport.java");
    assertTrue(support.contains("application.model.Customer"), support);
    for (Path source : generatedJavaFiles(output)) {
      assertFalse(Files.readString(source).contains("reference.model.Entity"), source.toString());
    }
    var completed = JavaLoader.parseCD(output.resolve("Concrete.cd").toString());
    assertTrue(
        new CDConformanceChecker(PARAMETERS)
            .checkConformance(
                completed, JavaLoader.parseCD(FIXTURE.resolve("Reference.cd").toString()), "ref"));
    assertNoAdapterMetadata(output);
    assertEquals(7, compileAndInvoke(output));
    assertEquals(referenceBefore, Files.readString(FIXTURE.resolve("Reference.cd")));
    assertEquals(concreteBefore, Files.readString(FIXTURE.resolve("Concrete.cd")));
    assertEquals(
        helperBefore,
        Files.readString(FIXTURE.resolve("reference/reference/service/internal/EntitySupport.java")));
  }

  @ParameterizedTest(name = "manual mode, TOP separation = {0}, annotation = {1}")
  @CsvSource({"false, false", "false, true", "true, false", "true, true"})
  void manualModePreservesAQualifiedCrossPackageMethodSignature(
      boolean topSeparation, boolean explicitAnnotation)
      throws Exception {
    Path reference = FIXTURE.resolve("ReferenceWithCrossPackageSignature.cd");
    Path concrete = FIXTURE.resolve("ConcreteWithCrossPackageSignature.cd");
    Path fixtureSource = FIXTURE.resolve("manual-reference");
    Path source = temporaryDirectory.resolve("manual-reference");
    for (Path input : generatedJavaFiles(fixtureSource)) {
      Path target = source.resolve(fixtureSource.relativize(input));
      Files.createDirectories(target.getParent());
      String content = Files.readString(input);
      if (!explicitAnnotation) {
        content = content.replace("  @Adapt(ref = \"Service.echo\", template = \"${}\")", "");
      }
      Files.writeString(target, content);
    }
    Path output = temporaryDirectory.resolve("manual-adapted");
    String concreteBefore = Files.readString(concrete);
    String serviceBefore = Files.readString(source.resolve("reference/service/Service.java"));
    CodeAdapter adapter =
        new CodeAdapter(Set.of(NAME_MATCHING, ANNOTATION_MATCHING, IGNORE_NON_MATCHED_VAR), PARAMETERS);

    // Both Java matching strategies must preserve the explicitly mapped CD signature and body.
    if (topSeparation) {
      adapter.adaptWithTopSeparation(
          reference.toFile(),
          concrete.toFile(),
          Set.of("ref"),
          source,
          FIXTURE.resolve("concrete"),
          output,
          false,
          true);
    } else {
      adapter.adapt(
          reference.toFile(),
          concrete.toFile(),
          Set.of("ref"),
          source,
          FIXTURE.resolve("concrete"),
          output,
          false,
          true);
    }

    assertTrue(Files.isRegularFile(output.resolve("application/model/Customer.java")));
    assertTrue(Files.isRegularFile(output.resolve("application/service/CustomerService.java")));
    assertEquals(topSeparation, generatedFileNames(output).contains("CustomerServiceTOP.java"));
    assertFalse(Files.exists(output.resolve(concrete.getFileName())));
    String service =
        readFileContent(output, topSeparation ? "CustomerServiceTOP.java" : "CustomerService.java");
    assertEquals(1, service.split("Customer echo\\(", -1).length - 1, service);
    assertTrue(service.contains("Customer echo(Customer customer)"), service);
    assertTrue(service.contains("return customer;"), service);
    assertTrue(service.contains("return echo(entity).id;"), service);
    assertTrue(service.contains("application.model.Customer"), service);
    assertNoAdapterMetadata(output);
    assertEquals(7, compileAndInvoke(output));
    assertEquals(concreteBefore, Files.readString(concrete));
    assertEquals(serviceBefore, Files.readString(source.resolve("reference/service/Service.java")));
  }

  private Object compileAndInvoke(Path output) throws Exception {
    Path classes = Files.createDirectory(temporaryDirectory.resolve("classes"));
    var compiler = ToolProvider.getSystemJavaCompiler();
    assertNotNull(compiler, "The regression requires a JDK");
    StringWriter diagnostics = new StringWriter();
    try (var manager = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8)) {
      assertEquals(
          Boolean.TRUE,
          compiler
              .getTask(
                  diagnostics,
                  manager,
                  null,
                  List.of("-d", classes.toString(), "-classpath", classes.toString(), "-proc:none"),
                  null,
                  manager.getJavaFileObjectsFromPaths(generatedJavaFiles(output)))
              .call(),
          diagnostics::toString);
    }
    try (var loader =
        new URLClassLoader(
            new URL[] {classes.toUri().toURL()}, ClassLoader.getPlatformClassLoader())) {
      Class<?> service = loader.loadClass("application.service.CustomerService");
      return service.getMethod("value").invoke(service.getConstructor().newInstance());
    }
  }
}
