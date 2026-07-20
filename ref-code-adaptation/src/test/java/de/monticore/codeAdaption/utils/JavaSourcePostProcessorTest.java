package de.monticore.codeAdaption.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JavaSourcePostProcessorTest {

  @TempDir Path tempDir;

  @Test
  void preservesCommentsAndLiteralsWhileRemovingInvalidImports() {
    String source =
        """
        package demo;
        import Person;
        import de.monticore.codeAdaption.utils.Adapt;
        import java.util.HashMap;

        public class Example {
          String url = "http://example/a,b";
          String block = "/* not a comment */";
          java.util.Map<String,String> map = new HashMap<>();

          void m() {
            int x=1; // keep comma,a and equals=
            /* keep generic List<String> text */
          }
        }
        """;

    String cleaned = JavaSourcePostProcessor.process(source);

    assertFalse(cleaned.contains("import Person;"));
    assertFalse(cleaned.contains("import de.monticore.codeAdaption.utils.Adapt;"));
    assertTrue(cleaned.contains("import java.util.HashMap;"));
    assertTrue(cleaned.contains("\"http://example/a,b\""));
    assertTrue(cleaned.contains("int x=1; // keep comma,a and equals="));
    assertTrue(cleaned.contains("/* keep generic List<String> text */"));
  }

  @Test
  void removesInvalidImportsAndPreservesExistingValidImportsWithoutAddingJdkImports() {
    String source =
        """
        package demo;

        // List in a comment must not drive imports by itself.
        import de.monticore.codeAdaption.utils.Adapt;
        import Person;
        import static java.util.Collections.emptyList;
        import java.io.File;

        public class Sample {
          private List<String> names;
          private Map<String, Optional<UUID>> ids;
          private ZonedDateTime created;
          private File file;
          private String literal = "Set";
        }
        """;

    String cleaned = JavaSourcePostProcessor.process(source, "Sample.java");

    assertFalse(cleaned.contains("import de.monticore.codeAdaption.utils.Adapt;"));
    assertFalse(cleaned.contains("import Person;"));
    assertTrue(cleaned.contains("import static java.util.Collections.emptyList;"));
    assertTrue(cleaned.contains("import java.io.File;"));
    assertFalse(cleaned.contains("import java.util.List;"));
    assertFalse(cleaned.contains("import java.util.Map;"));
    assertFalse(cleaned.contains("import java.util.Optional;"));
    assertFalse(cleaned.contains("import java.util.UUID;"));
    assertFalse(cleaned.contains("import java.time.ZonedDateTime;"));
    assertFalse(cleaned.contains("import java.util.Set;"));
  }

  @Test
  void preservesExistingJavaUtilWildcardWithoutAddingExplicitImports() {
    String source =
        """
        package demo;

        import java.util.*;

        public class Sample {
          private List<String> names;
        }
        """;

    String cleaned = JavaSourcePostProcessor.process(source, "Sample.java");

    assertTrue(cleaned.contains("import java.util.*;"));
    assertFalse(cleaned.contains("import java.util.List;"));
  }

  @Test
  void doesNotInventImportsForUnresolvedSimpleNames() {
    String source =
        """
        package demo;

        class List {}

        public class Sample {
          private List names;
          private Optional<String> value;
        }
        """;

    String cleaned = JavaSourcePostProcessor.process(source, "Sample.java");

    assertFalse(cleaned.contains("import java.util.List;"));
    assertFalse(cleaned.contains("import java.util.Optional;"));
  }

  @Test
  void processDirectoryDoesNotInventImportsForSamePackageTypes() throws IOException {
    Path list = tempDir.resolve("List.java");
    Path sample = tempDir.resolve("Sample.java");
    Files.writeString(
        list,
        """
        package demo;

        public class List {}
        """,
        StandardCharsets.UTF_8);
    Files.writeString(
        sample,
        """
        package demo;

        public class Sample {
          private List names;
          private Map<String, String> values;
        }
        """,
        StandardCharsets.UTF_8);

    JavaSourcePostProcessor.processDirectory(tempDir);
    String cleaned = Files.readString(sample, StandardCharsets.UTF_8);

    assertFalse(cleaned.contains("import java.util.List;"));
    assertFalse(cleaned.contains("import java.util.Map;"));
  }

  @Test
  void removesOnlyMalformedParameterizedImportLines() {
    String source =
        """
        package demo;

        import Optional<long>;
        import static java.util.Collections.emptyList;

        public class Sample {
          private Optional<Long> value;
          private List<Integer> numbers;
        }
        """;

    String cleaned = JavaSourcePostProcessor.process(source, "Sample.java");

    assertFalse(cleaned.contains("import Optional<long>;"));
    assertTrue(cleaned.contains("import static java.util.Collections.emptyList;"));
    assertFalse(cleaned.contains("import java.util.Optional;"));
    assertFalse(cleaned.contains("import java.util.List;"));
    assertTrue(cleaned.contains("Optional<Long> value"));
    assertTrue(cleaned.contains("List<Integer> numbers"));
  }

  @Test
  void malformedImportRecoveryPreservesCommentsAndTextBlocks() {
    String source =
        String.join(
            System.lineSeparator(),
            "package demo;",
            "",
            "/*",
            " import Commented<Type>;",
            "*/",
            "import Optional<long>;",
            "",
            "public class Sample {",
            "  String text = \"\"\"",
            "      import TextBlock<Type>;",
            "      \"\"\";",
            "  String literal = \"import Literal<Type>;\";",
            "}",
            "");

    String cleaned = JavaSourcePostProcessor.process(source, "Sample.java");

    assertFalse(cleaned.contains("import Optional<long>;"));
    assertTrue(cleaned.contains("import Commented<Type>;"));
    assertTrue(cleaned.contains("import TextBlock<Type>;"));
    assertTrue(cleaned.contains("\"import Literal<Type>;\""));
  }

  @Test
  void printAstPreservesPackageDirectoriesForSameSimpleNames() throws IOException {
    Path inputDir = tempDir.resolve("input");
    Path outputDir = tempDir.resolve("output");
    Files.createDirectories(inputDir.resolve("a"));
    Files.createDirectories(inputDir.resolve("b"));
    Files.writeString(
        inputDir.resolve("a").resolve("User.java"),
        """
        package a;

        public class User {}
        """,
        StandardCharsets.UTF_8);
    Files.writeString(
        inputDir.resolve("b").resolve("User.java"),
        """
        package b;

        public class User {}
        """,
        StandardCharsets.UTF_8);

    Set<ASTOrdinaryCompilationUnit> asts = JavaLoader.readJavaCode(inputDir);
    JavaLoader.printAST(asts, outputDir);

    assertTrue(Files.exists(outputDir.resolve("a").resolve("User.java")));
    assertTrue(Files.exists(outputDir.resolve("b").resolve("User.java")));
    assertFalse(Files.exists(outputDir.resolve("User.java")));
  }

}
