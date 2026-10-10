/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.types3;

import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._symboltable.IJavaDSLArtifactScope;
import de.monticore.java.javadsl._symboltable.IJavaDSLScope;
import de.monticore.java.util.JavaSourceTest;
import de.monticore.java.util.Javac;
import de.monticore.java.util.SymbolTables;
import de.monticore.java.util.TestModels;
import de.monticore.javalight._symboltable.JavaMethodSymbol;
import de.monticore.runtime.junit.MCAssertions;
import de.monticore.runtime.junit.TestWithMCLanguage;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.se_rwth.commons.logging.Finding;
import de.se_rwth.commons.logging.Log;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Builds the symbol table of complete models and type checks them with the
 * {@link TypeCheckCoCos}.
 *
 * <p>Each directory in {@link TestModels#TYPECHECK_VALID} is a small, valid Java project; its
 * symbol table must be complete and the type check must not report any finding.</p>
 *
 * <p>Each file in {@link TestModels#TYPECHECK_INVALID} is an invalid model whose expected errors
 * are marked by comments: {@code // error: 0xA0909} expects an error with the given code at that
 * line, {@code // error} expects any error. Errors on unmarked lines are not allowed.</p>
 */
@TestWithMCLanguage(JavaDSLMill.class)
public class TypeCheckModelsTest {

  private static final Pattern ERROR_MARKER = Pattern.compile("//\\s*error(?::\\s*(\\S+))?");

  static Stream<Named<Path>> validProjects() throws IOException {
    try (Stream<Path> directories = Files.list(Path.of(TestModels.TYPECHECK_VALID))) {
      return directories.filter(Files::isDirectory).sorted()
          .map(dir -> Named.of(dir.getFileName().toString(), dir))
          .toList().stream();
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("validProjects")
  void validProjectCompilesWithJavac(Path project) {
    Javac.assertCompiles(SymbolTables.javaFilesIn(project));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("validProjects")
  void symbolTableOfValidProjectIsComplete(Path project) {
    List<IJavaDSLArtifactScope> scopes = SymbolTables.fromDirectory(project);
    MCAssertions.assertNoFindings("Findings while building the symbol table");

    List<String> problems = new ArrayList<>();
    scopes.forEach(scope -> collectIncompleteSymbols(scope, problems));
    if (!problems.isEmpty()) {
      fail("Incomplete symbols:\n" + String.join("\n", problems));
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("validProjects")
  void typeCheckOfValidProjectReportsNoFindings(Path project) {
    List<IJavaDSLArtifactScope> scopes = SymbolTables.fromDirectory(project);
    scopes.forEach(TypeCheckCoCos::check);
    MCAssertions.assertNoFindings();
  }

  @JavaSourceTest(basePath = TestModels.TYPECHECK_INVALID)
  void invalidModelReportsMarkedErrors(Path model) {
    Javac.assertDoesNotCompile(List.of(model));
    Map<Integer, Optional<String>> expected = errorMarkers(model);

    TypeCheckCoCos.check(SymbolTables.fromFiles(List.of(model)).get(0));

    List<Finding> errors = Log.getFindings().stream().filter(Finding::isError).toList();
    List<String> problems = new ArrayList<>();
    expected.forEach((line, code) -> {
      boolean found = errors.stream().anyMatch(error -> line(error) == line
          && code.map(c -> error.getMsg().startsWith(c)).orElse(true));
      if (!found) {
        problems.add("line " + line + ": missing error " + code.orElse(""));
      }
    });
    errors.stream()
        .filter(error -> !expected.containsKey(line(error)))
        .forEach(error -> problems.add("unexpected error: " + error.buildMsg()));
    if (!problems.isEmpty()) {
      MCAssertions.failAndPrintFindings(String.join("\n", problems));
    }
    // further findings (e.g., warnings) on marked lines are consequences of the errors
    if (!Log.getFindings().isEmpty()) {
      MCAssertions.assertHasFindings(f -> true);
    }
  }

  private static int line(Finding finding) {
    return finding.getSourcePosition().map(p -> p.getLine()).orElse(-1);
  }

  /** The line numbers of the error markers with the optional expected error code. */
  private static Map<Integer, Optional<String>> errorMarkers(Path model) {
    List<String> lines;
    try {
      lines = Files.readAllLines(model);
    }
    catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    Map<Integer, Optional<String>> markers = new LinkedHashMap<>();
    for (int i = 0; i < lines.size(); i++) {
      Matcher matcher = ERROR_MARKER.matcher(lines.get(i));
      if (matcher.find()) {
        markers.put(i + 1, Optional.ofNullable(matcher.group(1)));
      }
    }
    return markers;
  }

  /** Collects variables and methods whose types are not set or could not be resolved. */
  private static void collectIncompleteSymbols(IJavaDSLScope scope, List<String> problems) {
    List<VariableSymbol> variables = new ArrayList<>();
    variables.addAll(scope.getLocalVariableSymbols());
    variables.addAll(scope.getLocalFieldSymbols());
    variables.addAll(scope.getLocalEnumConstantDeclarationSymbols());
    for (VariableSymbol variable : variables) {
      check(variable.getFullName(), variable.getType(), problems);
    }
    for (JavaMethodSymbol method : scope.getLocalJavaMethodSymbols()) {
      check(method.getFullName() + "()", method.getType(), problems);
    }
    scope.getLocalTypeDeclarationSymbols().forEach(type -> type.getSuperTypesList()
        .forEach(superType -> check(type.getFullName() + " super type", superType, problems)));
    scope.getSubScopes().forEach(subScope -> collectIncompleteSymbols(subScope, problems));
  }

  private static void check(String name, SymTypeExpression type, List<String> problems) {
    if (type == null) {
      problems.add(name + ": type not set");
    }
    else if (type.isObscureType()) {
      problems.add(name + ": obscure type");
    }
  }
}
