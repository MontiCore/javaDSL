/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java._symboltable;

import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.util.SymbolTables;
import de.monticore.javalight._symboltable.JavaMethodSymbol;
import de.monticore.runtime.junit.TestWithMCLanguage;
import de.monticore.symbols.basicsymbols._symboltable.TypeVarSymbol;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symbols.oosymbols._symboltable.MethodSymbol;
import de.monticore.types.check.SymTypeExpression;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static de.monticore.java.JavaDSLAssertions.assertType;
import static de.monticore.java.util.Symbols.*;
import static org.junit.jupiter.api.Assertions.*;

/** Symbols of methods and constructors. */
@TestWithMCLanguage(JavaDSLMill.class)
public class MethodSymbolTest {

  @ParameterizedTest(name = "{0} : {1}")
  @CsvSource(delimiterString = "=>", textBlock = """
      void m()                                => () -> void
      int m()                                 => () -> int
      String m(int a)                         => int -> java.lang.String
      int m(int a, long b)                    => (int, long) -> int
      int[] m(String[] a)                     => java.lang.String[] -> int[]
      int m()[]                               => () -> int[]
      List<String> m(List<? extends T> a)     => java.util.List<? extends a.C.T> -> java.util.List<java.lang.String>
      T m(T a)                                => a.C.T -> a.C.T
      Inner m(Inner a)                        => a.C.Inner -> a.C.Inner
      void m(String... a)                     => (java.lang.String...) -> void
      void m(int a, String... b)              => (int, java.lang.String...) -> void
      void m(int[]... a)                      => (int[]...) -> void
      """)
  void signatures(String signature, String expectedType) {
    SymbolTables.fromSource("""
        package a;
        import java.util.*;
        abstract class C<T> {
          class Inner { }
          abstract %s;
        }
        """.formatted(signature));
    assertType(expectedType, method("a.C.m").getFunctionType());
  }

  @Test
  void parametersAndVariableArity() {
    SymbolTables.fromSource("""
        package a;
        class C {
          void fixed(int a, String b) { }
          void variable(int a, String... b) { }
        }
        """);

    JavaMethodSymbol fixed = method("a.C.fixed");
    assertEquals(List.of("a", "b"),
        fixed.getParameterList().stream().map(VariableSymbol::getName).toList());
    assertFalse(fixed.isIsElliptic());
    assertTrue(method("a.C.variable").isIsElliptic());
    assertSame(fixed.getParameterList().get(0), localVariable(fixed, "a"));
  }

  @Test
  void modifiers() {
    SymbolTables.fromSource("""
        package a;
        abstract class C {
          public void publicMethod() { }
          protected void protectedMethod() { }
          private void privateMethod() { }
          void packagePrivateMethod() { }
          static void staticMethod() { }
          final void finalMethod() { }
          abstract void abstractMethod();
          synchronized void synchronizedMethod() { }
          native void nativeMethod();
          strictfp void strictfpMethod() { }
        }
        """);

    assertTrue(method("a.C.publicMethod").isIsPublic());
    assertTrue(method("a.C.protectedMethod").isIsProtected());
    assertTrue(method("a.C.privateMethod").isIsPrivate());
    JavaMethodSymbol packagePrivate = method("a.C.packagePrivateMethod");
    assertFalse(packagePrivate.isIsPublic() || packagePrivate.isIsProtected()
        || packagePrivate.isIsPrivate() || packagePrivate.isIsStatic()
        || packagePrivate.isIsAbstract());
    assertTrue(method("a.C.staticMethod").isIsStatic());
    assertTrue(method("a.C.finalMethod").isIsFinal());
    assertTrue(method("a.C.abstractMethod").isIsAbstract());
    assertTrue(method("a.C.synchronizedMethod").isIsSynchronized());
    assertTrue(method("a.C.nativeMethod").isIsNative());
    assertTrue(method("a.C.strictfpMethod").isIsStrictfp());
  }

