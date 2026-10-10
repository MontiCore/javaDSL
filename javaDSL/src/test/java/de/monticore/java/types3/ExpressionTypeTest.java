/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.types3;

import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.util.Javac;
import de.monticore.java.util.SymbolTables;
import de.monticore.runtime.junit.MCAssertions;
import de.monticore.runtime.junit.TestWithMCLanguage;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types3.SymTypeRelations;
import de.monticore.types3.TypeCheck3;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static de.monticore.java.JavaDSLAssertions.assertType;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Types of valid expressions without target type. The expressions are evaluated within
 * {@link ExpressionProbe#CONTEXT}; the expected types are printed with fully qualified names.
 * Each case is first verified with javac, i.e., a javac failure indicates a wrong test case and
 * not a problem of the JavaDSL.
 *
 * <p>The CSV sources use {@code =>} as delimiter and {@code `} as quote character, since the
 * expressions contain commas, bars, and character literals.</p>
 */
@TestWithMCLanguage(JavaDSLMill.class)
public class ExpressionTypeTest {

  @ParameterizedTest(name = "{0} : {1}")
  @CsvSource(delimiterString = "=>", quoteCharacter = '`', textBlock = """
      1                           => int
      0x1F                        => int
      0b101                       => int
      1_000                       => int
      1L                          => long
      1.5f                        => float
      1.5                         => double
      1e3                         => double
      'a'                         => char
      '\\n'                       => char
      true                        => boolean
      "text"                      => java.lang.String
      null                        => null
      """)
  void literals(String expression, String expectedType) {
    check(expression, expectedType);
  }

  @ParameterizedTest(name = "{0} : {1}")
  @CsvSource(delimiterString = "=>", quoteCharacter = '`', textBlock = """
      i + i                       => int
      b + b                       => int
      sh * b                      => int
      c + 1                       => int
      c + c                       => int
      i + l                       => long
      i + f                       => float
      l + f                       => float
      l + d                       => double
      i / 2                       => int
      d % 2                       => double
      -b                          => int
      +c                          => int
      -d                          => double
      ~l                          => long
      ~c                          => int
      i++                         => int
      --l                         => long
      c++                         => char
      boxedInt + 1                => int
      boxedInt + boxedLong        => long
      boxedDouble * 2             => double
      -boxedInt                   => int
      boxedInt++                  => java.lang.Integer
      """)
  void arithmetic(String expression, String expectedType) {
    check(expression, expectedType);
  }

  @ParameterizedTest(name = "{0} : {1}")
  @CsvSource(delimiterString = "=>", quoteCharacter = '`', textBlock = """
      s + s                       => java.lang.String
      s + 1                       => java.lang.String
      1 + s                       => java.lang.String
      s + o                       => java.lang.String
      "a" + 'b'                   => java.lang.String
      s + null                    => java.lang.String
      i + 1 + s                   => java.lang.String
      s + bool                    => java.lang.String
      s + ia                      => java.lang.String
      """)
  void stringConcatenation(String expression, String expectedType) {
    check(expression, expectedType);
  }

  @ParameterizedTest(name = "{0} : {1}")
  @CsvSource(delimiterString = "=>", quoteCharacter = '`', textBlock = """
      i < l                       => boolean
      d >= c                      => boolean
      i == l                      => boolean
      boxedInt == i               => boolean
      o == s                      => boolean
      s != null                   => boolean
      color == Color.RED          => boolean
      bool && true                => boolean
      bool || boxedBool           => boolean
      !bool                       => boolean
      !boxedBool                  => boolean
      bool & false                => boolean
      bool | bool                 => boolean
      bool ^ true                 => boolean
      i & l                       => long
      i | 1                       => int
      i ^ i                       => int
      b & b                       => int
      i << 2                      => int
      b << 1                      => int
      l >> i                      => long
      i >>> l                     => int
      """)
  void comparisonsAndBitOperations(String expression, String expectedType) {
    check(expression, expectedType);
  }

  @ParameterizedTest(name = "{0} : {1}")
  @CsvSource(delimiterString = "=>", quoteCharacter = '`', textBlock = """
      bool ? 1 : 2                => int
      bool ? 1 : 2L               => long
      bool ? i : d                => double
      bool ? i : boxedInt         => int
      bool ? "a" : "b"            => java.lang.String
      bool ? s : null             => java.lang.String
      bool ? color : null         => p.Color
      """)
  void conditional(String expression, String expectedType) {
    check(expression, expectedType);
  }

  @ParameterizedTest(name = "{0} : {1}")
  @CsvSource(delimiterString = "=>", quoteCharacter = '`', textBlock = """
      i = 5                       => int
      l = i                       => long
      d = f                       => double
      o = s                       => java.lang.Object
      s = null                    => java.lang.String
      i += 2                      => int
      i += 2.5                    => int
      b += 1                      => byte
      c -= 1                      => char
      s += 1                      => java.lang.String
      i <<= 2                     => int
      bool &= true                => boolean
      ia[0] = 1                   => int
      boxedInt = 1                => java.lang.Integer
      """)
  void assignments(String expression, String expectedType) {
    check(expression, expectedType);
  }

  @ParameterizedTest(name = "{0} : {1}")
  @CsvSource(delimiterString = "=>", quoteCharacter = '`', textBlock = """
      (long) i                    => long
      (int) d                     => int
      (char) i                    => char
      (byte) c                    => byte
      (Object) s                  => java.lang.Object
      (String) o                  => java.lang.String
      (Integer) o                 => java.lang.Integer
      (int) boxedInt              => int
      (Number) boxedInt           => java.lang.Number
      (Shape) o                   => p.Shape
      (Context) shape             => p.Context
      (List<String>) o            => java.util.List<java.lang.String>
      (int[]) o                   => int[]
      o instanceof String         => boolean
      shape instanceof Context    => boolean
      ls instanceof ArrayList     => boolean
      """)
  void castsAndInstanceOf(String expression, String expectedType) {
    check(expression, expectedType);
  }

  @ParameterizedTest(name = "{0} : {1}")
  @CsvSource(delimiterString = "=>", quoteCharacter = '`', textBlock = """
      i                           => int
      this.i                      => int
      param                       => int
      baseField                   => int
      this.baseField              => int
      super.baseField             => int
      staticField                 => int
      Context.staticField         => int
      p.Context.staticField   => int
      CONSTANT                    => java.lang.String
      staticBaseField             => java.lang.String
      Base.staticBaseField        => java.lang.String
      Nested.nestedStatic         => int
      Context.Nested.nestedStatic => int
      Color.RED                   => p.Color
      bs.value                    => java.lang.String
      bbi.value.value             => java.lang.Integer
      new Inner().innerField      => int
      Integer.MAX_VALUE           => int
      System.out                  => java.io.PrintStream
      this                        => p.Context
      Context.this                => p.Context
      """)
  void namesAndFieldAccess(String expression, String expectedType) {
    check(expression, expectedType);
  }

  @ParameterizedTest(name = "{0} : {1}")
  @CsvSource(delimiterString = "=>", quoteCharacter = '`', textBlock = """
      ia                          => int[]
      iaa                         => int[][]
      ia[0]                       => int
      ia[c]                       => int
      iaa[0]                      => int[]
      iaa[0][1]                   => int
      sa[i]                       => java.lang.String
      ia.length                   => int
      iaa[0].length               => int
      new int[3]                  => int[]
      new int[2][3]               => int[][]
      new int[2][]                => int[][]
      new String[i]               => java.lang.String[]
      new int[] {1, 2}            => int[]
      new String[][] {{"a"}}      => java.lang.String[][]
      new Context[0]              => p.Context[]
      params                      => java.lang.String[]
      params.length               => int
      """)
  void arrays(String expression, String expectedType) {
    check(expression, expectedType);
  }

  @ParameterizedTest(name = "{0} : {1}")
  @CsvSource(delimiterString = "=>", quoteCharacter = '`', textBlock = """
      intMethod()                 => int
      this.intMethod()            => int
      voidMethod()                => void
      stringMethod(1)             => java.lang.String
      stringMethod(c)             => java.lang.String
      intMethod() + 1             => int
      stringMethod(1).length()    => int
      baseMethod()                => int
      super.baseMethod()          => int
      self()                      => p.Base
      self().self()               => p.Base
      staticBaseMethod()          => long
      Base.staticBaseMethod()     => long
      area()                      => double
      describe()                  => java.lang.String
      shape.area()                => double
      shape.describe()            => java.lang.String
      Shape.unit()                => p.Shape
      color.next()                => p.Color
      new Inner().innerMethod()   => int
      """)
  void methodCalls(String expression, String expectedType) {
    check(expression, expectedType);
  }

  @ParameterizedTest(name = "{0} : {1}")
  @CsvSource(delimiterString = "=>", quoteCharacter = '`', textBlock = """
      overloaded(1)               => int
      overloaded(b)               => int
      overloaded(c)               => int
      overloaded(1L)              => long
      overloaded(1.5f)            => double
      overloaded(1.5)             => double
      overloaded("a")             => java.lang.String
      overloaded(o)               => java.lang.Object
      overloaded(ls)              => java.lang.Object
      overloaded(boxedInt)        => java.lang.Object
      overloaded(null)            => java.lang.String
      varargs()                   => int
      varargs("a")                => int
      varargs("a", "b", "c")      => int
      varargs(sa)                 => int
      varargs(params)             => int
      varargs(1)                  => java.lang.String
      varargs(1, "x", 2)          => java.lang.String
      """)
  void overloadingAndVarargs(String expression, String expectedType) {
    check(expression, expectedType);
  }

  @ParameterizedTest(name = "{0} : {1}")
  @CsvSource(delimiterString = "=>", quoteCharacter = '`', textBlock = """
      id("a")                     => java.lang.String
      id(1)                       => java.lang.Integer
      id(color)                   => p.Color
      Context.<String>id(null)    => java.lang.String
      listOf("a", "b")            => java.util.List<java.lang.String>
      max(1, 2)                   => java.lang.Integer
      max("a", "b")               => java.lang.String
      Box.of("a")                 => p.Box<java.lang.String>
      Box.of(Box.of(1))           => p.Box<p.Box<java.lang.Integer>>
      bs.get()                    => java.lang.String
      bbi.get().get()             => java.lang.Integer
      bs.map(x -> x.length())     => p.Box<java.lang.Integer>
      bs.map(String::length)      => p.Box<java.lang.Integer>
      """)
  void genericMethods(String expression, String expectedType) {
    check(expression, expectedType);
  }

  /** Members of JDK types, which are loaded via class2mc. */
  @ParameterizedTest(name = "{0} : {1}")
  @CsvSource(delimiterString = "=>", quoteCharacter = '`', textBlock = """
      s.length()                  => int
      s.charAt(0)                 => char
      s.substring(1)              => java.lang.String
      s.equals(o)                 => boolean
      s.isEmpty()                 => boolean
      o.toString()                => java.lang.String
      o.hashCode()                => int
      String.valueOf(i)           => java.lang.String
      String.valueOf(bool)        => java.lang.String
      Integer.parseInt(s)         => int
      Integer.valueOf(i)          => java.lang.Integer
      Math.max(i, l)              => long
      Math.abs(d)                 => double
      ls.get(0)                   => java.lang.String
      ls.size()                   => int
      ls.iterator()               => java.util.Iterator<java.lang.String>
      ls.contains(o)              => boolean
      ali.get(0)                  => java.lang.Integer
      ali.get(0) + 1              => int
      msi.get("a")                => java.lang.Integer
      msi.keySet()                => java.util.Set<java.lang.String>
      msi.entrySet()              => java.util.Set<java.util.Map.Entry<java.lang.String,java.lang.Integer>>
      List.of(1, 2)               => java.util.List<java.lang.Integer>
      Arrays.asList(sa)           => java.util.List<java.lang.String>
      fsi.apply("a")              => java.lang.Integer
      runnable.run()              => void
      color.ordinal()             => int
      color.name()                => java.lang.String
      Color.valueOf("RED")        => p.Color
      Color.values()              => p.Color[]
      System.out.println(s)       => void
      new StringBuilder().append(i).toString()=> java.lang.String
      """)
  void jdkMembers(String expression, String expectedType) {
    check(expression, expectedType);
  }

  @ParameterizedTest(name = "{0} : {1}")
  @CsvSource(delimiterString = "=>", quoteCharacter = '`', textBlock = """
      new Context()               => p.Context
      new Base()                  => p.Base
      new Object()                => java.lang.Object
      new StringBuilder("a")      => java.lang.StringBuilder
      new Box<String>("a")        => p.Box<java.lang.String>
      new Box<>("a")              => p.Box<java.lang.String>
      new ArrayList<String>()     => java.util.ArrayList<java.lang.String>
      new Context.Nested()        => p.Context.Nested
      new Nested()                => p.Context.Nested
      new Inner()                 => p.Context.Inner
      this.new Inner()            => p.Context.Inner
      String.class                => java.lang.Class<java.lang.String>
      Context.class               => java.lang.Class<p.Context>
      int.class                   => java.lang.Class<java.lang.Integer>
      """)
  void instantiationAndClassLiterals(String expression, String expectedType) {
    check(expression, expectedType);
  }

  /**
   * Expressions whose type depends on the target type. The derived type must be compatible with
   * the target type, e.g., a lambda may be represented by a function type.
   */
  @ParameterizedTest(name = "{1} __probe = {0}")
  @CsvSource(delimiterString = "=>", quoteCharacter = '`', textBlock = """
      () -> {}                    => java.lang.Runnable
      () -> 1                     => java.util.function.Supplier<java.lang.Integer>
      x -> x.length()             => java.util.function.Function<java.lang.String,java.lang.Integer>
      (String x) -> x.isEmpty()   => java.util.function.Predicate<java.lang.String>
      (a, b) -> a + b             => java.util.function.BinaryOperator<java.lang.Integer>
      x -> { return x * 2; }      => java.util.function.IntUnaryOperator
      String::length              => java.util.function.Function<java.lang.String,java.lang.Integer>
      Context::new                => java.util.function.Supplier<p.Context>
      s::charAt                   => java.util.function.IntFunction<java.lang.Character>
      ArrayList::new              => java.util.function.Supplier<java.util.List<java.lang.String>>
      new ArrayList<>()           => java.util.List<java.lang.String>
      new Box<>(null)             => p.Box<java.lang.String>
      Collections.emptyList()     => java.util.List<java.lang.String>
      id(null)                    => java.lang.String
      listOf()                    => java.util.List<java.lang.Integer>
      bool ? 1 : null             => java.lang.Integer
      """)
  void targetTyped(String expression, String targetType) {
    Javac.assertCompiles(ExpressionProbe.model(expression, targetType));
    SymTypeExpression type = ExpressionProbe.typeOfWithTarget(expression, targetType);
    MCAssertions.assertNoFindings();
    assertFalse(type.isObscureType(), "obscure type");
    SymTypeExpression target = ExpressionProbe.declaredType();
    assertTrue(SymTypeRelations.isCompatible(target, type),
        () -> type.printFullName() + " is not compatible to " + target.printFullName());
  }

  /**
   * Switch expressions (Java 14, JLS 15.28) and patterns (Java 16/21, JLS 14.30). The type of a
   * standalone switch expression follows the rules of the conditional expression.
   */
  @ParameterizedTest(name = "{0} : {1}")
  @CsvSource(delimiterString = "=>", quoteCharacter = '`', textBlock = """
      switch (i) { case 1 -> "a"; default -> "b"; }                  => java.lang.String
      switch (color) { case RED -> 1; case GREEN, BLUE -> 2; }       => int
      switch (i) { case 1 -> 1; default -> 2L; }                     => long
      switch (s) { case "a": yield 1.5; default: yield 2.5; }        => double
      switch (i) { default -> { int x = i * 2; yield x; } }          => int
      switch (o) { case String t -> t.length(); default -> 0; }     => int
      switch (o) { case Integer t when t > 0 -> t; default -> 0; }   => int
      switch (o) { case null -> 0; case String t -> 1; default -> 2; } => int
      o instanceof String t                                          => boolean
      o instanceof String t && t.isEmpty()                           => boolean
      o instanceof String t ? t.length() : 0                         => int
      """)
  void switchExpressionsAndPatterns(String expression, String expectedType) {
    check(expression, expectedType);
  }

  /** Members imported by single static imports and static imports on demand (JLS 7.5.3). */
  @ParameterizedTest(name = "{0} : {1}")
  @CsvSource(delimiterString = "=>", quoteCharacter = '`', textBlock = """
      max(1, 2L)                  => long
      PI                          => double
      emptyList()                 => java.util.List<java.lang.Object>
      MAX_VALUE                   => int
      """)
  void staticImports(String expression, String expectedType) {
    String model = """
        package q;
        import static java.lang.Math.max;
        import static java.lang.Math.PI;
        import static java.util.Collections.*;
        import static java.lang.Integer.MAX_VALUE;
        class S {
          void probe() {
            %s __probe = %s;
          }
        }
        """.formatted(expectedType, expression);
    Javac.assertCompiles(model);
    SymbolTables.fromUncheckedSources(model);
    assertType(expectedType,
        TypeCheck3.typeOf(ExpressionProbe.probedExpressionIn("q.S.probe")));
    MCAssertions.assertNoFindings();
  }

  /** Ensures the case is valid Java and checks the type derived by the JavaDSL. */
  private static void check(String expression, String expectedType) {
    String model = ExpressionProbe.model(expression, expectedType);
    Javac.assertCompiles(model);
    assertType(expectedType, ExpressionProbe.typeOf(expression, expectedType));
    MCAssertions.assertNoFindings();
  }
}
