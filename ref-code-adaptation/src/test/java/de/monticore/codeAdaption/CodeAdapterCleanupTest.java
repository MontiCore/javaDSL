package de.monticore.codeAdaption;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CodeAdapterCleanupTest {

  @TempDir Path output;

  @Test
  void cleanCodePreservesPackagePathsAndRemovesOnlyToolAdaptAnnotations() throws IOException {
    Path first = output.resolve("a/User.java");
    Path second = output.resolve("b/User.java");
    Files.createDirectories(first.getParent());
    Files.createDirectories(second.getParent());
    Files.writeString(
        first,
        """
        package a;
        import de.monticore.codeAdaption.utils.Adapt;

        @Adapt(ignore = true)
        @demo.local.Adapt(ignore = true)
        public class User {}
        """);
    Files.writeString(
        second,
        """
        package b;
        import de.monticore.codeAdaption.utils.Adapt;

        @Adapt(ignore = true)
        public class User {}
        """);

    new OutputCodeService().cleanCode(output);

    assertTrue(Files.exists(first));
    assertTrue(Files.exists(second));
    assertFalse(Files.exists(output.resolve("User.java")));
    String firstCleaned = Files.readString(first);
    String secondCleaned = Files.readString(second);
    assertTrue(firstCleaned.contains("@demo.local.Adapt"), firstCleaned);
    assertFalse(firstCleaned.contains("import de.monticore.codeAdaption.utils.Adapt"));
    assertFalse(secondCleaned.contains("import de.monticore.codeAdaption.utils.Adapt"));
    assertFalse(secondCleaned.contains("@Adapt"));
  }
}