  /**
   * Interface methods without body are implicitly public and abstract; default and static methods
   * are implicitly public (JLS 9.4).
   */
  @Test
  void interfaceMethodsAreImplicitlyPublic() {
    SymbolTables.fromSource("""
        package a;
        interface I {
          void abstractMethod();
          default void defaultMethod() { }
          static void staticMethod() { }
          private void privateMethod() { }
        }
        """);

    JavaMethodSymbol abstractMethod = method("a.I.abstractMethod");
    assertTrue(abstractMethod.isIsPublic(), "abstract method is public");
    assertTrue(abstractMethod.isIsAbstract(), "abstract method is abstract");

    JavaMethodSymbol defaultMethod = method("a.I.defaultMethod");
    assertTrue(defaultMethod.isIsDefault(), "default method is default");
    assertTrue(defaultMethod.isIsPublic(), "default method is public");
    assertFalse(defaultMethod.isIsAbstract(), "default method is not abstract");

    JavaMethodSymbol staticMethod = method("a.I.staticMethod");
    assertTrue(staticMethod.isIsStatic(), "static method is static");
    assertTrue(staticMethod.isIsPublic(), "static method is public");

    assertTrue(method("a.I.privateMethod").isIsPrivate());
    assertFalse(method("a.I.privateMethod").isIsPublic());
  }

  @Test
  void exceptions() {
    SymbolTables.fromSource("""
        package a;
        import java.io.IOException;
        class MyException extends Exception { }
        class C {
          void m() throws IOException, MyException, java.util.concurrent.TimeoutException { }
        }
        """);

    assertEquals(List.of("java.io.IOException", "a.MyException",
            "java.util.concurrent.TimeoutException"),
        method("a.C.m").getExceptionsList().stream()
            .map(SymTypeExpression::printFullName).toList());
  }

  @Test
  void annotations() {
    SymbolTables.fromSource("""
        package a;
        @interface Marker { }
        class C {
          @Override public String toString() { return ""; }
          @Marker @Deprecated void m() { }
        }
        """);

    assertEquals(List.of("java.lang.Override"), method("a.C.toString").getAnnotationsList()
        .stream().map(SymTypeExpression::printFullName).toList());
    assertEquals(List.of("a.Marker", "java.lang.Deprecated"), method("a.C.m")
        .getAnnotationsList().stream().map(SymTypeExpression::printFullName).toList());
  }

  @Test
  void genericMethods() {
    SymbolTables.fromSource("""
        package a;
        import java.util.*;
        class C {
          static <T> T id(T t) { return t; }
          <K, V extends Comparable<V>> Map<K, V> map(K key, V value) { return null; }
        }
        """);

    JavaMethodSymbol id = method("a.C.id");
    assertEquals(List.of("a.C.id.T"),
        id.getTypeVariableList().stream().map(TypeVarSymbol::getFullName).toList());
    assertType("a.C.id.T -> a.C.id.T", id.getFunctionType());

    JavaMethodSymbol map = method("a.C.map");
    assertEquals(List.of("K", "V"),
        map.getTypeVariableList().stream().map(TypeVarSymbol::getName).toList());
    assertType("java.lang.Comparable<a.C.map.V>",
        map.getTypeVariableList().get(1).getSuperTypesList().get(0));
    assertType("(a.C.map.K, a.C.map.V) -> java.util.Map<a.C.map.K,a.C.map.V>",
        map.getFunctionType());
  }

  @Test
  void overloadedMethods() {
    SymbolTables.fromSource("""
        package a;
        class C {
          void m() { }
          void m(int a) { }
          int m(String a, int... b) { return 0; }
        }
        """);

    assertEquals(List.of("() -> void", "int -> void", "(java.lang.String, int...) -> int"),
        methods("a.C.m").stream().map(m -> m.getFunctionType().printFullName()).toList());
  }

  @Test
  void constructors() {
    SymbolTables.fromSource("""
        package a;
        class C {
          C() { }
          private C(int a, String b) { }
        }
        enum E {
          X(1);
          E(int a) { }
        }
        """);

    List<MethodSymbol> constructors = methods("a.C.C");
    assertEquals(2, constructors.size());
    for (MethodSymbol constructor : constructors) {
      assertTrue(constructor.isIsConstructor(), "isConstructor");
      assertType("a.C", constructor.getType());
    }
    assertType("(int, java.lang.String) -> a.C", constructors.get(1).getFunctionType());
    assertTrue(constructors.get(1).isIsPrivate());

    JavaMethodSymbol enumConstructor = method("a.E.E");
    assertTrue(enumConstructor.isIsConstructor());
    assertType("int -> a.E", enumConstructor.getFunctionType());
  }

