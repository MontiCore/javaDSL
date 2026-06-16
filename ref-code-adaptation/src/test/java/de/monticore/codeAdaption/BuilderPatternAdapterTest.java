package de.monticore.codeAdaption;

import static de.monticore.cdconformance.CDConfParameter.*;
import static de.monticore.codeAdaption.utils.AdapterParam.*;
import static org.junit.jupiter.api.Assertions.*;

import de.monticore.cdconformance.CDConfParameter;
import de.monticore.codeAdaption.utils.AdapterParam;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class BuilderPatternAdapterTest extends AdapterAbstractTest {
  private final String resourcesPath =
      "src/test/resources/de/monticore/codeAdaption/evaluation/testcase_6_builder_pattern/";
  private final File refCD = new File(resourcesPath + "Reference.cd");
  private final File concreteCD = new File(resourcesPath + "Concrete.cd");
  private final Path concreteCodePath = Path.of(resourcesPath + "concrete");
  private final Path adapterCodePath = Path.of(resourcesPath + "adapter");
  private final Path outputPath = Path.of("target/adapter/builder_pattern");

  private Set<CDConfParameter> confParameters;
  private Set<AdapterParam> adapterParams;

  @BeforeEach
  public void setup() throws IOException {
    initMills();
    deleteRecursively(outputPath);
    confParameters = Set.of(NAME_MAPPING, INHERITANCE, STEREOTYPE_MAPPING, STRICT_PARAMETER_ORDER);
    adapterParams = Set.of(NAME_MATCHING, ANNOTATION_MATCHING, IGNORE_NON_MATCHED_VAR, IGNORE_NON_MATCHED_TYPE);
  }

  @Test
  @DisplayName("Builder Pattern: Adapt Builder to create PersonBuilder and TaskBuilder")
  public void testBuilderPatternAdaptation() {
    CodeAdapter adapter = new CodeAdapter(adapterParams, confParameters);

    assertDoesNotThrow(
        () ->
            adapter.adapt(
                refCD, concreteCD, Set.of("buildPat"), adapterCodePath, concreteCodePath, outputPath));

    assertTrue(Files.isRegularFile(outputPath.resolve("PersonBuilder.java")));
    assertTrue(Files.isRegularFile(outputPath.resolve("TaskBuilder.java")));
    assertTrue(Files.isRegularFile(outputPath.resolve("Person.java")));
    assertTrue(Files.isRegularFile(outputPath.resolve("Task.java")));
    assertEquals(
        Set.of("PersonBuilder.java", "TaskBuilder.java", "Person.java", "Task.java"),
        generatedFileNames(outputPath));
    assertNoAdapterMetadata(outputPath);
    assertGeneratedJavaCompiles(outputPath);

    String personBuilderContent = readFileContent(outputPath, "PersonBuilder.java");
    String taskBuilderContent = readFileContent(outputPath, "TaskBuilder.java");

    assertTrue(personBuilderContent.contains("public class PersonBuilder"));
    assertTrue(personBuilderContent.contains("public PersonBuilder setName(String name)"));
    assertTrue(personBuilderContent.contains("public PersonBuilder setAge(int age)"));
    assertTrue(personBuilderContent.contains("public PersonBuilder setEmail(String email)"));
    assertTrue(personBuilderContent.contains("public PersonBuilder setPhone(String phone)"));
    assertTrue(personBuilderContent.contains("public Person build()"));
    assertTrue(personBuilderContent.contains("return this;"));

    assertTrue(taskBuilderContent.contains("public class TaskBuilder"));
    assertTrue(taskBuilderContent.contains("public TaskBuilder setId(int id)"));
    assertTrue(taskBuilderContent.contains("public TaskBuilder setTitle(String title)"));
    assertTrue(taskBuilderContent.contains("public TaskBuilder setDescription(String description)"));
    assertTrue(taskBuilderContent.contains("public TaskBuilder setStatus(String status)"));
    assertTrue(taskBuilderContent.contains("public TaskBuilder setDueDate(Date dueDate)"));
    assertTrue(taskBuilderContent.contains("public TaskBuilder setAssignee(String assignee)"));
    assertTrue(taskBuilderContent.contains("public Task build()"));
    assertTrue(taskBuilderContent.contains("return this;"));

    assertTrue(personBuilderContent.contains("String nameField"));
    assertTrue(personBuilderContent.contains("int ageField"));
    assertTrue(personBuilderContent.contains("String emailField"));
    assertTrue(personBuilderContent.contains("String phoneField"));

    assertTrue(taskBuilderContent.contains("int idField"));
    assertTrue(taskBuilderContent.contains("String titleField"));
    assertTrue(taskBuilderContent.contains("String descriptionField"));
    assertTrue(taskBuilderContent.contains("String statusField"));
    assertTrue(taskBuilderContent.contains("Date dueDateField"));
    assertTrue(taskBuilderContent.contains("String assigneeField"));

    assertTrue(personBuilderContent.contains("this.nameField = name"));
    assertTrue(personBuilderContent.contains("this.ageField = age"));

    assertTrue(taskBuilderContent.contains("this.idField = id"));
    assertTrue(taskBuilderContent.contains("this.titleField = title"));

    assertTrue(personBuilderContent.contains("new Person("));
    assertTrue(taskBuilderContent.contains("new Task("));
    assertTrue(
        personBuilderContent.contains(
            "return new Person(this.nameField, this.ageField, this.emailField, this.phoneField);"));
    assertTrue(
        taskBuilderContent
            .contains(
                "return new Task(this.idField, this.titleField, this.descriptionField, this.statusField, this.dueDateField, this.assigneeField);"));
    assertFalse(personBuilderContent.contains("class Builder"));
    assertFalse(taskBuilderContent.contains("class Builder"));

    String personContent = readFileContent(outputPath, "Person.java");
    String taskContent = readFileContent(outputPath, "Task.java");

    assertTrue(personContent.contains("public class Person"));
    assertTrue(personContent.contains("String name"));
    assertTrue(personContent.contains("int age"));

    assertTrue(taskContent.contains("public class Task"));
    assertTrue(taskContent.contains("int id"));
    assertTrue(taskContent.contains("String title"));
  }

  private String readFileContent(Path outputPath, String filename) {
    try {
      Path filePath = outputPath.resolve(filename);
      assertTrue(Files.exists(filePath));
      return Files.readString(filePath, StandardCharsets.UTF_8);
    } catch (IOException e) {
      fail("Failed to read file: " + filename);
      return "";
    }
  }

  private Set<String> generatedFileNames(Path outputPath) {
    try (var files = Files.list(outputPath)) {
      return files
          .filter(Files::isRegularFile)
          .map(path -> path.getFileName().toString())
          .collect(Collectors.toSet());
    } catch (IOException e) {
      fail("Failed to list generated files");
      return Set.of();
    }
  }

  private void assertNoAdapterMetadata(Path outputPath) {
    try (var files = Files.list(outputPath)) {
      for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
        String content = Files.readString(file, StandardCharsets.UTF_8);
        assertFalse(content.contains("@Adapt"), () -> "Adapter annotation leaked into " + file);
        assertFalse(
            content.contains("de.monticore.codeAdaption.utils.Adapt"),
            () -> "Adapter import leaked into " + file);
      }
    } catch (IOException e) {
      fail("Failed to inspect generated Java metadata: " + e.getMessage());
    }
  }

  private void assertGeneratedJavaCompiles(Path sourceDir) {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    assertNotNull(compiler, "Tests must run on a JDK with javax.tools.JavaCompiler available");

    Path classesDir = sourceDir.resolve("_compile");
    try {
      deleteRecursively(classesDir);
      Files.createDirectories(classesDir);
      List<Path> javaFiles = generatedJavaFiles(sourceDir);
      DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
      try (StandardJavaFileManager fileManager =
          compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8)) {
        Iterable<? extends JavaFileObject> compilationUnits =
            fileManager.getJavaFileObjectsFromPaths(javaFiles);
        List<String> options =
            List.of(
                "-d",
                classesDir.toString(),
                "-classpath",
                System.getProperty("java.class.path"));
        Boolean compiled =
            compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits).call();
        assertTrue(
            Boolean.TRUE.equals(compiled),
            () ->
                diagnostics.getDiagnostics().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(System.lineSeparator())));
      }
    } catch (IOException e) {
      fail("Failed to compile generated Java: " + e.getMessage());
    } finally {
      try {
        deleteRecursively(classesDir);
      } catch (IOException ignored) {
        // Temporary compile output is not part of the oracle.
      }
    }
  }

  private List<Path> generatedJavaFiles(Path sourceDir) throws IOException {
    try (var files = Files.list(sourceDir)) {
      return files
          .filter(path -> path.toString().endsWith(".java"))
          .sorted()
          .collect(Collectors.toCollection(ArrayList::new));
    }
  }

  private void deleteRecursively(Path path) throws IOException {
    if (!Files.exists(path)) {
      return;
    }
    try (var paths = Files.walk(path)) {
      for (Path current : paths.sorted(Comparator.reverseOrder()).toList()) {
        Files.deleteIfExists(current);
      }
    }
  }
}

