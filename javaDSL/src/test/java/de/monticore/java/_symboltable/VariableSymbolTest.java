/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java._symboltable;

import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._symboltable.TypeDeclarationSymbol;
import de.monticore.java.util.Javac;
import de.monticore.java.util.SymbolTables;
import de.monticore.java.utils.JavaDSLSymbolTableUtil;
import de.monticore.javalight._symboltable.JavaMethodSymbol;
import de.monticore.runtime.junit.MCAssertions;
import de.monticore.runtime.junit.TestWithMCLanguage;
import de.monticore.symbols.oosymbols._symboltable.FieldSymbol;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static de.monticore.java.JavaDSLAssertions.assertParsingSuccess;
import static de.monticore.java.JavaDSLAssertions.assertType;
import static de.monticore.java.util.Symbols.*;
import static org.junit.jupiter.api.Assertions.*;

/** Symbols of fields, parameters, local variables, and other variables. */
@TestWithMCLanguage(JavaDSLMill.class)
public class VariableSymbolTest {

  @ParameterizedTest(name = "{0} : {1}")
  @CsvSource(delimiterString = "=>", textBlock = """
      int x                                   => int
      boolean x                               => boolean
      char x                                  => char
      String x                                => java.lang.String
      java.lang.String x                      => java.lang.String
      Object x                                => java.lang.Object
      D x                                     => a.D
      a.D x                                   => a.D
      C.Inner x                               => a.C.Inner
      D.Nested x                              => a.D.Nested
      java.util.concurrent.TimeUnit x         => java.util.concurrent.TimeUnit
      java.sql.Connection x                   => java.sql.Connection
      int[] x                                 => int[]
      int x[]                                 => int[]
      int[] x[]                               => int[][]
      String[][] x                            => java.lang.String[][]
      List<String> x                          => java.util.List<java.lang.String>
      List<List<Integer>> x                   => java.util.List<java.util.List<java.lang.Integer>>
      Map<String, int[]> x                    => java.util.Map<java.lang.String,int[]>
      List<?> x                               => java.util.List<?>
      List<? extends Number> x                => java.util.List<? extends java.lang.Number>
      List<? super Integer> x                 => java.util.List<? super java.lang.Integer>
      List<String>[] x                        => java.util.List<java.lang.String>[]
      Map.Entry<String, Integer> x            => java.util.Map.Entry<java.lang.String,java.lang.Integer>
      java.util.Map.Entry<String, Integer> x  => java.util.Map.Entry<java.lang.String,java.lang.Integer>
      T x                                     => a.C.T
      List<T> x                               => java.util.List<a.C.T>
      T[] x                                   => a.C.T[]
      # within a generic class, its simple name denotes the raw type (JLS 4.8)
      C x                                     => a.C
      """)
  void typesOfFields(String declaration, String expectedType) {
    SymbolTables.fromSource("""
        package a;
        import java.util.*;
        class D { public static class Nested { } }
        class C<T> {
          class Inner { }
          %s;
        }
        """.formatted(declaration));
    assertType(expectedType, field("a.C.x").getType());
  }

  @Test
  void typeAnnotationsAreIgnored() {
    SymbolTables.fromSource("""
        package a;
        import java.lang.annotation.*;
        @Target(ElementType.TYPE_USE) @interface NonNull { }
        class C {
          @NonNull String s;
          java.util.@NonNull List<@NonNull String> list;
          String @NonNull [] array;
        }
        """);

    assertType("java.lang.String", field("a.C.s").getType());
    assertType("java.util.List<java.lang.String>", field("a.C.list").getType());
    assertType("java.lang.String[]", field("a.C.array").getType());
  }

  @Test
  void modifiersOfFields() {
    SymbolTables.fromSource("""
        package a;
        class C {
          public int publicField;
          protected int protectedField;
          private int privateField;
          int packagePrivateField;
          static int staticField;
          final int finalField = 0;
          public static final int CONSTANT = 1;
        }
        """);

    FieldSymbol publicField = field("a.C.publicField");
    assertTrue(publicField.isIsPublic());
    assertFalse(publicField.isIsProtected() || publicField.isIsPrivate());
    assertTrue(field("a.C.protectedField").isIsProtected());
    assertTrue(field("a.C.privateField").isIsPrivate());

    FieldSymbol packagePrivate = field("a.C.packagePrivateField");
    assertFalse(packagePrivate.isIsPublic() || packagePrivate.isIsProtected()
        || packagePrivate.isIsPrivate() || packagePrivate.isIsStatic()
        || packagePrivate.isIsFinal());

    assertTrue(field("a.C.staticField").isIsStatic());
    assertTrue(field("a.C.finalField").isIsFinal());
    FieldSymbol constant = field("a.C.CONSTANT");
    assertTrue(constant.isIsPublic() && constant.isIsStatic() && constant.isIsFinal());
  }

