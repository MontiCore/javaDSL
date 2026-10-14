/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java._cocos;

import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._cocos.JavaDSLCoCoChecker;
import de.monticore.java.javadsl._cocos.VarResourceNoForbiddenInitializerCoCo;
import de.monticore.java.util.Javac;
import de.monticore.java.util.SymbolTables;
import de.monticore.runtime.junit.MCAssertions;
import de.monticore.runtime.junit.TestWithMCLanguage;
import de.se_rwth.commons.logging.Log;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * The initializer of a 'var' resource must not be a lambda expression, a method reference, or of
 * the null type or void (JLS 14.20.3, 14.4.1). The symbol table is built, as the CoCo checks the
 * types inferred in phase 3.
 */
@TestWithMCLanguage(JavaDSLMill.class)
public class VarResourceNoForbiddenInitializerCoCoTest {

  static final String CONTEXT = """
      package a;
      import java.io.*;
      class C {
        void nothing() { }
        void m(String s) throws Exception {
          /*STATEMENT*/
        }
      }
      """;

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {
      "try (var r = new StringReader(s)) { }",
      "try (var r = new StringReader(s); var w = new StringWriter()) { }",
      // the restrictions only apply to 'var'
      "try (AutoCloseable r = null) { }",
      "try (AutoCloseable r = () -> { }) { }",
  })
  void valid(String statement) {
    Javac.assertCompiles(model(statement));
    check(statement);

    MCAssertions.assertNoFindings();
  }

  /** The CoCo error is the only finding, i.e., the symbol table construction reports nothing. */
  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {
      "try (var r = () -> { }) { }",
      "try (var r = (() -> { })) { }",
      "try (var r = null) { }",
      "try (var r = nothing()) { }",
      "try (var r = new StringReader(s); var w = null) { }",
  })
  void invalid(String statement) {
    Javac.assertDoesNotCompile(model(statement));
    check(statement);

    if (Log.getFindings().stream()
        .anyMatch(f -> !f.getMsg().startsWith(VarResourceNoForbiddenInitializerCoCo.ERROR_CODE))) {
      MCAssertions.failAndPrintFindings(
          "Expected only the error " + VarResourceNoForbiddenInitializerCoCo.ERROR_CODE);
    }
    MCAssertions.assertHasFindingStartingWith(VarResourceNoForbiddenInitializerCoCo.ERROR_CODE);
  }

  /**
   * Reported as well, but phase 3 of the symbol table construction reports further errors when
   * typing the initializer, as the type check does not support method references yet.
   */
  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {
      "try (var r = s::length) { }",
  })
  void invalidWithFurtherErrors(String statement) {
    Javac.assertDoesNotCompile(model(statement));
    check(statement);

    MCAssertions.assertHasFindingStartingWith(VarResourceNoForbiddenInitializerCoCo.ERROR_CODE);
    MCAssertions.assertHasFindings(f -> true);
  }

  private static String model(String statement) {
    return CONTEXT.replace("/*STATEMENT*/", statement);
  }

  private static void check(String statement) {
    ASTCompilationUnit ast = (ASTCompilationUnit)
        SymbolTables.fromUncheckedSources(model(statement)).get(0).getAstNode();
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new VarResourceNoForbiddenInitializerCoCo());
    checker.checkAll(ast);
  }
}
