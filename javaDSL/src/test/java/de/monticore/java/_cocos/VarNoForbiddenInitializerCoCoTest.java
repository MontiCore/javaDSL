/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java._cocos;

import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._cocos.JavaDSLCoCoChecker;
import de.monticore.java.javadsl._cocos.VarNoForbiddenInitializerCoCo;
import de.monticore.java.util.Javac;
import de.monticore.java.util.SymbolTables;
import de.monticore.runtime.junit.MCAssertions;
import de.monticore.runtime.junit.TestWithMCLanguage;
import de.se_rwth.commons.logging.Log;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * The initializer of a 'var' variable must not be an array initializer, a lambda expression, a
 * method reference, or of the null type or void (JLS 14.4.1). The symbol table is built, as the
 * CoCo checks the types inferred in phase 3.
 */
@TestWithMCLanguage(JavaDSLMill.class)
public class VarNoForbiddenInitializerCoCoTest {

  static final String CONTEXT = """
      package a;
      import java.util.*;
      class C {
        void nothing() { }
        void m(String s, int[] ia, List<String> ls) {
          /*STATEMENT*/
        }
      }
      """;

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {
      "var x = 1;",
      "var x = s;",
      "var x = s.length();",
      "var x = new ArrayList<String>();",
      // FAIL: the type check rejects casts of null and of lambdas to functional interfaces
      "var x = (Object) null;",
      "var x = (Runnable) () -> { };",
      // the restrictions only apply to 'var'
      "int[] x = {1, 2};",
      "Runnable x = () -> { };",
      "Object x = null;",
  })
  void valid(String statement) {
    Javac.assertCompiles(model(statement));
    check(statement);

    MCAssertions.assertNoFindings();
  }

  /** The CoCo error is the only finding, i.e., the symbol table construction reports nothing. */
  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {
      "var x = {1, 2};",
      "var x = () -> 1;",
      "var x = (() -> 1);",
      "var x = null;",
      "var x = nothing();",
  })
  void invalid(String statement) {
    Javac.assertDoesNotCompile(model(statement));
    check(statement);

    if (Log.getFindings().stream()
        .anyMatch(f -> !f.getMsg().startsWith(VarNoForbiddenInitializerCoCo.ERROR_CODE))) {
      MCAssertions.failAndPrintFindings(
          "Expected only the error " + VarNoForbiddenInitializerCoCo.ERROR_CODE);
    }
    MCAssertions.assertHasFindingStartingWith(VarNoForbiddenInitializerCoCo.ERROR_CODE);
  }

  /**
   * Reported as well, but phase 3 of the symbol table construction reports further errors when
   * typing the initializer: the type check does not support method references yet, and lambda
   * parameters without explicit type have no type.
   */
  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {
      "var x = s::length;",
      "var x = String::valueOf;",
      "var x = (a, b) -> a;",
  })
  void invalidWithFurtherErrors(String statement) {
    Javac.assertDoesNotCompile(model(statement));
    check(statement);

    MCAssertions.assertHasFindingStartingWith(VarNoForbiddenInitializerCoCo.ERROR_CODE);
    MCAssertions.assertHasFindings(f -> true);
  }

  private static String model(String statement) {
    return CONTEXT.replace("/*STATEMENT*/", statement);
  }

  private static void check(String statement) {
    ASTCompilationUnit ast = (ASTCompilationUnit)
        SymbolTables.fromUncheckedSources(model(statement)).get(0).getAstNode();
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new VarNoForbiddenInitializerCoCo());
    checker.checkAll(ast);
  }
}