  /** Fields of interfaces are implicitly public, static, and final (JLS 9.3). */
  @Test
  void fieldsOfInterfacesArePublicStaticFinal() {
    SymbolTables.fromSource("""
        package a;
        interface I {
          int X = 1;
          String[] NAMES = {"a"};
        }
        """);

    for (String name : new String[] {"a.I.X", "a.I.NAMES"}) {
      FieldSymbol symbol = field(name);
      assertAll(name,
          () -> assertTrue(symbol.isIsPublic(), "public"),
          () -> assertTrue(symbol.isIsStatic(), "static"),
          () -> assertTrue(symbol.isIsFinal(), "final"));
    }
    assertType("int", field("a.I.X").getType());
    assertType("java.lang.String[]", field("a.I.NAMES").getType());
  }

  @Test
  void severalDeclaratorsInOneDeclaration() {
    SymbolTables.fromSource("""
        package a;
        class C {
          private int a, b[], c[][] = {};
          void m() {
            int d = 1, e[] = {d};
          }
        }
        """);

    assertType("int", field("a.C.a").getType());
    assertType("int[]", field("a.C.b").getType());
    assertType("int[][]", field("a.C.c").getType());
    assertTrue(field("a.C.b").isIsPrivate(), "modifiers apply to all declarators");
    assertType("int", localVariable(method("a.C.m"), "d").getType());
    assertType("int[]", localVariable(method("a.C.m"), "e").getType());
  }

  @Test
  void parameters() {
    SymbolTables.fromSource("""
        package a;
        import java.util.List;
        class C {
          void m(int i, final String s, List<String> list, int[] array, int matrix[][]) { }
        }
        """);

    JavaMethodSymbol m = method("a.C.m");
    assertEquals(5, m.getParameterList().size());
    assertType("int", localVariable(m, "i").getType());
    assertType("java.lang.String", localVariable(m, "s").getType());
    assertType("java.util.List<java.lang.String>", localVariable(m, "list").getType());
    assertType("int[]", localVariable(m, "array").getType());
    assertType("int[][]", localVariable(m, "matrix").getType());
  }

  /** Within the method, a variable arity parameter T... has the type T[] (JLS 8.4.1). */
  @Test
  void variableArityParameterIsArray() {
    SymbolTables.fromSource("""
        package a;
        class C {
          void m(int first, String... rest) { }
        }
        """);
    assertType("java.lang.String[]", localVariable(method("a.C.m"), "rest").getType());
  }

  @Test
  void localVariables() {
    SymbolTables.fromSource("""
        package a;
        import java.util.*;
        class C {
          void m() {
            int i = 0;
            final String s = "";
            List<String> list = new ArrayList<>();
            Thread.State state = null;
          }
        }
        """);

    JavaMethodSymbol m = method("a.C.m");
    assertType("int", localVariable(m, "i").getType());
    assertType("java.lang.String", localVariable(m, "s").getType());
    assertType("java.util.List<java.lang.String>", localVariable(m, "list").getType());
    assertType("java.lang.Thread.State", localVariable(m, "state").getType());
  }

  /** The type of a local variable declared with var is the type of its initializer. */
  @ParameterizedTest(name = "var x = {0} : {1}")
  @CsvSource(delimiterString = "=>", quoteCharacter = '`', textBlock = """
      1                         => int
      1L                        => long
      'c'                       => char
      1.5                       => double
      true                      => boolean
      i + 1                     => int
      i + 1.5                   => double
      field                     => java.lang.String
      field.length()            => int
      new StringBuilder()       => java.lang.StringBuilder
      new ArrayList<String>()   => java.util.ArrayList<java.lang.String>
      new int[3]                => int[]
      list                      => java.util.List<java.lang.String>
      list.get(0)               => java.lang.String
      map.keySet()              => java.util.Set<java.lang.String>
      (Object) field            => java.lang.Object
      """)
  void typeOfVarIsInferred(String initializer, String expectedType) {
    SymbolTables.fromSource("""
        package a;
        import java.util.*;
        class C {
          String field;
          List<String> list;
          Map<String, Integer> map;
          void m(int i) {
            var x = %s;
          }
        }
        """.formatted(initializer));
    assertType(expectedType, localVariable(method("a.C.m"), "x").getType());
  }

