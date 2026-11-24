package de.monticore.codeAdaption;

import static de.monticore.cdconformance.CDConfParameter.INHERITANCE;
import static de.monticore.cdconformance.CDConfParameter.NAME_MAPPING;
import static de.monticore.cdconformance.CDConfParameter.STEREOTYPE_MAPPING;
import static de.monticore.cdconformance.CDConfParameter.STRICT_PARAMETER_ORDER;
import static de.monticore.codeAdaption.utils.AdapterParam.ANNOTATION_MATCHING;
import static de.monticore.codeAdaption.utils.AdapterParam.NAME_MATCHING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.monticore.codeAdaption.utils.AdapterParam;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/** Shared checks for the bounded mode and composition experiments. */
abstract class ComparisonEvaluationSupport extends AdapterAbstractTest {
  protected static final Path EXAMPLE =
      Path.of("src/test/resources/de/monticore/codeAdaption/evaluation/comparison_evaluation");

  protected static CodeAdapter adapter() {
    return adapter(Set.of(NAME_MATCHING, ANNOTATION_MATCHING));
  }

  protected static CodeAdapter adapter(Set<AdapterParam> parameters) {
    return new CodeAdapter(
        parameters,
        Set.of(NAME_MAPPING, STEREOTYPE_MAPPING, INHERITANCE, STRICT_PARAMETER_ORDER));
  }

  /** Excludes the optional completed CD, so comparisons concern the emitted Java tree. */
  protected static Map<String, String> javaSnapshot(Path source) throws IOException {
    Map<String, String> snapshot = new TreeMap<>();
    for (Path file : generatedJavaFilesRecursively(source)) {
      snapshot.put(source.relativize(file).toString(), Files.readString(file));
    }
    return snapshot;
  }

  /**
   * Compiles only the emitted Java against an empty classpath, then executes it in a loader whose
   * parent exposes JDK classes only. This excludes test-classpath dependencies and model stubs.
   */
  protected static Object invoke(Path output, String type, String method) throws Exception {
    Path classes = Files.createTempDirectory(output.getParent(), "comparison-classes-");
    try {
      JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
      assertNotNull(compiler, "Evaluation requires a JDK");
      StringWriter diagnostics = new StringWriter();
      try (StandardJavaFileManager manager =
          compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8)) {
        Boolean compiled =
            compiler
                .getTask(
                    diagnostics,
                    manager,
                    null,
                    List.of("-d", classes.toString(), "-classpath", classes.toString(), "-proc:none"),
                    null,
                    manager.getJavaFileObjectsFromPaths(generatedJavaFilesRecursively(output)))
                .call();
        assertEquals(Boolean.TRUE, compiled, () -> "Standalone Java compilation failed:\n" + diagnostics);
      }
      try (URLClassLoader loader =
          new URLClassLoader(new URL[] {classes.toUri().toURL()}, ClassLoader.getPlatformClassLoader())) {
        Class<?> emittedType = Class.forName("evaluationcomparison." + type, true, loader);
        Object instance = emittedType.getConstructor().newInstance();
        return emittedType.getMethod(method).invoke(instance);
      }
    } finally {
      deleteRecursively(classes);
    }
  }
}
