package de.monticore.codeAdaption;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

class CodeAdapterCleanupTest {

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

    new OutputCodeService().cleanCode(output);

    String cleaned = Files.readString(source);
    assertFalse(cleaned.contains("@Adapt"));
    assertFalse(cleaned.contains("de.monticore.codeAdaption.utils.Adapt"));
  }
}
