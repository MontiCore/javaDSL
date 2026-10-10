/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.utils;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.lambdaexpressions._ast.ASTLambdaExpression;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTLambdaBlockBody;
import de.monticore.java.util.SymbolTables;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.runtime.junit.TestWithMCLanguage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static de.monticore.java.util.Symbols.method;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * The collected return statements belong to the given body only; the return statements of nested
 * lambdas, methods, and types belong to them.
 */
@TestWithMCLanguage(JavaDSLMill.class)
public class ReturnStatementCollectorTest {

  @BeforeEach
  void buildModel() {
    SymbolTables.fromSource("""
        package a;
        import java.util.function.*;
        class C {
          int m(int x) {
            if (x > 0) { return 1; }
            for (int i = 0; i < x; i++) { if (i == 2) { return 2; } }
            try { if (x == 3) return 3; } finally { }
            switch (x) { case 4: return 4; default: break; }
            IntSupplier lambda = () -> { return 91; };
            IntUnaryOperator nested = y -> { IntSupplier inner = () -> { return 92; }; return 93; };
            Object anonymous = new Object() { public int hashCode() { return 94; } };
            class Local { int n() { return 95; } }
            record LocalRecord(int v) { public int v() { return 96; } }
            enum LocalEnum { A; int n() { return 97; } }
            interface LocalInterface { default int n() { return 98; } }
            return 5;
          }
          class Inner { }
          void innerCreator(C c) {
            Object o = c.new Inner() { int k() { return 99; } };
            return;
          }
          IntUnaryOperator lambda() {
            return y -> { IntSupplier inner = () -> { return 92; }; return 93; };
          }
        }
        """);
  }

  @Test
  void returnStatementsOfMethod() {
    assertEquals(List.of("1", "2", "3", "4", "5"), returnedExpressions("a.C.m"));
  }

  @Test
  void returnStatementsOfMethodWithInnerCreator() {
    assertEquals(List.of("<none>"), returnedExpressions("a.C.innerCreator"));
  }

  @Test
  void returnStatementsOfLambda() {
    ASTExpression returned = ReturnStatementCollector.getReturnExpressions(body("a.C.lambda"))
        .values().iterator().next().orElseThrow();
    ASTLambdaBlockBody lambdaBody = assertInstanceOf(ASTLambdaBlockBody.class,
        assertInstanceOf(ASTLambdaExpression.class, returned).getLambdaBody());

    assertEquals(List.of("93"), print(ReturnStatementCollector
        .getReturnExpressions(lambdaBody.getMCJavaBlock()).values().stream().toList()));
  }

  private static List<String> returnedExpressions(String method) {
    return print(ReturnStatementCollector.getReturnExpressions(body(method)).values().stream()
        .toList());
  }

  private static de.monticore.ast.ASTNode body(String method) {
    return assertInstanceOf(ASTMethodDeclaration.class, method(method).getAstNode())
        .getMCJavaBlock();
  }

  private static List<String> print(List<Optional<ASTExpression>> expressions) {
    return expressions.stream()
        .map(e -> e.map(expr -> JavaDSLMill.prettyPrint(expr, false).trim()).orElse("<none>"))
        .toList();
  }
}
