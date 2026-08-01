package de.monticore.codeAdaption.dependency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.monticore.codeAdaption.AdapterAbstractTest;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReferenceCodeDependencySelectorTest extends AdapterAbstractTest {
  @TempDir Path tempDir;

  @BeforeEach
  void setUp() {
    initMills();
  }

  @Test
  void selectsTransitiveSourceLocalHelpersAndReferenceCDUses() throws IOException {
    write(
        "p/Service.java",
        """
        package p;
        class Service {
          Entity entity;
          ServiceSupport support = new ServiceSupport();
          Class<?> supportType = ServiceSupport.class;
        }
        """);
    write(
        "p/ServiceSupport.java",
        """
        package p;
        import q.SupportValue;
        class ServiceSupport {
          SupportValue create() { return new SupportValue(); }
        }
        """);
    write(
        "q/SupportValue.java",
        """
        package q;
        public class SupportValue {
          Entity convert(Entity value) { return value; }
        }
        """);
    write("p/Unrelated.java", "package p; class Unrelated {}");

    Set<ASTOrdinaryCompilationUnit> units = JavaLoader.readJavaCode(tempDir);
    ReferenceCodeSelection selection =
        new ReferenceCodeDependencySelector(tempDir, units, Set.of("Entity"))
            .select(Set.of("p.Service"));

    assertEquals(Set.of("p.Service"), selection.rootTypeIdentities());
    assertEquals(
        Set.of("p.ServiceSupport", "q.SupportValue"), selection.helperTypeIdentities());
    assertEquals(Set.of("p.ServiceSupport"), selection.dependencyEdges().get("p.Service"));
    assertEquals(Set.of("q.SupportValue"), selection.dependencyEdges().get("p.ServiceSupport"));
    assertEquals(Set.of("Entity"), selection.referencedCDTypeKeys());
    assertTrue(selection.diagnostics().isEmpty());
  }

  @Test
  void allowsPackagePrivateHelperFromSamePackage() throws IOException {
    write(
        "same/Root.java",
        """
        package same;
        class Root { PackagePrivateHelper helper; }
        """);
    write(
        "same/PackagePrivateHelper.java",
        "package same; class PackagePrivateHelper {}");

    Set<ASTOrdinaryCompilationUnit> units = JavaLoader.readJavaCode(tempDir);
    ReferenceCodeSelection selection =
        new ReferenceCodeDependencySelector(tempDir, units, Set.of())
            .select(Set.of("same.Root"));

    assertEquals(Set.of("same.PackagePrivateHelper"), selection.helperTypeIdentities());
  }

  @Test
  void ignoresCoincidentalSourceTypeWhenJavaLangTypeIsUsed() throws IOException {
    write("root/Root.java", "package root; class Root { String value; }");
    write("unrelated/String.java", "package unrelated; public class String {}");

    Set<ASTOrdinaryCompilationUnit> units = JavaLoader.readJavaCode(tempDir);
    ReferenceCodeSelection selection =
        new ReferenceCodeDependencySelector(tempDir, units, Set.of())
            .select(Set.of("root.Root"));

    assertTrue(selection.helperTypeIdentities().isEmpty());
  }

  @Test
  void ignoresCoincidentalSourceTypeWhenExternalTypeIsExplicitlyImported()
      throws IOException {
    write(
        "root/Root.java",
        """
        package root;
        import java.time.Clock;
        class Root { Clock clock; }
        """);
    write("unrelated/Clock.java", "package unrelated; public class Clock {}");

    Set<ASTOrdinaryCompilationUnit> units = JavaLoader.readJavaCode(tempDir);
    ReferenceCodeSelection selection =
        new ReferenceCodeDependencySelector(tempDir, units, Set.of())
            .select(Set.of("root.Root"));

    assertTrue(selection.helperTypeIdentities().isEmpty());
  }

  private void write(String relativePath, String source) throws IOException {
    Path target = tempDir.resolve(relativePath);
    Files.createDirectories(target.getParent());
    Files.writeString(target, source);
  }
}
