/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.types3;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.java.javadsl._ast.ASTLocalVariableDeclaration;
import de.monticore.java.util.SymbolTables;
import de.monticore.java.util.Symbols;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.statements.mccommonstatements._ast.ASTExpressionStatement;
import de.monticore.statements.mcstatementsbasis._ast.ASTMCBlockStatement;
import de.monticore.statements.mcvardeclarationstatements._ast.ASTLocalVariableDeclarationStatement;
import de.monticore.statements.mcvardeclarationstatements._ast.ASTSimpleInit;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types3.TypeCheck3;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Derives the type of an expression within the {@link #CONTEXT} model. The expression is placed
 * as the initializer of a local variable {@code __probe} at the end of the method
 * {@code Context.probe}, so it can use the members of {@code Context} and the method parameters.
 *
 * <p>The members of the other classes are public, as the JavaDSL does not yet support the access
 * of package private members, see {@link AccessTest}. Likewise, all classes used in
 * {@code new} expressions declare a constructor (except for {@code Base} and {@code Context},
 * which test the default constructor).</p>
 *
 * <p>The declared type of {@code __probe} is the expected type of the expression. It is not
 * passed to the type check (unless requested), but it allows javac to verify that the expression
 * is valid and assignable to the expected type, see {@link de.monticore.java.util.Javac}.</p>
 */
final class ExpressionProbe {

  static final String PLACEHOLDER = "/*PROBE*/";

  /** The context of the probed expressions; must be compilable by javac. */
  static final String CONTEXT = """
      package p;

      import java.util.*;
      import java.util.function.*;

      class Base {
        public int baseField;
        public static String staticBaseField = "";
        public int baseMethod() { return 0; }
        public static long staticBaseMethod() { return 0L; }
        public Base self() { return this; }
      }

      interface Shape {
        public double area();
        public default String describe() { return "shape"; }
        public static Shape unit() { return null; }
      }

      enum Color {
        RED, GREEN, BLUE;
        public Color next() { return values()[(ordinal() + 1) % values().length]; }
      }

      class Box<T> {
        public T value;
        public Box(T value) { this.value = value; }
        public T get() { return value; }
        public <R> Box<R> map(Function<? super T, ? extends R> mapper) { return new Box<>(mapper.apply(value)); }
        public static <E> Box<E> of(E element) { return new Box<>(element); }
      }

      class Secret {
        private int hidden;
        private static int staticHidden;
        protected int prot;
        private Secret() { }
        private void hide() { }
        public static Secret create() { return new Secret(); }
      }

      class Context extends Base implements Shape {
        byte b; short sh; char c; int i; long l; float f; double d; boolean bool;
        Integer boxedInt; Long boxedLong; Double boxedDouble; Boolean boxedBool; Character boxedChar;
        String s; Object o; Number n;
        int[] ia; int[][] iaa; String[] sa;
        List<String> ls; ArrayList<Integer> ali; Map<String, Integer> msi; Set<? extends Number> sn;
        Box<String> bs; Box<Box<Integer>> bbi;
        Color color; Shape shape; Runnable runnable; Function<String, Integer> fsi;
        static int staticField;
        static final String CONSTANT = "constant";

        public double area() { return 0; }
        void voidMethod() { }
        int intMethod() { return 0; }
        String stringMethod(int x) { return ""; }
        int overloaded(int x) { return x; }
        long overloaded(long x) { return x; }
        double overloaded(double x) { return x; }
        String overloaded(String x) { return x; }
        Object overloaded(Object x) { return x; }
        int varargs(String... xs) { return xs.length; }
        String varargs(int first, Object... rest) { return ""; }
        static <T> T id(T t) { return t; }
        @SafeVarargs static <T> List<T> listOf(T... ts) { return Arrays.asList(ts); }
        static <T extends Comparable<T>> T max(T a, T b) { return a.compareTo(b) >= 0 ? a : b; }

        class Inner {
          public int innerField;
          public Inner() { }
          public int innerMethod() { return innerField; }
        }

        static class Nested {
          public static int nestedStatic;
          public String nestedField;
          public Nested() { }
        }

        void probe(int param, String... params) {
          /*PROBE*/
        }
      }
      """;

  private ExpressionProbe() {
  }

  /**
   * The context model with the probe {@code declaredType __probe = expression;}. Use
   * {@code "void"} for expressions without a value (the expression becomes an expression
   * statement) and {@code "null"} for the null literal (declared as {@code Object}).
   */
  static String model(String expression, String declaredType) {
    String probe = switch (declaredType) {
      case "void" -> expression + ";";
      case "null" -> "Object __probe = " + expression + ";";
      default -> declaredType + " __probe = " + expression + ";";
    };
    return CONTEXT.replace(PLACEHOLDER, probe);
  }

  /** Builds the symbol table of the model and returns the probed expression. */
  static ASTExpression probedExpression(String model) {
    SymbolTables.fromUncheckedSources(model);
    return probedExpressionIn("p.Context.probe");
  }

  /**
   * The expression of the last statement of the method in the current symbol table, which is
   * either an expression statement or a local variable declaration with initializer.
   */
  static ASTExpression probedExpressionIn(String method) {
    ASTMethodDeclaration probe = assertInstanceOf(ASTMethodDeclaration.class,
        Symbols.method(method).getAstNode());
    List<ASTMCBlockStatement> statements = probe.getMCJavaBlock().getMCBlockStatementList();
    ASTMCBlockStatement last = statements.get(statements.size() - 1);
    if (last instanceof ASTExpressionStatement expressionStatement) {
      return expressionStatement.getExpression();
    }
    if (last instanceof ASTLocalVariableDeclarationStatement declarationStatement) {
      ASTLocalVariableDeclaration declaration = (ASTLocalVariableDeclaration)
          declarationStatement.getLocalVariableDeclaration();
      return assertInstanceOf(ASTSimpleInit.class,
          declaration.getVariableDeclarator(0).getVariableInit()).getExpression();
    }
    return fail("Unexpected probe statement " + last.getClass().getSimpleName());
  }

  /** Derives the type of the expression without a target type. */
  static SymTypeExpression typeOf(String expression, String declaredType) {
    return TypeCheck3.typeOf(probedExpression(model(expression, declaredType)));
  }

  /** Derives the type of the expression with the declared type of the probe as target type. */
  static SymTypeExpression typeOfWithTarget(String expression, String declaredType) {
    ASTExpression probed = probedExpression(model(expression, declaredType));
    return TypeCheck3.typeOf(probed, declaredType());
  }

  /** The type of the variable {@code __probe} in the current symbol table. */
  static SymTypeExpression declaredType() {
    return Symbols.localVariable(Symbols.method("p.Context.probe"), "__probe").getType();
  }
}