  /** Explicitly declared (non-canonical) constructors of records (JLS 8.10.4). */
  @Test
  void constructorsOfRecords() {
    SymbolTables.fromSource("""
        package a;
        record Point(int x, int y) {
          Point(int both) { this(both, both); }
        }
        record Pair<A>(A first) {
          Pair(A first, int ignored) { this(first); }
        }
        """);

    JavaMethodSymbol point = declaredConstructor("a.Point.Point");
    assertTrue(point.isIsConstructor());
    assertType("int -> a.Point", point.getFunctionType());
    assertType("(a.Pair.A, int) -> a.Pair<a.Pair.A>",
        declaredConstructor("a.Pair.Pair").getFunctionType());
  }

  /** The constructor that is declared in the model (records may also have implicit ones). */
  private static JavaMethodSymbol declaredConstructor(String fullName) {
    List<MethodSymbol> declared = methods(fullName).stream()
        .filter(MethodSymbol::isPresentAstNode).toList();
    assertEquals(1, declared.size(), "declared constructors of " + fullName);
    return assertInstanceOf(JavaMethodSymbol.class, declared.get(0));
  }

  @Test
  void constructorOfGenericClass() {
    SymbolTables.fromSource("""
        package a;
        class Box<T> {
          Box(T value) { }
        }
        """);
    assertType("a.Box.T -> a.Box<a.Box.T>", method("a.Box.Box").getFunctionType());
  }

  /**
   * A class without constructor declaration has an implicit default constructor with the access
   * modifier of the class (JLS 8.8.9); the one of an enum is private (JLS 8.9.2).
   */
  @Test
  void defaultConstructor() {
    SymbolTables.fromSource("""
        package a;
        public class Public { }
        class PackagePrivate { }
        class Outer {
          protected static class Protected { }
          private class Private { }
        }
        class Box<T> { }
        enum E { X }
        """);

    JavaMethodSymbol publicConstructor = method("a.Public.Public");
    assertTrue(publicConstructor.isIsConstructor());
    assertTrue(publicConstructor.isIsPublic());
    assertType("() -> a.Public", publicConstructor.getFunctionType());
    assertTrue(publicConstructor.getParameterList().isEmpty());

    JavaMethodSymbol packagePrivate = method("a.PackagePrivate.PackagePrivate");
    assertFalse(packagePrivate.isIsPublic() || packagePrivate.isIsProtected()
        || packagePrivate.isIsPrivate());
    assertTrue(method("a.Outer.Protected.Protected").isIsProtected());
    assertTrue(method("a.Outer.Private.Private").isIsPrivate());
    assertType("() -> a.Box<a.Box.T>", method("a.Box.Box").getFunctionType());

    JavaMethodSymbol enumConstructor = method("a.E.E");
    assertTrue(enumConstructor.isIsConstructor());
    assertTrue(enumConstructor.isIsPrivate());
    assertType("() -> a.E", enumConstructor.getFunctionType());
  }

  /** There is no default constructor if a constructor is declared. */
  @Test
  void noDefaultConstructorBesidesDeclaredConstructors() {
    SymbolTables.fromSource("""
        package a;
        class C { C(int x) { } }
        enum E { X(1); E(int x) { } }
        """);

    assertEquals(List.of("int -> a.C"), methods("a.C.C").stream()
        .map(m -> m.getFunctionType().printFullName()).toList());
    assertEquals(List.of("int -> a.E"), methods("a.E.E").stream()
        .map(m -> m.getFunctionType().printFullName()).toList());
  }

  @Test
  void constructorsOfNestedClasses() {
    SymbolTables.fromSource("""
        package a;
        class Outer {
          static class Nested { Nested(int a) { } }
          class Inner { Inner() { } }
        }
        """);

    assertType("int -> a.Outer.Nested", method("a.Outer.Nested.Nested").getFunctionType());
    assertType("() -> a.Outer.Inner", method("a.Outer.Inner.Inner").getFunctionType());
  }

  @Test
  void methodsOfAnonymousClassesAreNotMethodsOfOuterClass() {
    SymbolTables.fromSource("""
        package a;
        class C {
          Runnable r = new Runnable() { public void run() { } };
          void m() { }
        }
        """);

    assertEquals(List.of("m"), typeDeclaration("a.C").getSpannedScope()
        .getLocalJavaMethodSymbols().stream()
        .filter(m -> !m.isIsConstructor())
        .map(JavaMethodSymbol::getName).toList());
    assertTrue(methods("a.C.run").isEmpty());
  }
}
