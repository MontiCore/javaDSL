/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.types3;

import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.util.Javac;
import de.monticore.java.util.SymbolTables;
import de.monticore.runtime.junit.MCAssertions;
import de.monticore.runtime.junit.TestWithMCLanguage;
import de.se_rwth.commons.logging.Log;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Type checks statements and member declarations with the {@link TypeCheckCoCos}. Valid cases
 * must not produce any finding; invalid cases must produce the expected error. Every case is first
 * verified with javac.
 */
@TestWithMCLanguage(JavaDSLMill.class)
public class StatementTypeCheckTest {

  static final String CONTEXT = """
      package p;

      import java.io.*;
      import java.util.*;

      class Resource implements AutoCloseable {
        public void close() { }
      }

      class CloseableResource implements Closeable {
        public void close() { }
      }

      enum Level { LOW, HIGH }

      class StatementContext {
        int i; long l; double d; char ch; boolean bool; Boolean boxedBool; Integer boxedInt;
        String s; Object o; int[] ia; List<String> ls; Map<String, Integer> msi; Level level;

        void nothing() { }

        /*MEMBERS*/

        void method() throws Exception {
          /*STATEMENTS*/
        }
      }
      """;

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {
      "if (bool) { }",
      "if (i > 0) { } else if (boxedBool) { } else { }",
      "while (bool) { }",
      "do { } while (i < 10);",
      "for (int x = 0; x < 10; x++) { }",
      "for (;;) { break; }",
      "label: for (;;) { break label; }",
      "assert bool;",
      "assert i > 0 : \"message\";",
      "assert bool : i;",
      "synchronized (o) { }",
      "synchronized (this) { }",
  })
  void validControlFlow(String statements) {
    checkValid("", statements);
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {
      "for (String x : ls) { }",
      "for (Object x : ls) { }",
      "for (int x : ia) { }",
      "for (long x : ia) { }",
      "for (var x : ls) { x.length(); }",
      "for (Map.Entry<String, Integer> e : msi.entrySet()) { }",
      "for (String key : msi.keySet()) { }",
  })
  void validForEach(String statements) {
    checkValid("", statements);
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {
      "switch (i) { case 1: break; default: }",
      "switch (ch) { case 'a': break; }",
      "switch (s) { case \"a\": break; }",
      "switch (level) { case LOW: break; case HIGH: break; }",
      "switch (boxedInt) { case 1: break; }",
  })
  void validSwitch(String statements) {
    checkValid("", statements);
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {
      "throw new RuntimeException();",
      "throw new Exception(\"message\");",
      "try { } catch (RuntimeException e) { }",
      "try { } catch (IllegalStateException | IllegalArgumentException e) { }",
      "try { } finally { }",
      "try (Resource r = new Resource()) { }",
      "try (CloseableResource r = new CloseableResource()) { }",
      "try (var r = new Resource()) { }",
  })
  void validExceptions(String statements) {
    checkValid("", statements);
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {
      "int x = 1;",
      "long x = i;",
      "double x = i;",
      "Object x = s;",
      "String x = null;",
      "byte x = 1;",
      "char x = 65;",
      "Integer x = 1;",
      "int x = boxedInt;",
      "int[] x = {1, 2};",
      "int[] x = new int[] {1};",
      "List<String> x = new ArrayList<>();",
      "Map<String, List<Integer>> x = new HashMap<>();",
      "var x = s.length();",
      "final int x = 1;",
      "int x = 1, y = x + 1;",
      "i++;",
      "i += 1;",
      "s.length();",
      "ls.add(\"a\");",
      "nothing();",
  })
  void validDeclarationsAndExpressionStatements(String statements) {
    checkValid("", statements);
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {
      "int f = 1;",
      "String f = \"a\" + 1;",
      "static final long F = 1;",
      "int r() { return 1; }",
      "long r() { return i; }",
      "Object r() { return s; }",
      "Integer r() { return 1; }",
      "int r() { return boxedInt; }",
      "List<String> r() { return new ArrayList<>(); }",
      "void r() { return; }",
      "int r(boolean c) { if (c) { return 1; } else { return 2; } }",
      "int r() { throw new RuntimeException(); }",
      "int r() { while (true) { } }",
      "Object r() { return this; }",
      "String r() { return super.toString(); }",
      "int r() { return this.i + super.hashCode(); }",
      // return statements of lambdas do not belong to the method
      "void r() { java.util.function.Supplier<String> f = () -> { return \"\"; }; }",
      "int r() { java.util.function.Supplier<String> f = () -> { return \"\"; }; return 1; }",
  })
  void validMembers(String members) {
    checkValid(members, "");
  }

  /**
   * A pattern variable is only in scope where the pattern has definitely matched (Java 16,
   * JLS 6.3.1).
   */
  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {
      "if (o instanceof String t) { t.length(); }",
      "if (o instanceof String t && t.length() > 0) { }",
      "if (!(o instanceof String t)) { return; } t.length();",
      "if (!(o instanceof String t) || t.isEmpty()) { }",
      "while (!(o instanceof String t)) { o = \"\"; } t.length();",
      "int x = o instanceof String t ? t.length() : 0;",
      "if (o instanceof String t) { } else if (o instanceof Integer t) { int x = t; }",
  })
  void validPatternScoping(String statements) {
    checkValid("", statements);
  }

  /** Switch statements with arrow labels (Java 14) and patterns (Java 21, JLS 14.11). */
  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {
      "switch (i) { case 1 -> nothing(); case 2, 3 -> { } default -> { } }",
      "switch (level) { case LOW -> { } case HIGH -> nothing(); }",
      "switch (s) { case \"a\", \"b\" -> { } default -> throw new IllegalStateException(); }",
      "switch (o) { case String t -> t.length(); default -> { } }",
      "switch (o) { case String t when t.isEmpty() -> { } case String t -> { } default -> { } }",
      "switch (o) { case null, default -> { } }",
      "switch (o) { case Integer t: int x = t; break; default: break; }",
      "int x = switch (i) { case 1 -> 1; default -> { yield 2; } };",
      "String x = switch (level) { case LOW -> \"low\"; case HIGH -> \"high\"; };",
  })
  void validArrowAndPatternSwitch(String statements) {
    checkValid("", statements);
  }

  @ParameterizedTest(name = "{0} [{1}]")
  @CsvSource(delimiterString = "=>", quoteCharacter = '`', textBlock = """
      if (o instanceof String t) { } t.length();                              => error
      if (o instanceof String t || t.isEmpty()) { }                            => error
      if (o instanceof Integer t) { String x = t; }                            => 0xA0921
      if (s instanceof Integer t) { }                                          => error
      switch (o) { case String t when t.length() -> { } default -> { } }     => error
      switch (o) { case String t -> t.unknown(); default -> { } }             => error
      int x = switch (i) { case 1 -> "a"; default -> "b"; };                  => error
      int x = switch (i) { case 1: yield 1; default: yield "b"; };            => error
      """)
  void invalidPatternsAndSwitches(String statements, String expectedError) {
    checkInvalid("", statements, expectedError);
  }

  @ParameterizedTest(name = "{0} [{1}]")
  @CsvSource(delimiterString = "=>", quoteCharacter = '`', textBlock = """
      if (i) { }                              => 0xA0909
      if (s) { }                              => 0xA0909
      while (i) { }                           => 0xA0919
      do { } while (s);                       => 0xA0905
      for (; i; ) { }                         => 0xA0906
      for (int x : i) { }                     => 0xA0907
      for (int x : ls) { }                    => 0xA0908
      for (String x : ia) { }                 => 0xA0908
      assert i;                               => 0xA0901
      assert bool : nothing();                => 0xA0902
      synchronized (i) { }                    => 0xA0918
      throw new Object();                     => 0xA0918
      throw 1;                                => 0xA0918
      try { } catch (String e) { }            => 0xA0903
      try (Object r = new Object()) { }       => 0xA0920
      switch (d) { case 1: break; }           => 0xA0917
      switch (bool) { default: }              => 0xA0917
      switch (i) { case "a": break; }         => 0xA0925
      switch (s) { case 1: break; }           => 0xA0925
      int x = "a";                            => 0xA0921
      String x = 1;                           => 0xA0921
      int x = 1L;                             => 0xA0921
      byte x = i;                             => 0xA0921
      Integer x = 1L;                         => 0xA0921
      List<String> x = new ArrayList<Integer>(); => error
      var x = {1};                            => 0x7A024
      var x = () -> 1;                        => 0x7A024
      var x = null;                           => 0x7A024
      var x = nothing();                      => 0x7A024
      try (var r = null) { }                  => 0x7A024
      s.unknown();                            => error
      ls.add(1);                              => error
      """)
  void invalidStatements(String statements, String expectedError) {
    checkInvalid("", statements, expectedError);
  }

  @ParameterizedTest(name = "{0} [{1}]")
  @CsvSource(delimiterString = "=>", quoteCharacter = '`', textBlock = """
      int f = "a";                            => 0xA0921
      String f = 1;                           => 0xA0921
      void r() { return 1; }                  => 0xA0910
      int r() { return; }                     => 0xA0911
      int r() { }                             => 0xA0911
      int r() { return "a"; }                 => 0xA0912
      String r() { return i; }                => 0xA0912
      List<String> r() { return ia; }         => 0xA0912
      static Object r() { return this; }      => 0x7A020
      static void r() { super.toString(); }   => 0x7A020
      """)
  void invalidMembers(String members, String expectedError) {
    checkInvalid(members, "", expectedError);
  }

  static String model(String members, String statements) {
    return CONTEXT.replace("/*MEMBERS*/", members).replace("/*STATEMENTS*/", statements);
  }

  private static void checkValid(String members, String statements) {
    String model = model(members, statements);
    Javac.assertCompiles(model);
    TypeCheckCoCos.check(SymbolTables.fromUncheckedSources(model).get(0));
    MCAssertions.assertNoFindings();
  }

  /**
   * @param expectedError the expected error code, or {@code error} if any type error is
   *                      expected, e.g., for an expression statement that has no specific code
   */
  private static void checkInvalid(String members, String statements, String expectedError) {
    String model = model(members, statements);
    Javac.assertDoesNotCompile(model);
    TypeCheckCoCos.check(SymbolTables.fromUncheckedSources(model).get(0));
    boolean hasExpectedError = Log.getFindings().stream()
        .filter(f -> f.isError() && !f.getMsg().contains("internal error"))
        .anyMatch(f -> expectedError.equals("error") || f.getMsg().startsWith(expectedError));
    if (!hasExpectedError) {
      MCAssertions.failAndPrintFindings("Expected the type error " + expectedError);
    }
    // further findings may be a consequence of the error
    MCAssertions.assertHasFindings(f -> true);
  }
}
