package de.monticore.codeAdaption.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JavaSourcePostProcessorTest {

  @TempDir Path tempDir;

  @Test
  void removesInvalidImportsAndAddsExplicitJavaUtilImports() {
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
          private File file;
          private String literal = "Set";
        }
        """;

    String cleaned = JavaSourcePostProcessor.process(source, "Sample.java");

    assertFalse(cleaned.contains("import de.monticore.codeAdaption.utils.Adapt;"));
    assertFalse(cleaned.contains("import Person;"));
    assertTrue(cleaned.contains("import static java.util.Collections.emptyList;"));
    assertTrue(cleaned.contains("import java.io.File;"));
    assertTrue(cleaned.contains("import java.util.List;"));
    assertTrue(cleaned.contains("import java.util.Map;"));
    assertTrue(cleaned.contains("import java.util.Optional;"));
    assertTrue(cleaned.contains("import java.util.UUID;"));
    assertFalse(cleaned.contains("import java.util.Set;"));
  }

  @Test
  void preservesExistingJavaUtilWildcardInsteadOfAddingDuplicates() {
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
  void avoidsSameFileTypeCollisionsWhenResolvingJavaUtilTypes() {
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
    assertTrue(cleaned.contains("import java.util.Optional;"));
  }

  @Test
  void avoidsSamePackageTypeCollisionsWhenProcessingDirectory() throws IOException {
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
    assertTrue(cleaned.contains("import java.util.Map;"));
  }
}
