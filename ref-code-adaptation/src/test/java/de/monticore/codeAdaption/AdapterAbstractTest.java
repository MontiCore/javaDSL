package de.monticore.codeAdaption;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import de.monticore.cd._symboltable.BuiltInTypes;
import de.monticore.cd4code.CD4CodeMill;
import de.monticore.cdconcretization.UnderspecifiedPlaceholderType;
import de.monticore.java.javadsl.JavaDSLMill;
import de.se_rwth.commons.logging.Log;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public abstract class AdapterAbstractTest {

  public static void initMills() {
    JavaDSLMill.init();
    JavaDSLMill.globalScope().clear();
    CD4CodeMill.init();
    CD4CodeMill.globalScope().clear();
    BuiltInTypes.addBuiltInTypes(CD4CodeMill.globalScope());
    UnderspecifiedPlaceholderType.addPlaceholderType(CD4CodeMill.globalScope());
    Log.init();
  }

  protected static String readFileContent(Path outputPath, String filename) {
    Path filePath = findFile(outputPath, filename);
    assertTrue(Files.exists(filePath), () -> "Expected generated file: " + filePath);
    try {
      return Files.readString(filePath, StandardCharsets.UTF_8);
    } catch (IOException e) {
      fail("Failed to read file: " + filePath + " (" + e.getMessage() + ")");
      return "";
    }
  }

  private static Path findFile(Path outputPath, String filename) {
    Path direct = outputPath.resolve(filename);
    if (Files.exists(direct)) {
      return direct;
    }
    try (Stream<Path> paths = Files.walk(outputPath)) {
      return paths
          .filter(Files::isRegularFile)
          .filter(path -> path.getFileName().toString().equals(filename))
          .findFirst()
          .orElse(direct);
    } catch (IOException e) {
      return direct;
    }
  }

  protected static Set<String> generatedFileNames(Path outputPath) {
    try (Stream<Path> files = Files.walk(outputPath)) {
      return files
          .filter(Files::isRegularFile)
          .map(path -> path.getFileName().toString())
          .collect(Collectors.toSet());
    } catch (IOException e) {
      fail("Failed to list generated files in " + outputPath + ": " + e.getMessage());
      return Set.of();
    }
  }

  protected static List<Path> generatedJavaFiles(Path sourceDir) {
    try (Stream<Path> files = Files.walk(sourceDir)) {
      return files
          .filter(path -> path.toString().endsWith(".java"))
          .sorted()
          .collect(Collectors.toCollection(ArrayList::new));
    } catch (IOException e) {
      fail("Failed to list generated Java files in " + sourceDir + ": " + e.getMessage());
      return List.of();
    }
  }

  protected static List<Path> generatedJavaFilesRecursively(Path sourceDir) {
    assertTrue(Files.isDirectory(sourceDir), () -> "Expected Java source directory: " + sourceDir);
    try (Stream<Path> files = Files.walk(sourceDir)) {
      return files
          .filter(Files::isRegularFile)
          .filter(path -> path.toString().endsWith(".java"))
          .sorted()
          .collect(Collectors.toCollection(ArrayList::new));
    } catch (IOException e) {
      fail("Failed to list generated Java files in " + sourceDir + ": " + e.getMessage());
      return List.of();
    }
  }

  protected static void assertNoAdapterMetadata(Path outputPath) {
    for (Path file : generatedJavaFilesRecursively(outputPath)) {
      try {
        String content = Files.readString(file, StandardCharsets.UTF_8);
        assertFalse(content.contains("@Adapt"), () -> "Adapter annotation leaked into " + file);
        assertFalse(
            content.contains("de.monticore.codeAdaption.utils.Adapt"),
            () -> "Adapter import leaked into " + file);
      } catch (IOException e) {
        fail("Failed to inspect generated Java metadata in " + file + ": " + e.getMessage());
      }
    }
  }

  protected static void assertGeneratedJavaCompiles(Path sourceDir) {
    assertGeneratedJavaCompiles(generatedJavaFiles(sourceDir));
  }

  protected static void assertGeneratedJavaCompiles(List<Path> javaFiles) {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    assertNotNull(compiler, "Tests must run on a JDK with javax.tools.JavaCompiler available");

    Path classesDir =
        javaFiles.isEmpty()
            ? Path.of("target", "adapter", "_compile")
            : javaFiles.get(0).toAbsolutePath().getParent().resolve("_compile");
    try {
      deleteRecursively(classesDir);
      Files.createDirectories(classesDir);
      StringWriter compilerOutput = new StringWriter();
      try (StandardJavaFileManager fileManager =
          compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8)) {
        Iterable<? extends javax.tools.JavaFileObject> compilationUnits =
            fileManager.getJavaFileObjectsFromPaths(javaFiles);
        List<String> options =
            List.of(
                "-d",
                classesDir.toString(),
                "-classpath",
                System.getProperty("java.class.path"));
        Boolean compiled =
            compiler.getTask(compilerOutput, fileManager, null, options, null, compilationUnits)
                .call();
        assertEquals(
            Boolean.TRUE,
            compiled,
            () -> "Generated Java does not compile" + System.lineSeparator()
                + compilerOutput);
      }
    } catch (IOException e) {
      fail("Failed to compile generated Java: " + e.getMessage());
    } finally {
      deleteRecursively(classesDir);
    }
  }

  protected static void deleteRecursively(Path path) {
    if (path == null || !Files.exists(path)) {
      return;
    }
    try (Stream<Path> paths = Files.walk(path)) {
      for (Path current : paths.sorted(Comparator.reverseOrder()).toList()) {
        deletePathWithRetry(current);
      }
    } catch (IOException e) {
      fail("Failed to delete path recursively: " + path + " (" + e.getMessage() + ")");
    }
  }

  protected static void deletePathWithRetry(Path path) {
    IOException last = null;
    for (int attempt = 0; attempt < 5; attempt++) {
      try {
        Files.deleteIfExists(path);
        return;
      } catch (IOException e) {
        last = e;
        try {
          Thread.sleep(100L * (attempt + 1));
        } catch (InterruptedException interrupted) {
          Thread.currentThread().interrupt();
          fail("Interrupted while deleting " + path + ": " + interrupted.getMessage());
        }
      }
    }
    fail("Failed to delete path: " + path + " (" + (last == null ? "unknown" : last.getMessage()) + ")");
  }
}
