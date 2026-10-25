package de.monticore.codeAdaption;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HelperVariantRegistryTest {
  @TempDir Path tempDir;

  @Test
  void deduplicatesStructurallyIdenticalVariantsAcrossMappingsAndPasses() throws IOException {
    HelperVariantRegistry registry = new HelperVariantRegistry();
    ASTOrdinaryCompilationUnit first =
        parse("first/Support.java", "package shared; class Support { int value() { return 1; } }");
    ASTOrdinaryCompilationUnit identical =
        parse("second/Support.java", "package shared; class Support { int value() { return 1; } }");

    assertTrue(registry.register("shared.Support", first, "observer", "[Subject=Publisher]"));
    assertFalse(
        registry.register("shared.Support", identical, "command", "[Command=ReserveCommand]"));
  }

  @Test
  void ignoresCommentsAndSourcePositionsWhenComparingVariants() throws IOException {
    HelperVariantRegistry registry = new HelperVariantRegistry();
    ASTOrdinaryCompilationUnit first =
        parse(
            "first/Support.java",
            "package shared; // first origin\n"
                + "class Support { int value() { return 1; } }");
    ASTOrdinaryCompilationUnit relocated =
        parse(
            "deeply/nested/second/Support.java",
            "package shared;\n"
                + "/* a different comment at another source position */\n"
                + "class Support {\n int value() {\n return 1;\n }\n }");

    assertTrue(registry.register("shared.Support", first, "observer", "[Subject=Publisher]"));
    assertFalse(
        registry.register("shared.Support", relocated, "observer", "[Subject=AuditPublisher]"));
  }

  @Test
  void rejectsDivergentBodiesAndReportsBothOrigins() throws IOException {
    HelperVariantRegistry registry = new HelperVariantRegistry();
    ASTOrdinaryCompilationUnit first =
        parse("first/Support.java", "package shared; class Support { int value() { return 1; } }");
    ASTOrdinaryCompilationUnit divergent =
        parse("second/Support.java", "package shared; class Support { int value() { return 2; } }");

    assertTrue(registry.register("shared.Support", first, "observer", "[Subject=Publisher]"));
    CodeAdaptationException exception =
        assertThrows(
            CodeAdaptationException.class,
            () ->
                registry.register(
                    "shared.Support", divergent, "command", "[Command=ReserveCommand]"));

    assertTrue(exception.getMessage().contains("shared.Support"));
    assertTrue(exception.getMessage().contains("mapping 'observer'"));
    assertTrue(exception.getMessage().contains("[Subject=Publisher]"));
    assertTrue(exception.getMessage().contains("mapping 'command'"));
    assertTrue(exception.getMessage().contains("[Command=ReserveCommand]"));
  }

  @Test
  void storesACloneInsteadOfTheMutableInput() throws IOException {
    HelperVariantRegistry registry = new HelperVariantRegistry();
    ASTOrdinaryCompilationUnit first =
        parse("first/Support.java", "package shared; class Support { int value() { return 1; } }");
    ASTOrdinaryCompilationUnit originalShape = first.deepClone();

    assertTrue(registry.register("shared.Support", first, "observer", "[Subject=Publisher]"));
    first.getTypeDeclarationList().clear();

    assertFalse(
        registry.register(
            "shared.Support", originalShape, "observer", "[Subject=SecondPublisher]"));
  }

  private ASTOrdinaryCompilationUnit parse(String relativePath, String source) throws IOException {
    Path file = tempDir.resolve(relativePath);
    Files.createDirectories(file.getParent());
    Files.writeString(file, source);
    return JavaLoader.loadJava(file.toFile());
  }
}
