package de.monticore.codeAdaption;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.monticore.codeAdaption.utils.JavaSourcePostProcessor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

class CodeAdapterCleanupTest {

  @Test
  void postProcessorRemovesOnlyKnownInvalidImports() {
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
  void postProcessorDoesNotInventJavaUtilImportsForSourceIdentifiers() {
    String unresolvedSimpleName =
        """
        package demo;
        public class Example {
          List<String> names;
        }
        """;
    String commentOnly =
        """
        package demo;
        public class Example {
          String text = "List";
          // Map appears only in a comment.
        }
        """;

    String cleanedNeedsImport = JavaSourcePostProcessor.process(unresolvedSimpleName);
    assertTrue(cleanedNeedsImport.contains("package demo;"));
    assertFalse(cleanedNeedsImport.contains("import java.util.List;"));
    assertFalse(JavaSourcePostProcessor.process(commentOnly).contains("import java.util."));
  }

  @Test
  void postProcessorPreservesExistingImportsButDoesNotAddMissingImports() {
    String source =
        """
        package demo;
        import java.util.Set;

        public class Example {
          List<String> names;
          Set<String> ids;
          Map<String, Integer> lookup;
        }
        """;

    String cleaned = JavaSourcePostProcessor.process(source);

    assertFalse(cleaned.contains("import java.util.List;"));
    assertFalse(cleaned.contains("import java.util.Map;"));
    assertTrue(cleaned.contains("import java.util.Set;"));
    assertFalse(cleaned.contains("import java.util.*;"));
  }

  @Test
  void cleanCodeRemovesAdaptAnnotationsWithSpoon() throws IOException {
    Path output = Path.of("target/codeAdapter/cleanupUnit");
    FileUtils.deleteQuietly(output.toFile());
    Files.createDirectories(output);
    Path source = output.resolve("CleanupSubject.java");
    Files.writeString(
        source,
        """
        import de.monticore.codeAdaption.utils.Adapt;

        @Adapt(ignore = true)
        @demo.local.Adapt(ignore = true)
        public class CleanupSubject {
        }
        """);

    CodeAdapter.cleanCode(output);

    String cleaned = Files.readString(source);
    assertFalse(cleaned.contains("@Adapt"));
    assertFalse(cleaned.contains("de.monticore.codeAdaption.utils.Adapt"));
  }
}