  /** String literals have the type String, which is in particular relevant for var. */
  @Test
  void typeOfVarWithStringLiteral() {
    SymbolTables.fromSource("""
        package a;
        class C {
          void m() { var x = "text"; }
        }
        """);
    assertType("java.lang.String", localVariable(method("a.C.m"), "x").getType());
  }

  /**
   * {@link JavaDSLSymbolTableUtil#buildSymbolTable(ASTCompilationUnit)} for a single model must
   * also infer the types of var.
   */
  @Test
  void buildSymbolTableOfSingleModelInfersVar() {
    JavaDSLSymbolTableUtil.prepareMill(true);
    ASTCompilationUnit ast = assertParsingSuccess("""
        package a;
        class C { void m() { var x = 1; } }
        """, JavaDSLMill.parser()::parse_String);
    JavaDSLSymbolTableUtil.buildSymbolTable(ast);
    assertType("int", localVariable(method("a.C.m"), "x").getType());
  }

  @Test
  void loopVariables() {
    SymbolTables.fromSource("""
        package a;
        import java.util.*;
        class C {
          void m(List<String> list, int[] array, Map<String, Integer> map) {
            for (int i = 0, j = 1; i < j; i++) { }
            for (String s : list) { }
            for (final int e : array) { }
            for (Map.Entry<String, Integer> entry : map.entrySet()) { }
          }
        }
        """);

    JavaMethodSymbol m = method("a.C.m");
    assertType("int", localVariable(m, "i").getType());
    assertType("int", localVariable(m, "j").getType());
    assertType("java.lang.String", localVariable(m, "s").getType());
    assertType("int", localVariable(m, "e").getType());
    assertType("java.util.Map.Entry<java.lang.String,java.lang.Integer>",
        localVariable(m, "entry").getType());
  }

  /** The type of a var loop variable is the element type of the iterated array or Iterable. */
  @Test
  void varLoopVariables() {
    SymbolTables.fromSource("""
        package a;
        import java.util.*;
        class C {
          void m(List<String> list, int[] array, Map<String, Integer> map) {
            for (var s : list) { }
            for (var e : array) { }
            for (var key : map.keySet()) { }
            for (var i = 0; i < 10; i++) { }
          }
        }
        """);

    JavaMethodSymbol m = method("a.C.m");
    assertType("java.lang.String", localVariable(m, "s").getType());
    assertType("int", localVariable(m, "e").getType());
    assertType("java.lang.String", localVariable(m, "key").getType());
    assertType("int", localVariable(m, "i").getType());
  }

  /** The type of a 'var' loop variable is the element type of the array or Iterable. */
  @Test
  void varLoopVariablesOfOtherIterables() {
    SymbolTables.fromSource("""
        package a;
        import java.util.*;
        class Numbers implements Iterable<Integer> {
          public Iterator<Integer> iterator() { return null; }
        }
        class C {
          void m(int[][] matrix, Numbers numbers, List<? extends Number> upper,
              List<? super Integer> lower, List<?> unbounded, Map<String, Integer> map) {
            for (var row : matrix) { }
            for (var number : numbers) { }
            for (var u : upper) { }
            for (var l : lower) { }
            for (var o : unbounded) { }
            for (var entry : map.values()) { }
          }
        }
        """);

    JavaMethodSymbol m = method("a.C.m");
    assertType("int[]", localVariable(m, "row").getType());
    assertType("java.lang.Integer", localVariable(m, "number").getType());
    assertType("java.lang.Number", localVariable(m, "u").getType());
    assertType("java.lang.Object", localVariable(m, "l").getType());
    assertType("java.lang.Object", localVariable(m, "o").getType());
    assertType("java.lang.Integer", localVariable(m, "entry").getType());
  }

  @Test
  void varLoopVariableOfNonIterableIsReported() {
    String model = """
        package a;
        class C {
          void m(String s) {
            for (var c : s) { }
          }
        }
        """;
    Javac.assertDoesNotCompile(model);
    SymbolTables.fromUncheckedSources(model);

    MCAssertions.assertHasFindingStartingWith("0x7A023");
    assertTrue(localVariable(method("a.C.m"), "c").getType().isObscureType());
  }

