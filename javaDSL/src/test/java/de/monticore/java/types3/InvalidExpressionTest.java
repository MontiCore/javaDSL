/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.types3;

import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.util.Javac;
import de.monticore.runtime.junit.MCAssertions;
import de.monticore.runtime.junit.TestWithMCLanguage;
import de.monticore.types.check.SymTypeExpression;
import de.se_rwth.commons.logging.Log;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Ill-typed expressions within {@link ExpressionProbe#CONTEXT}. The type check must report an
 * error and derive the obscure type. Each case is first verified to be rejected by javac.
 */
@TestWithMCLanguage(JavaDSLMill.class)
public class InvalidExpressionTest {

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {
      "true + 1",
      "bool - 1",
      "s - 1",
      "s * 2",
      "o + 1",
      "ia + 1",
      "color + 1",
      "bool * bool",
      "-bool",
      "-s",
      "!i",
      "!s",
      "~d",
      "~bool",
      "s++",
      "bool++",
      "--o",
  })
  void arithmetic(String expression) {
    check(expression);
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {
      "i && bool",
      "bool || 1",
      "s < s",
      "bool < true",
      "o > 1",
      "i == s",
      "bool == 1",
      "color == s",
      "i << d",
      "d >> 1",
      "d & 1",
      "s | 1",
      "bool ^ 1",
      "i ? 1 : 2",
      "s ? 1 : 2",
  })
  void comparisonsAndBitOperations(String expression) {
    check(expression);
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {
      "i = \"a\"",
      "s = 1",
      "i = 1L",
      "b = i",
      "c = s",
      "bool = 1",
      "color = 1",
      "boxedInt = 1L",
      "boxedLong = 1",
      "ia = sa",
      "ls = ali",
      "i += \"a\"",
      "i += bool",
      "bool += 1",
      "1 = i",
      "i + 1 = 2",
      "intMethod() = 1",
  })
  void assignments(String expression) {
    check(expression);
  }

  /** Final variables must not be assigned. */
  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {
      "CONSTANT = \"x\"",
      "CONSTANT += \"x\"",
      "ia.length = 1",
  })
  void assignmentsToFinalVariables(String expression) {
    check(expression);
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {
      "(String) i",
      "(int) s",
      "(Integer) s",
      "(Integer) l",
      "(Long) i",
      "(boolean) i",
      "(int) bool",
      "(Color) s",
      "(int[]) s",
      "(String[]) ia",
      "i instanceof Integer",
      "s instanceof Integer",
      "color instanceof String",
  })
  void castsAndInstanceOf(String expression) {
    check(expression);
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {
      "unknown",
      "p.Unknown.x",
      "i.length",
      "s.length",
      "ia.size",
      "bs.unknown",
      "Color.PURPLE",
      "Context.i",
      "Base.baseField",
      "Context.Nested.nestedField",
  })
  void namesAndFieldAccess(String expression) {
    check(expression);
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {
      "Secret.create().hidden",
      "Secret.staticHidden",
      "Secret.create().hide()",
      "new Secret()",
  })
  void privateMembers(String expression) {
    check(expression);
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {
      "unknownMethod()",
      "intMethod(1)",
      "stringMethod()",
      "stringMethod(\"a\")",
      "stringMethod(1L)",
      "stringMethod(1, 2)",
      "voidMethod() + 1",
      "overloaded()",
      "varargs(true)",
      "varargs(\"a\", 1)",
      "Context.intMethod()",
      "Shape.area()",
      "super.area()",
      "s.charAt(\"a\")",
      "ls.get(\"a\")",
      "ls.add(1)",
      "ali.add(\"a\")",
      "id()",
      "id(1, 2)",
      "max(1, \"a\")",
      "max(o, o)",
      "bs.map(1)",
  })
  void methodCalls(String expression) {
    check(expression);
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {
      "new Unknown()",
      "new Shape()",
      "new Color()",
      "new Context(1)",
      "new Box<>()",
      "new Box<String>(1)",
      "new int[\"a\"]",
      "new int[1.5]",
      "new int[] {\"a\"}",
      "new String[] {1}",
  })
  void instantiation(String expression) {
    check(expression);
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {
      "ia[\"0\"]",
      "ia[1.5]",
      "ia[l]",
      "ia[0][0]",
      "i[0]",
      "s[0]",
  })
  void arrays(String expression) {
    check(expression);
  }

  private static void check(String expression) {
    Javac.assertDoesNotCompile(ExpressionProbe.model(expression, "Object"));
    SymTypeExpression type = ExpressionProbe.typeOf(expression, "Object");
    assertTypeError(expression, type);
  }

  /**
   * Asserts that a type error has been reported and that the type is obscure. Internal errors
   * (e.g., of a misconfigured type check) do not count as type error. All findings are marked as
   * checked, as further findings are expected as consequence of the type error.
   */
  static void assertTypeError(String expression, SymTypeExpression type) {
    boolean hasTypeError = Log.getFindings().stream()
        .anyMatch(f -> f.isError() && !f.getMsg().contains("internal error"));
    if (!hasTypeError) {
      MCAssertions.failAndPrintFindings("Expected a type error for " + expression
          + " (derived type " + type.printFullName() + ")");
    }
    MCAssertions.assertHasFindings(f -> true);
    assertTrue(type.isObscureType(),
        () -> "Expected the obscure type, but was " + type.printFullName());
  }
}
