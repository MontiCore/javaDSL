package de.monticore.codeAdaption.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.codeAdaption.AdapterAbstractTest;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CDImportProjectorTest extends AdapterAbstractTest {

  @TempDir Path tempDir;

  @BeforeEach
  void setUp() {
    initMills();
  }

  @Test
  void projectsOnlyExplicitImportsUsedByTheJavaUnit() throws IOException {
    ASTCDCompilationUnit reference =
        cd(
            "Reference",
            """
            import alpha.types.Widget;
            import alpha.time.Date;
            """);
    ASTCDCompilationUnit concrete =
        cd(
            "Concrete",
            """
            import beta.time.Date;
            import beta.types.Unused;
            """);
    ASTOrdinaryCompilationUnit unit = java("Holder", "class Holder { Widget value; }");

    CDImportProjector.project(Set.of(unit), reference, concrete);

    assertEquals(List.of("alpha.types.Widget"), imports(unit));
  }

  @Test
  void preservesAnExistingJavaBindingOnCdCollision() throws IOException {
    ASTCDCompilationUnit reference = cd("Reference", "import org.joda.time.Date;");
    ASTOrdinaryCompilationUnit unit =
        java(
            "Holder",
            """
            import java.util.Date;
            class Holder { Date value; }
            """);

    CDImportProjector.project(Set.of(unit), reference);

    assertEquals(List.of("java.util.Date"), imports(unit));
  }

  @Test
  void rejectsUsedReferenceConcreteImportCollisionDeterministically() throws IOException {
    ASTCDCompilationUnit reference = cd("Reference", "import z.reference.Date;");
    ASTCDCompilationUnit concrete = cd("Concrete", "import a.concrete.Date;");
    ASTOrdinaryCompilationUnit unit = java("Holder", "class Holder { Date value; }");

    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> CDImportProjector.project(Set.of(unit), reference, concrete));

    assertTrue(exception.getMessage().contains("a.concrete.Date, z.reference.Date"));
    assertEquals(List.of(), imports(unit));
  }

  @Test
  void deduplicatesTheSameExplicitImportAcrossJavaAndCds() throws IOException {
    ASTCDCompilationUnit reference = cd("Reference", "import java.util.List;");
    ASTCDCompilationUnit concrete = cd("Concrete", "import java.util.List;");
    ASTOrdinaryCompilationUnit unit =
        java(
            "Holder",
            """
            import java.util.List;
            class Holder { List<String> values; }
            """);

    CDImportProjector.project(Set.of(unit), reference, concrete);

    assertEquals(List.of("java.util.List"), imports(unit));
  }

  @Test
  void preservesAndDeduplicatesCdWildcardImports() throws IOException {
    ASTCDCompilationUnit reference = cd("Reference", "import java.util.*;");
    ASTCDCompilationUnit concrete = cd("Concrete", "import java.util.*;");
    ASTOrdinaryCompilationUnit unit = java("Holder", "class Holder {}");

    CDImportProjector.project(Set.of(unit), reference, concrete);

    assertEquals(List.of("java.util.*"), imports(unit));
    assertTrue(unit.getImportDeclarationList().get(0).isSTAR());
  }

  @Test
  void projectsJavaLangSubpackageAndNestedTypeImports() throws IOException {
    ASTCDCompilationUnit cd =
        cd(
            "JavaLangImports",
            """
            import java.lang.reflect.Method;
            import java.lang.Thread.State;
            """);
    ASTOrdinaryCompilationUnit unit =
        java(
            "Holder",
            """
            class Holder {
              Method method;
              State state;
            }
            """);

    CDImportProjector.project(Set.of(unit), cd);

    assertEquals(List.of("java.lang.reflect.Method", "java.lang.Thread.State"), imports(unit));
  }

  @Test
  void ignoresTypeNamesMentionedOnlyInStringsAndComments() throws IOException {
    ASTCDCompilationUnit reference = cd("ReferenceImports", "import alpha.Customer;");
    ASTCDCompilationUnit concrete = cd("ConcreteImports", "import beta.Customer;");
    ASTOrdinaryCompilationUnit unit =
        java(
            "Message",
            """
            class Message {
              // Customer is only documentation, not a type use.
              String text = "Customer";
            }
            """);

    CDImportProjector.project(Set.of(unit), reference, concrete);

    assertEquals(List.of(), imports(unit));
  }

  @Test
  void ignoresNonTypeIdentifiersThatMatchImportedTypeNames() throws IOException {
    ASTCDCompilationUnit reference =
        cd("ReferenceIdentifierImports", "import alpha.Customer;");
    ASTCDCompilationUnit concrete =
        cd("ConcreteIdentifierImports", "import beta.Customer;");
    ASTOrdinaryCompilationUnit unit =
        java(
            "Identifier",
            """
            class Identifier {
              int Customer;
              void Customer() {}
            }
            """);

    CDImportProjector.project(Set.of(unit), reference, concrete);

    assertEquals(List.of(), imports(unit));
  }

  private ASTCDCompilationUnit cd(String name, String imports) throws IOException {
    Path file = tempDir.resolve(name + ".cd");
    Files.writeString(file, imports + System.lineSeparator() + "classdiagram " + name + " {}");
    return JavaLoader.parseCD(file.toString());
  }

  private ASTOrdinaryCompilationUnit java(String name, String source) throws IOException {
    Path file = tempDir.resolve(name + ".java");
    Files.writeString(file, source);
    return JavaLoader.loadJava(file.toFile());
  }

  private static List<String> imports(ASTOrdinaryCompilationUnit unit) {
    return unit.getImportDeclarationList().stream()
        .map(
            importDeclaration ->
                importDeclaration.getMCQualifiedName().getQName()
                    + (importDeclaration.isSTAR() ? ".*" : ""))
        .toList();
  }
}