  @Test
  void resourcesAndCatchParameters() {
    SymbolTables.fromSource("""
        package a;
        import java.io.*;
        class C {
          void m() {
            try (StringReader reader = new StringReader(""); var writer = new StringWriter()) {
            }
            catch (IOException ioException) {
            }
            catch (IllegalStateException | IllegalArgumentException multiCatch) {
            }
          }
        }
        """);

    JavaMethodSymbol m = method("a.C.m");
    assertType("java.io.StringReader", localVariable(m, "reader").getType());
    assertType("java.io.StringWriter", localVariable(m, "writer").getType());
    assertType("java.io.IOException", localVariable(m, "ioException").getType());
    // the type of a multi-catch parameter is the least upper bound of the alternatives (JLS 14.20)
    assertType("java.lang.RuntimeException", localVariable(m, "multiCatch").getType());
  }

  @Test
  void lambdaParameters() {
    SymbolTables.fromSource("""
        package a;
        import java.util.function.*;
        class C {
          BiFunction<Integer, Integer, Integer> add = (Integer x, Integer y) -> x + y;
          Function<String, Integer> length = (var s) -> s.length();
          Predicate<String> isEmpty = t -> t.isEmpty();
        }
        """);

    TypeDeclarationSymbol c = typeDeclaration("a.C");
    assertType("java.lang.Integer", localVariable(c, "x").getType());
    assertType("java.lang.String", localVariable(c, "s").getType());
    assertType("java.lang.String", localVariable(c, "t").getType());
  }

  /** Pattern variables of instanceof (Java 16, JLS 14.30.1). */
  @Test
  void patternVariables() {
    SymbolTables.fromSource("""
        package a;
        import java.util.List;
        class C {
          void m(Object o) {
            if (o instanceof String s) { }
            if (o instanceof List<?> list && !list.isEmpty()) { }
          }
        }
        """);

    JavaMethodSymbol m = method("a.C.m");
    assertType("java.lang.String", localVariable(m, "s").getType());
    assertType("java.util.List<?>", localVariable(m, "list").getType());
  }

  /** Pattern variables of record patterns (Java 21, JLS 14.30.1); var infers the type. */
  @Test
  void recordPatternVariables() {
    SymbolTables.fromSource("""
        package a;
        record Point(int x, String label) { }
        record Line(Point from, Point to) { }
        class C {
          void m(Object o) {
            if (o instanceof Point(int x, var label)) { }
            if (o instanceof Line(Point(var x1, var y1), Point to)) { }
          }
        }
        """);

    JavaMethodSymbol m = method("a.C.m");
    assertType("int", localVariable(m, "x").getType());
    assertType("java.lang.String", localVariable(m, "label").getType());
    assertType("int", localVariable(m, "x1").getType());
    assertType("java.lang.String", localVariable(m, "y1").getType());
    assertType("a.Point", localVariable(m, "to").getType());
  }

  /** Pattern variables in switch labels (Java 21, JLS 14.11.1). */
  @Test
  void switchPatternVariables() {
    SymbolTables.fromSource("""
        package a;
        class C {
          int m(Object o) {
            return switch (o) {
              case Integer i when i > 0 -> i;
              case String s -> s.length();
              default -> 0;
            };
          }
        }
        """);

    JavaMethodSymbol m = method("a.C.m");
    assertType("java.lang.Integer", localVariable(m, "i").getType());
    assertType("java.lang.String", localVariable(m, "s").getType());
  }

  /** Inner classes may declare static members (Java 16, JLS 8.1.3). */
  @Test
  void staticMembersOfInnerClasses() {
    SymbolTables.fromSource("""
        package a;
        class C {
          class Inner { static int counter; }
        }
        """);
    assertTrue(field("a.C.Inner.counter").isIsStatic());
  }

  @Test
  void fieldsOfNestedAndAnonymousClassesAreNotFieldsOfOuterClass() {
    SymbolTables.fromSource("""
        package a;
        class C {
          int outer;
          class Inner { int inner; }
          Object anonymous = new Object() { int anonymousField; };
        }
        """);

    var classScope = typeDeclaration("a.C").getSpannedScope();
    assertEquals(java.util.List.of("outer", "anonymous"),
        classScope.getLocalFieldSymbols().stream().map(FieldSymbol::getName).toList());
    assertEquals("a.C.Inner.inner", field("a.C.Inner.inner").getFullName());
  }
}
