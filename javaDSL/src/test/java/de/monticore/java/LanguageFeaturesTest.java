/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.util.Javac;
import de.monticore.java.util.SymbolTables;
import de.monticore.runtime.junit.MCAssertions;
import de.monticore.runtime.junit.TestWithMCLanguage;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static de.monticore.java.JavaDSLAssertions.assertParsingSuccess;

/**
 * The language features up to {@link Javac#JAVA_VERSION Java 21}, named after the Java version
 * in which they became final. Each model must be accepted by javac for Java 21, be parsable, and
 * its symbol table must be built without findings. Hence, the test report shows which features
 * the JavaDSL supports already and which are still missing.
 *
 * <p>Detailed tests of the symbol table and type check of the features are part of the
 * respective test classes, e.g., {@code VariableSymbolTest#patternVariables}.</p>
 */
@TestWithMCLanguage(JavaDSLMill.class)
public class LanguageFeaturesTest {

  static Stream<Named<String>> features() {
    return Stream.of(
        feature("Java 5: generics with wildcards and bounds", """
            package f;
            import java.util.*;
            class C<T extends Comparable<? super T>> {
              <E extends Number & Comparable<E>> E max(List<? extends E> list) { return list.get(0); }
              Map<String, List<? super Integer>> map = new HashMap<String, List<? super Integer>>();
            }
            """),
        feature("Java 5: enhanced for, variable arity, autoboxing", """
            package f;
            class C {
              int sum(Integer... values) {
                int sum = 0;
                for (int value : values) { sum += value; }
                return sum;
              }
            }
            """),
        feature("Java 5: enums with constructors and constant bodies", """
            package f;
            enum Op {
              PLUS("+") { int apply(int a, int b) { return a + b; } },
              TIMES("*") { int apply(int a, int b) { return a * b; } };
              private final String symbol;
              Op(String symbol) { this.symbol = symbol; }
              abstract int apply(int a, int b);
            }
            """),
        feature("Java 5: annotation types and annotations", """
            package f;
            import java.lang.annotation.*;
            @Retention(RetentionPolicy.RUNTIME) @Target({ElementType.TYPE, ElementType.METHOD})
            @interface Info { String value() default ""; int[] numbers() default {1, 2}; }
            @Info(value = "c", numbers = {3}) class C {
              @Info("m") @SuppressWarnings({"unchecked", "rawtypes"}) void m() { }
            }
            """),
        feature("Java 5: static imports", """
            package f;
            import static java.lang.Math.*;
            import static java.util.Collections.emptyList;
            class C { double d = max(PI, 1); Object o = emptyList(); }
            """),
        feature("Java 7: diamond, binary literals, underscores in literals", """
            package f;
            import java.util.*;
            class C { List<String> list = new ArrayList<>(); int mask = 0b1010_1010; long big = 1_000_000L; }
            """),
        feature("Java 7: try-with-resources and multi-catch", """
            package f;
            import java.io.*;
            class C {
              void m() {
                try (StringReader a = new StringReader(""); StringWriter b = new StringWriter()) {
                  a.read();
                } catch (IOException | RuntimeException e) { }
              }
            }
            """),
        feature("Java 7: switch on strings", """
            package f;
            class C {
              int m(String s) {
                switch (s) { case "a": return 1; case "b": return 2; default: return 0; }
              }
            }
            """),
        feature("Java 8: lambda expressions", """
            package f;
            import java.util.function.*;
            class C {
              Runnable r = () -> { };
              Function<String, Integer> f = s -> s.length();
              BinaryOperator<Integer> op = (Integer a, Integer b) -> { return a + b; };
              Supplier<Supplier<String>> nested = () -> () -> "";
            }
            """),
        feature("Java 8: method and constructor references", """
            package f;
            import java.util.*;
            import java.util.function.*;
            class C {
              Function<String, Integer> unbound = String::length;
              Supplier<Integer> bound = "abc"::length;
              Function<String, Integer> statik = Integer::parseInt;
              Supplier<List<String>> constructor = ArrayList::new;
              IntFunction<int[]> array = int[]::new;
              Function<List<String>, Integer> generic = List<String>::size;
              Supplier<String> superRef = super::toString;
            }
            """),
        feature("Java 8: default and static interface methods", """
            package f;
            interface I {
              void m();
              default void d() { m(); }
              static I create() { return () -> { }; }
            }
            """),
        feature("Java 8: type annotations in declarations", """
            package f;
            import java.lang.annotation.*;
            import java.util.List;
            @Target(ElementType.TYPE_USE) @interface NonNull { }
            class C {
              @NonNull String s;
              List<@NonNull String> list;
              String @NonNull [] array;
              java.util.@NonNull List<String> qualified;
            }
            """),
        feature("Java 8: type annotations in casts and type arguments of calls", """
            package f;
            import java.lang.annotation.*;
            import java.util.*;
            @Target(ElementType.TYPE_USE) @interface NonNull { }
            class C {
              String m(Object o) { return (@NonNull String) o; }
              List<String> n() { return Collections.<@NonNull String>emptyList(); }
            }
            """),
        feature("Java 8: receiver parameters", """
            package f;
            class C {
              void m(C this, Object o) { }
              class Inner { Inner(C C.this) { } }
            }
            """),
        feature("Java 8: intersection casts", """
            package f;
            import java.io.Serializable;
            class C { Runnable r = (Runnable & Serializable) () -> { }; }
            """),
        feature("Java 9: private interface methods", """
            package f;
            interface I {
              default int d() { return helper(); }
              private int helper() { return 1; }
              private static int staticHelper() { return 2; }
            }
            """),
        feature("Java 9: try-with-resources on effectively final variables", """
            package f;
            import java.io.*;
            class C {
              void m() throws IOException {
                StringReader reader = new StringReader("");
                try (reader) { reader.read(); }
              }
            }
            """),
        feature("Java 9: diamond with anonymous classes", """
            package f;
            import java.util.*;
            class C { Comparator<String> c = new Comparator<>() { public int compare(String a, String b) { return 0; } }; }
            """),
        feature("Java 9: module declarations", """
            open module f.mod {
              requires java.base;
              requires transitive java.logging;
              requires static java.sql;
              uses java.lang.Runnable;
            }
            """),
        feature("Java 10: local variable type inference", """
            package f;
            import java.util.*;
            class C {
              void m() {
                var list = new ArrayList<String>();
                for (var s : list) { }
                for (var i = 0; i < 1; i++) { }
              }
            }
            """),
        feature("Java 11: var in lambda parameters", """
            package f;
            import java.util.function.*;
            class C { BiFunction<Integer, Integer, Integer> f = (var a, var b) -> a + b; }
            """),
        feature("Java 14: switch expressions with arrow labels", """
            package f;
            class C {
              String m(int i) {
                return switch (i) {
                  case 1 -> "one";
                  case 2, 3 -> "few";
                  default -> { String s = "many"; yield s; }
                };
              }
            }
            """),
        feature("Java 14: switch expressions with yield", """
            package f;
            class C {
              int m(String s) {
                int length = switch (s) {
                  case "a", "b":
                    yield 1;
                  default:
                    yield s.length();
                };
                return length;
              }
            }
            """),
        feature("Java 14: switch statements with arrow labels", """
            package f;
            class C {
              void m(int i) {
                switch (i) {
                  case 1 -> System.out.println("one");
                  case 2, 3 -> { System.out.println("few"); }
                  default -> throw new IllegalArgumentException();
                }
              }
            }
            """),
        feature("Java 15: text blocks", """
            package f;
            class C {
              String html = \"""
                  <p>
                    "quoted" \\
                  continued\\s
                  </p>
                  \""";
            }
            """),
        feature("Java 16: records", """
            package f;
            import java.util.List;
            record Point(int x, int y) implements Comparable<Point> {
              static final Point ORIGIN = new Point(0, 0);
              Point { if (x < 0) throw new IllegalArgumentException(); }
              Point(int both) { this(both, both); }
              public int compareTo(Point other) { return Integer.compare(x, other.x); }
              record Pair<A, B>(A first, B second) { }
            }
            record Varargs(String... names) { }
            record Annotated(@Deprecated List<String> items) { }
            """),
        feature("Java 16: pattern matching for instanceof", """
            package f;
            class C {
              int m(Object o) {
                if (o instanceof String s && !s.isEmpty()) { return s.length(); }
                if (!(o instanceof Integer i)) { return 0; }
                return i;
              }
            }
            """),
        feature("Java 16: local records, enums, and interfaces", """
            package f;
            class C {
              void m() {
                record Pair(int a, int b) { }
                enum Color { RED, GREEN }
                interface Shape { double area(); }
                Pair p = new Pair(1, 2);
              }
            }
            """),
        feature("Java 16: static members of inner classes", """
            package f;
            class C {
              class Inner {
                static int counter;
                static void count() { counter++; }
              }
            }
            """),
        feature("Java 17: sealed classes and interfaces", """
            package f;
            sealed interface Shape permits Circle, Square, Polygon { }
            final class Circle implements Shape { }
            record Square(double side) implements Shape { }
            non-sealed class Polygon implements Shape { }
            sealed abstract class Node { }
            final class Leaf extends Node { }
            """),
        feature("Java 21: pattern matching for switch", """
            package f;
            class C {
              String m(Object o) {
                return switch (o) {
                  case null -> "null";
                  case Integer i when i > 10 -> "big " + i;
                  case Integer i -> "small " + i;
                  case String s -> s;
                  default -> o.toString();
                };
              }
              void n(Object o) {
                switch (o) {
                  case String s: System.out.println(s); break;
                  case null, default: System.out.println("other");
                }
              }
            }
            """),
        feature("Java 21: record patterns", """
            package f;
            record Point(int x, int y) { }
            record Line(Point from, Point to) { }
            class C {
              int m(Object o) {
                if (o instanceof Point(int x, var y)) { return x + y; }
                return switch (o) {
                  case Line(Point(var x1, var y1), Point p) when x1 > 0 -> x1 + y1 + p.x();
                  case Line l -> 0;
                  default -> -1;
                };
              }
            }
            """),
        feature("Java 21: exhaustive switch over sealed types", """
            package f;
            sealed interface Shape permits Circle, Square { }
            record Circle(double radius) implements Shape { }
            record Square(double side) implements Shape { }
            class C {
              double area(Shape shape) {
                return switch (shape) {
                  case Circle c -> Math.PI * c.radius() * c.radius();
                  case Square s -> s.side() * s.side();
                };
              }
            }
            """),
        keyword("var", "Java 10"),
        keyword("yield", "Java 14"),
        keyword("record", "Java 16"),
        keyword("sealed", "Java 17"),
        keyword("permits", "Java 17"),
        keyword("when", "Java 21"),
        keyword("module", "Java 9"),
        keyword("open", "Java 9"),
        keyword("exports", "Java 9"),
        keyword("to", "Java 9"),
        keyword("with", "Java 9"));
  }

  /**
   * Contextual keywords (JLS 3.9) remain valid identifiers of variables, fields, and methods
   * (except for yield, which can not be the name of an unqualified method invocation).
   */
  private static Named<String> keyword(String keyword, String since) {
    return feature(since + ": contextual keyword '" + keyword + "' as identifier", """
        package f;
        class C {
          int %1$s = 1;
          int %1$s(int %1$s) { return %1$s + this.%1$s; }
          void m() { int %1$s = this.%1$s(2); %1$s++; }
        }
        """.formatted(keyword));
  }

  private static Named<String> feature(String name, String model) {
    return Named.of(name, model);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("features")
  void parse(String model) {
    Javac.assertCompiles(model);
    assertParsingSuccess(model, JavaDSLMill.parser()::parse_String);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("features")
  void buildSymbolTable(String model) {
    SymbolTables.fromSource(model);
    MCAssertions.assertNoFindings("Findings while building the symbol table");
  }
}
