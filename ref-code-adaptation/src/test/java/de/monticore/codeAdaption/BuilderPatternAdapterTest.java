package de.monticore.codeAdaption;

import static de.monticore.cdconformance.CDConfParameter.*;
import static de.monticore.codeAdaption.utils.AdapterParam.*;
import static org.junit.jupiter.api.Assertions.*;

import de.monticore.cdconformance.CDConfParameter;
import de.monticore.codeAdaption.updater.CodeUpdaterMill;
import de.monticore.codeAdaption.updater.spoonUpdater.SpoonUpdater;
import de.monticore.codeAdaption.utils.AdapterParam;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class BuilderPatternAdapterTest extends AdapterAbstractTest {
  @TempDir Path temporaryDirectory;

  private final String resourcesPath =
      "src/test/resources/de/monticore/codeAdaption/evaluation/testcase_6_builder_pattern/";
  private final File refCD = new File(resourcesPath + "Reference.cd");
  private final File concreteCD = new File(resourcesPath + "Concrete.cd");
  private final Path concreteCodePath = Path.of(resourcesPath + "concrete");
  private final Path adapterCodePath = Path.of(resourcesPath + "adapter");
  private Path outputPath;

  private Set<CDConfParameter> confParameters;
  private Set<AdapterParam> adapterParams;

  @BeforeEach
  public void setup() {
    initMills();
    outputPath = temporaryDirectory.resolve("builder-pattern");
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

    assertEquals(
        Set.of("PersonBuilder.java", "TaskBuilder.java", "Person.java", "Task.java"),
        generatedFileNames(outputPath));
    assertNoAdapterMetadata(outputPath);
    assertGeneratedJavaCompiles(outputPath);

    String personBuilderContent = simplifyGeneratedReferences(readFileContent(outputPath, "PersonBuilder.java"));
    String taskBuilderContent = simplifyGeneratedReferences(readFileContent(outputPath, "TaskBuilder.java"));

    assertTrue(personBuilderContent.contains("public class PersonBuilder"));
    assertTrue(personBuilderContent.contains("public PersonBuilder setName(String name)"));
    assertTrue(personBuilderContent.contains("public PersonBuilder setAge(int age)"));
    assertTrue(personBuilderContent.contains("public PersonBuilder setEmail(String email)"));
    assertTrue(personBuilderContent.contains("public PersonBuilder setPhone(String phone)"));
    assertTrue(personBuilderContent.contains("public PersonBuilder setTags(List<String> tags)"));
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
    assertTrue(personBuilderContent.contains("List<String> tagsField"));

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
            "return new Person(this.nameField, this.ageField, this.emailField, this.phoneField, this.tagsField);"));
    assertTrue(
        taskBuilderContent
            .contains(
                "return new Task(this.idField, this.titleField, this.descriptionField, this.statusField, this.dueDateField, this.assigneeField);"));
    assertFalse(personBuilderContent.contains("class Builder"));
    assertFalse(taskBuilderContent.contains("class Builder"));

    String personContent = simplifyGeneratedReferences(readFileContent(outputPath, "Person.java"));
    String taskContent = simplifyGeneratedReferences(readFileContent(outputPath, "Task.java"));

    assertTrue(personContent.contains("public class Person"));
    assertTrue(personContent.contains("String name"));
    assertTrue(personContent.contains("int age"));

    assertTrue(taskContent.contains("public class Task"));
    assertTrue(taskContent.contains("int id"));
    assertTrue(taskContent.contains("String title"));
  }

  @Test
  @DisplayName("Builder Pattern: TOP separation preserves fluent public self types")
  void topSeparationCompilesFluentBuilderWithConcreteHwc() throws IOException {
    Path concreteWithBuilder = Files.createDirectories(temporaryDirectory.resolve("concrete"));
    Files.copy(concreteCodePath.resolve("Person.java"), concreteWithBuilder.resolve("Person.java"));
    Files.copy(concreteCodePath.resolve("Task.java"), concreteWithBuilder.resolve("Task.java"));
    String packageName = "de.monticore.codeAdaption.evaluation.testcase_6_builder_pattern";
    Files.writeString(
        concreteWithBuilder.resolve("PersonBuilder.java"),
        "package "
            + packageName
            + "; public class PersonBuilder extends PersonBuilderTOP {"
            + " public void validate() {} } ");
    Files.writeString(
        concreteWithBuilder.resolve("BuilderUsage.java"),
        "package "
            + packageName
            + "; class BuilderUsage { void use() {"
            + " new PersonBuilder().setName(\"Ada\").validate(); } }");
    Path topOutput = temporaryDirectory.resolve("top-output");

    new CodeAdapter(adapterParams, confParameters)
        .adaptWithTopSeparation(
            refCD,
            concreteCD,
            Set.of("buildPat"),
            adapterCodePath,
            concreteWithBuilder,
            topOutput);

    String personBuilderTop = readFileContent(topOutput, "PersonBuilderTOP.java");
    assertTrue(personBuilderTop.contains("return ((PersonBuilder) (this));"));
    assertGeneratedJavaCompiles(topOutput);
  }

  @Test
  @DisplayName("Builder Pattern: configured updater provider is used across isolated passes")
  public void usesConfiguredUpdaterProvider() {
    CodeAdapter adapter = new CodeAdapter(adapterParams, confParameters);
    Path millOutputPath = outputPath.resolve("mill");
    AtomicInteger createdUpdaters = new AtomicInteger();
    CodeUpdaterMill.init(
        () -> {
          createdUpdaters.incrementAndGet();
          return new SpoonUpdater();
        });

    try {
      assertDoesNotThrow(
          () ->
              adapter.adapt(
                  refCD,
                  concreteCD,
                  Set.of("buildPat"),
                  adapterCodePath,
                  concreteCodePath,
                  millOutputPath));

      assertTrue(
          createdUpdaters.get() > 1,
          "Adaptation should obtain separate updater instances for independent passes");
      assertTrue(generatedFileNames(millOutputPath).contains("PersonBuilder.java"));
      assertTrue(generatedFileNames(millOutputPath).contains("TaskBuilder.java"));
      assertGeneratedJavaCompiles(millOutputPath);
    } finally {
      CodeUpdaterMill.init();
    }
  }

  private static String simplifyGeneratedReferences(String content) {
    return content
        .replace("de.monticore.codeAdaption.evaluation.testcase_6_builder_pattern.", "")
        .replace("java.lang.", "")
        .replace("java.util.", "");
  }
}

