/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java._symboltable;

import de.monticore.java.javadsl._symboltable.JavaDSLArtifactScope;
import de.monticore.symboltable.ImportStatement;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

public class JavaDSLArtifactScopeTest {

  private static final String PACKAGE = "de.monticore.java._symboltable";

  static Stream<Arguments> calculateQualifiedNames() {
    return Stream.of(
        argumentSet("star import",
            "List", imports("java.util.*"),
            List.of("java.util.List")),
        argumentSet("single type import among others",
            "List", imports("java.util.Iterator", "java.util.List", "java.util.Set"),
            List.of("java.util.List")),
        argumentSet("single type import",
            "Map", imports("java.util.Map"),
            List.of("java.util.Map")),
        argumentSet("inner type via star import",
            "Map.Entry", imports("java.util.*"),
            List.of("java.util.Map.Entry")),
        argumentSet("inner type via import of outer type",
            "Map.Entry", imports("java.util.Map", "java.util.List", "java.util.Set"),
            List.of("java.util.Map.Entry")),
        argumentSet("qualified name matching an imported inner type",
            "Map.Entry", imports("java.util.Map.Entry", "java.util.List", "java.util.Set"),
            List.of()),
        argumentSet("imports sharing a prefix",
            "X.Y.Z", imports("A.B", "A.B.C", "A.B.C.X"),
            List.of("A.B.C.X.Y.Z"))
    );
  }

  /**
   * The calculated names always start with the name itself and the name within the current
   * package, followed by the names derived from the imports.
   */
  @ParameterizedTest
  @MethodSource
  void calculateQualifiedNames(String name, List<ImportStatement> imports,
      List<String> expectedNamesFromImports) {
    JavaDSLArtifactScope scope = new JavaDSLArtifactScope();

    List<String> expected = Stream.concat(
        Stream.of(name, PACKAGE + "." + name), expectedNamesFromImports.stream()).toList();
    assertIterableEquals(expected, scope.calculateQualifiedNames(name, PACKAGE, imports));
  }

  /** Creates import statements; a trailing {@code .*} denotes a star import. */
  private static List<ImportStatement> imports(String... statements) {
    return Stream.of(statements)
        .map(s -> s.endsWith(".*")
            ? new ImportStatement(s.substring(0, s.length() - 2), true)
            : new ImportStatement(s, false))
        .toList();
  }
}
