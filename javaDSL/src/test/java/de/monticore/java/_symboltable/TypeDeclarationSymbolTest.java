/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java._symboltable;

import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._symboltable.IJavaDSLArtifactScope;
import de.monticore.java.javadsl._symboltable.IJavaDSLScope;
import de.monticore.java.javadsl._symboltable.TypeDeclarationSymbol;
import de.monticore.java.util.SymbolTables;
import de.monticore.javalight._symboltable.JavaMethodSymbol;
import de.monticore.runtime.junit.TestWithMCLanguage;
import de.monticore.symbols.basicsymbols._symboltable.TypeVarSymbol;
import de.monticore.symbols.oosymbols._symboltable.OOTypeSymbol;
import de.monticore.symboltable.ImportStatement;
import de.monticore.types.check.SymTypeExpression;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.monticore.java.JavaDSLAssertions.assertType;
import static de.monticore.java.util.Symbols.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Symbols of type declarations: their kind, modifiers, super types, type parameters, and their
 * location in the scope tree.
 */
@TestWithMCLanguage(JavaDSLMill.class)
public class TypeDeclarationSymbolTest {

  @Nested
  @TestWithMCLanguage(JavaDSLMill.class)
  class ArtifactScope {

    @Test
    void packageAndImports() {
      IJavaDSLArtifactScope scope = SymbolTables.fromSource("""
          package a.b;
          import java.util.List;
          import java.io.*;
          public class A { }
          """);

      assertEquals("a.b", scope.getPackageName());
      assertEquals(List.of("java.util.List", "java.io", "java.lang"),
          scope.getImportsList().stream().map(ImportStatement::getStatement).toList());
      assertEquals(List.of(false, true, true),
          scope.getImportsList().stream().map(ImportStatement::isStar).toList());
    }

    @Test
    void nameOfArtifactScopeIsTheNameOfThePublicType() {
      IJavaDSLArtifactScope scope = SymbolTables.fromSource("""
          package a;
          class Helper { }
          public class Main { }
          """);
      assertEquals("Main", scope.getName());
    }

    @Test
    void defaultPackage() {
      IJavaDSLArtifactScope scope = SymbolTables.fromSource("class A { class B { } }");

      assertEquals("", scope.getPackageName());
      assertEquals("A", typeDeclaration("A").getFullName());
      assertEquals("A.B", typeDeclaration("A.B").getFullName());
    }

    @Test
    void severalTopLevelTypes() {
      IJavaDSLArtifactScope scope = SymbolTables.fromSource("""
          package a;
          class A { }
          interface I { }
          enum E { }
          """);

      assertEquals(List.of("a.A", "a.I", "a.E"), scope.getLocalTypeDeclarationSymbols().stream()
          .map(TypeDeclarationSymbol::getFullName).toList());
    }
  }

  @Nested
  @TestWithMCLanguage(JavaDSLMill.class)
  class Kinds {

    @Test
    void kindOfTypes() {
      SymbolTables.fromSource("""
          package a;
          class C { }
          interface I { }
          enum E { }
          record R() { }
          @interface A { }
          """);

      assertKind("a.C", true, false, false, false, false);
      assertKind("a.I", false, true, false, false, false);
      assertKind("a.E", false, false, true, false, false);
      assertKind("a.R", false, false, false, true, false);
      assertKind("a.A", false, false, false, false, true);
    }

    /** An annotation type is a special interface (JLS 9.6). */
    @Test
    void annotationIsInterface() {
      SymbolTables.fromSource("package a; @interface A { }");
      assertTrue(typeDeclaration("a.A").isIsInterface());
    }

    private void assertKind(String type, boolean isClass, boolean isInterface, boolean isEnum,
        boolean isRecord, boolean isAnnotation) {
      TypeDeclarationSymbol symbol = typeDeclaration(type);
      assertAll(type,
          () -> assertEquals(isClass, symbol.isIsClass(), "isClass"),
          () -> assertEquals(isInterface || isAnnotation, symbol.isIsInterface(), "isInterface"),
          () -> assertEquals(isEnum, symbol.isIsEnum(), "isEnum"),
          () -> assertEquals(isRecord, symbol.isIsRecord(), "isRecord"),
          () -> assertEquals(isAnnotation, symbol.isIsAnnotation(), "isAnnotation"));
    }
  }

  @Nested
  @TestWithMCLanguage(JavaDSLMill.class)
  class Modifiers {

    @Test
    void modifiersOfTopLevelTypes() {
      SymbolTables.fromSources(
          "package a; public abstract class A { }",
          "package a; final class F { }");

      TypeDeclarationSymbol a = typeDeclaration("a.A");
      assertTrue(a.isIsPublic());
      assertTrue(a.isIsAbstract());
      assertFalse(a.isIsFinal());

      TypeDeclarationSymbol f = typeDeclaration("a.F");
      assertFalse(f.isIsPublic());
      assertTrue(f.isIsFinal());
      assertFalse(f.isIsAbstract());
    }

    @Test
    void modifiersOfMemberTypes() {
      SymbolTables.fromSource("""
          package a;
          class Outer {
            public static class PublicStatic { }
            protected class Protected { }
            private class Private { }
            class PackagePrivate { }
          }
          """);

      TypeDeclarationSymbol publicStatic = typeDeclaration("a.Outer.PublicStatic");
      assertTrue(publicStatic.isIsPublic());
      assertTrue(publicStatic.isIsStatic());

      assertTrue(typeDeclaration("a.Outer.Protected").isIsProtected());
      assertFalse(typeDeclaration("a.Outer.Protected").isIsStatic());
      assertTrue(typeDeclaration("a.Outer.Private").isIsPrivate());

      TypeDeclarationSymbol packagePrivate = typeDeclaration("a.Outer.PackagePrivate");
      assertFalse(packagePrivate.isIsPublic());
      assertFalse(packagePrivate.isIsProtected());
      assertFalse(packagePrivate.isIsPrivate());
    }

    /** Interfaces are implicitly abstract (JLS 9.1.1.1). */
    @Test
    void interfaceIsImplicitlyAbstract() {
      SymbolTables.fromSource("package a; interface I { }");
      assertTrue(typeDeclaration("a.I").isIsAbstract());
    }

    /** Member types of interfaces are implicitly public and static (JLS 9.5). */
    @Test
    void memberTypesOfInterfacesAreImplicitlyPublicAndStatic() {
      SymbolTables.fromSource("""
          package a;
          interface I { class C { } }
          @interface A { enum E { X } }
          """);

      for (String name : List.of("a.I.C", "a.A.E")) {
        TypeDeclarationSymbol type = typeDeclaration(name);
        assertTrue(type.isIsPublic(), name + " is public");
        assertTrue(type.isIsStatic(), name + " is static");
      }
    }

    /** Nested enums, records, and interfaces are implicitly static (JLS 8.9, 8.10, 9.1.1.3). */
    @Test
    void nestedEnumsRecordsAndInterfacesAreImplicitlyStatic() {
      SymbolTables.fromSource("""
          package a;
          class Outer {
            enum E { }
            record R() { }
            interface I { }
          }
          """);

      assertTrue(typeDeclaration("a.Outer.E").isIsStatic(), "enum");
      assertTrue(typeDeclaration("a.Outer.R").isIsStatic(), "record");
      assertTrue(typeDeclaration("a.Outer.I").isIsStatic(), "interface");
    }

    /** An enum with a constant body is not final, but sealed (JLS 8.9). */
    @Test
    void enumWithConstantBodyIsNotFinal() {
      SymbolTables.fromSource("package a; enum E { X { }, Y }");
      assertFalse(typeDeclaration("a.E").isIsFinal());
    }

    /** Member classes and local classes are not implicitly static. */
    @Test
    void classesAreNotImplicitlyStatic() {
      SymbolTables.fromSource("""
          package a;
          class Outer {
            class Inner { }
            void m() { class Local { } }
          }
          """);

      assertFalse(typeDeclaration("a.Outer.Inner").isIsStatic(), "inner class");
      assertFalse(typeDeclaration("a.Outer").isIsStatic(), "top level class");
      assertFalse(typeDeclaration("a.Outer.Inner").isIsFinal(), "inner class is not final");
    }

    /**
     * Elements of annotation interfaces are public and abstract, their constants public, static,
     * and final (JLS 9.6.1, 9.3).
     */
    @Test
    void elementsOfAnnotationTypes() {
      SymbolTables.fromSource("""
          package a;
          @interface A {
            int DEFAULT = 1;
            int value() default DEFAULT;
          }
          """);

      var value = method("a.A.value");
      assertTrue(value.isIsPublic() && value.isIsAbstract(), "public abstract element");
      var constant = field("a.A.DEFAULT");
      assertTrue(constant.isIsPublic() && constant.isIsStatic() && constant.isIsFinal(),
          "public static final constant");
      assertTrue(typeDeclaration("a.A").isIsAbstract(), "annotation interface is abstract");
    }

    /** Enums without constant bodies and records are implicitly final (JLS 8.9, 8.10). */
    @Test
    void enumsAndRecordsAreImplicitlyFinal() {
      SymbolTables.fromSource("""
          package a;
          class Outer {
            enum E { X }
            record R() { }
          }
          """);

      assertTrue(typeDeclaration("a.Outer.E").isIsFinal(), "enum");
      assertTrue(typeDeclaration("a.Outer.R").isIsFinal(), "record");
    }
  }

  @Nested
  @TestWithMCLanguage(JavaDSLMill.class)
  class SuperTypes {

    @Test
    void superClassAndInterfaces() {
      SymbolTables.fromSource("""
          package a;
          import java.util.*;
          interface I { }
          interface J<T> { }
          class Base { }
          class C extends Base implements I, J<String> { }
          class L extends ArrayList<String> implements Comparable<L> {
            public int compareTo(L other) { return 0; }
          }
          """);

      TypeDeclarationSymbol c = typeDeclaration("a.C");
      assertTrue(c.isPresentSuperClass());
      assertType("a.Base", c.getSuperClass());
      assertTypes(List.of("a.I", "a.J<java.lang.String>"), c.getInterfaceList());
      assertTypes(List.of("a.Base", "a.I", "a.J<java.lang.String>"), c.getSuperTypesList());

      TypeDeclarationSymbol l = typeDeclaration("a.L");
      assertType("java.util.ArrayList<java.lang.String>", l.getSuperClass());
      assertTypes(List.of("java.lang.Comparable<a.L>"), l.getInterfaceList());
    }

    @Test
    void superInterfacesOfInterface() {
      SymbolTables.fromSource("""
          package a;
          interface I { }
          interface J extends I, java.io.Serializable { }
          """);

      TypeDeclarationSymbol j = typeDeclaration("a.J");
      assertFalse(j.isPresentSuperClass());
      assertTypes(List.of("a.I", "java.io.Serializable"), j.getSuperTypesList());
    }

    @Test
    void superInterfacesOfEnumAndRecord() {
      SymbolTables.fromSource("""
          package a;
          interface I { }
          enum E implements I { }
          record R() implements I { }
          """);

      assertTrue(typeDeclaration("a.E").getSuperTypesList().stream()
          .anyMatch(t -> t.printFullName().equals("a.I")), "enum implements I");
      assertTrue(typeDeclaration("a.R").getSuperTypesList().stream()
          .anyMatch(t -> t.printFullName().equals("a.I")), "record implements I");
    }

    /** Every class except Object has a direct super class (JLS 8.1.4). */
    @Test
    void implicitSuperClassObject() {
      SymbolTables.fromSource("package a; class C { }");

      TypeDeclarationSymbol c = typeDeclaration("a.C");
      assertTrue(c.isPresentSuperClass());
      assertType("java.lang.Object", c.getSuperClass());
    }

    /** The direct super class of an enum E is Enum&lt;E&gt; (JLS 8.9). */
    @Test
    void implicitSuperClassOfEnum() {
      SymbolTables.fromSource("package a; enum E { }");

      TypeDeclarationSymbol e = typeDeclaration("a.E");
      assertTrue(e.isPresentSuperClass());
      assertType("java.lang.Enum<a.E>", e.getSuperClass());
    }

    /**
     * Object is the direct super type of an interface without super interfaces (JLS 4.10.2);
     * Annotation is the one of an annotation interface (JLS 9.6).
     */
    @Test
    void implicitSuperTypesOfInterfaceAndAnnotation() {
      SymbolTables.fromSource("""
          package a;
          interface I { }
          interface J extends I { }
          @interface A { }
          """);

      assertTypes(List.of("java.lang.Object"), typeDeclaration("a.I").getSuperTypesList());
      assertTypes(List.of("a.I"), typeDeclaration("a.J").getSuperTypesList());
      assertTypes(List.of("java.lang.annotation.Annotation"),
          typeDeclaration("a.A").getSuperTypesList());
    }

    /** The direct super class of a record is Record (JLS 8.10). */
    @Test
    void implicitSuperClassOfRecord() {
      SymbolTables.fromSource("package a; record R() { }");

      TypeDeclarationSymbol r = typeDeclaration("a.R");
      assertTrue(r.isPresentSuperClass());
      assertType("java.lang.Record", r.getSuperClass());
    }

    @Test
    void superTypeIsNestedTypeOfOtherClass() {
      SymbolTables.fromSource("""
          package a;
          class Outer { public static class Inner { } public interface Listener { } }
          class C extends Outer.Inner implements Outer.Listener { }
          """);

      TypeDeclarationSymbol c = typeDeclaration("a.C");
      assertTypes(List.of("a.Outer.Inner", "a.Outer.Listener"), c.getSuperTypesList());
      assertType("a.Outer.Inner", c.getSuperClass());
    }

    /** Sealed classes and interfaces (Java 17, JLS 8.1.1.2, 9.1.1.4). */
    @Test
    void sealedHierarchy() {
      SymbolTables.fromSource("""
          package a;
          sealed interface Shape permits Circle, Square, Polygon { }
          final class Circle implements Shape { }
          record Square(double side) implements Shape { }
          non-sealed class Polygon implements Shape { }
          """);

      assertTrue(typeDeclaration("a.Shape").isIsInterface());
      assertTrue(typeDeclaration("a.Circle").isIsFinal());
      assertFalse(typeDeclaration("a.Polygon").isIsFinal());
      for (String type : List.of("a.Circle", "a.Square", "a.Polygon")) {
        assertTrue(typeDeclaration(type).getSuperTypesList().stream()
            .anyMatch(t -> t.printFullName().equals("a.Shape")), type + " implements Shape");
      }
    }

    @Test
    void superClassIsGenericTypeWithTypeVariable() {
      SymbolTables.fromSource("""
          package a;
          class Base<T> { }
          class C<U> extends Base<U> { }
          """);
      assertType("a.Base<a.C.U>", typeDeclaration("a.C").getSuperClass());
    }
  }

  @Nested
  @TestWithMCLanguage(JavaDSLMill.class)
  class TypeParameters {

    @Test
    void typeParametersOfClass() {
      SymbolTables.fromSource("package a; class Pair<K, V> { }");

      TypeDeclarationSymbol pair = typeDeclaration("a.Pair");
      assertEquals(List.of("a.Pair.K", "a.Pair.V"),
          pair.getTypeParameterList().stream().map(TypeVarSymbol::getFullName).toList());
      assertSame(pair.getTypeParameterList().get(0), typeVar("a.Pair.K"));
    }

    @Test
    void boundsOfTypeParameters() {
      SymbolTables.fromSource("""
          package a;
          interface I<T> { }
          class C<A, B extends Number, D extends Number & Comparable<D>, E extends I<E>, F extends B> { }
          """);

      assertTypes(List.of(), typeVar("a.C.A").getSuperTypesList());
      assertTypes(List.of("java.lang.Number"), typeVar("a.C.B").getSuperTypesList());
      // multiple bounds are represented by an intersection type
      assertTypes(List.of("java.lang.Comparable<a.C.D> & java.lang.Number"),
          typeVar("a.C.D").getSuperTypesList());
      assertTypes(List.of("a.I<a.C.E>"), typeVar("a.C.E").getSuperTypesList());
      assertTypes(List.of("a.C.B"), typeVar("a.C.F").getSuperTypesList());
    }

    @Test
    void typeParametersOfInterfaceAndRecord() {
      SymbolTables.fromSource("""
          package a;
          interface I<T extends Comparable<T>> { }
          record R<T>(T value) { }
          """);

      TypeVarSymbol t = typeDeclaration("a.I").getTypeParameterList().get(0);
      assertEquals("a.I.T", t.getFullName());
      assertTypes(List.of("java.lang.Comparable<a.I.T>"), t.getSuperTypesList());
      assertEquals(1, typeDeclaration("a.R").getTypeParameterList().size());
    }
  }

  @Nested
  @TestWithMCLanguage(JavaDSLMill.class)
  class NestedLocalAndAnonymousTypes {

    @Test
    void nestedTypes() {
      SymbolTables.fromSource("""
          package a;
          class Outer {
            class Inner { class InnerInner { } }
            static class Nested { }
            interface I { }
            enum E { X }
          }
          """);

      TypeDeclarationSymbol outer = typeDeclaration("a.Outer");
      TypeDeclarationSymbol inner = typeDeclaration("a.Outer.Inner");
      assertSame(outer.getSpannedScope(), inner.getEnclosingScope());
      assertSame(inner.getSpannedScope(),
          typeDeclaration("a.Outer.Inner.InnerInner").getEnclosingScope());
      typeDeclaration("a.Outer.Nested");
      typeDeclaration("a.Outer.I");
      typeDeclaration("a.Outer.E");
    }

    @Test
    void nestedTypeWithSameNameInDifferentOuterTypes() {
      SymbolTables.fromSource("""
          package a;
          class A { class Node { } }
          class B { class Node { } }
          """);
      assertNotSame(typeDeclaration("a.A.Node"), typeDeclaration("a.B.Node"));
    }

    @Test
    void localClass() {
      SymbolTables.fromSource("""
          package a;
          class C {
            void m() {
              class Local { int x; }
              Local l = new Local();
            }
          }
          """);

      IJavaDSLScope methodScope = (IJavaDSLScope) method("a.C.m").getSpannedScope();
      List<TypeDeclarationSymbol> locals = typeDeclarationsIn(methodScope, "Local");
      assertEquals(1, locals.size(), "local class");
      assertSame(locals.get(0), localVariable(method("a.C.m"), "l").getType().getTypeInfo());
    }

    /** Local records, enums, and interfaces are implicitly static (Java 16, JLS 14.3). */
    @Test
    void localRecordsEnumsAndInterfaces() {
      SymbolTables.fromSource("""
          package a;
          class C {
            void m() {
              record Pair(int a, int b) { }
              enum Color { RED }
              interface Shape { }
            }
          }
          """);

      IJavaDSLScope methodScope = (IJavaDSLScope) method("a.C.m").getSpannedScope();
      TypeDeclarationSymbol pair = onlyTypeDeclarationIn(methodScope, "Pair");
      assertTrue(pair.isIsRecord(), "record");
      assertTrue(onlyTypeDeclarationIn(methodScope, "Color").isIsEnum(), "enum");
      assertTrue(onlyTypeDeclarationIn(methodScope, "Shape").isIsInterface(), "interface");
      assertTrue(pair.isIsStatic(), "local record is static");
    }

    private TypeDeclarationSymbol onlyTypeDeclarationIn(IJavaDSLScope scope, String name) {
      List<TypeDeclarationSymbol> types = typeDeclarationsIn(scope, name);
      assertEquals(1, types.size(), "local type " + name);
      return types.get(0);
    }

    @Test
    void anonymousClassSpansScope() {
      SymbolTables.fromSource("""
          package a;
          class C {
            Runnable r = new Runnable() {
              int counter;
              public void run() { counter++; }
            };
          }
          """);

      IJavaDSLScope classScope = (IJavaDSLScope) typeDeclaration("a.C").getSpannedScope();
      List<? extends IJavaDSLScope> anonymous = classScope.getSubScopes().stream()
          .filter(s -> s.isPresentAstNode()
              && s.getAstNode() instanceof de.monticore.java.javadsl._ast.ASTAnonymousClass)
          .toList();
      assertEquals(1, anonymous.size(), "scope of the anonymous class");
      IJavaDSLScope anonymousScope = anonymous.get(0);
      assertEquals(1, anonymousScope.getLocalFieldSymbols().size());
      assertEquals(1, anonymousScope.getLocalJavaMethodSymbols().size());
    }

    private List<TypeDeclarationSymbol> typeDeclarationsIn(IJavaDSLScope scope, String name) {
      List<TypeDeclarationSymbol> result = new java.util.ArrayList<>(
          scope.getLocalTypeDeclarationSymbols().stream()
              .filter(t -> t.getName().equals(name)).toList());
      scope.getSubScopes().forEach(sub -> result.addAll(typeDeclarationsIn(sub, name)));
      return result;
    }
  }

  @Nested
  @TestWithMCLanguage(JavaDSLMill.class)
  class EnumsRecordsAndAnnotations {

    @Test
    void enumConstants() {
      SymbolTables.fromSource("""
          package a;
          enum Planet {
            MERCURY(1), VENUS(2) { double gravity() { return 0; } };
            private final int order;
            Planet(int order) { this.order = order; }
            double gravity() { return 1; }
          }
          """);

      for (String constant : List.of("a.Planet.MERCURY", "a.Planet.VENUS")) {
        var symbol = field(constant);
        assertAll(constant,
            () -> assertType("a.Planet", symbol.getType()),
            () -> assertTrue(symbol.isIsEnumConstant(), "isEnumConstant"),
            () -> assertTrue(symbol.isIsPublic(), "public"),
            () -> assertTrue(symbol.isIsStatic(), "static"),
            () -> assertTrue(symbol.isIsFinal(), "final"));
      }
      assertFalse(field("a.Planet.order").isIsEnumConstant());
    }

    /** Enums have the implicit methods values() and valueOf(String) (JLS 8.9.3). */
    @Test
    void implicitMethodsOfEnum() {
      SymbolTables.fromSource("package a; enum E { X }");

      assertType("a.E[]", method("a.E.values").getType());
      assertTrue(method("a.E.values").isIsStatic());
      assertType("java.lang.String -> a.E", method("a.E.valueOf").getFunctionType());
    }

    /** Record components define private final fields (JLS 8.10.3). */
    @Test
    void recordComponentsAreFields() {
      SymbolTables.fromSource("package a; record Point(int x, int[] ys, String... names) { }");

      assertType("int", field("a.Point.x").getType());
      assertTrue(field("a.Point.x").isIsPrivate(), "private");
      assertTrue(field("a.Point.x").isIsFinal(), "final");
      assertType("int[]", field("a.Point.ys").getType());
      assertType("java.lang.String[]", field("a.Point.names").getType());
    }

    /** Records have implicit accessors and a canonical constructor (JLS 8.10.3, 8.10.4). */
    @Test
    void implicitMembersOfRecord() {
      SymbolTables.fromSource("package a; record Point(int x, String name) { }");

      assertType("() -> int", method("a.Point.x").getFunctionType());
      assertType("() -> java.lang.String", method("a.Point.name").getFunctionType());
      assertType("(int, java.lang.String) -> a.Point", method("a.Point.Point").getFunctionType());
      assertTrue(method("a.Point.x").isIsPublic(), "accessor is public");
      assertTrue(method("a.Point.Point").isIsConstructor());
      assertEquals(List.of("x", "name"), method("a.Point.Point").getParameterList().stream()
          .map(p -> p.getName()).toList());
    }

    /** The implicit canonical constructor has the access modifier of the record (JLS 8.10.4). */
    @Test
    void accessOfImplicitCanonicalConstructor() {
      SymbolTables.fromSource("""
          package a;
          public record Public(int x) { }
          class Outer { private record Private(int x) { } }
          """);

      assertTrue(method("a.Public.Public").isIsPublic());
      assertTrue(method("a.Outer.Private.Private").isIsPrivate());
    }

    @Test
    void canonicalConstructorOfGenericAndVariableArityRecords() {
      SymbolTables.fromSource("""
          package a;
          record Pair<A, B>(A first, B second) { }
          record Names(int count, String... names) { }
          """);

      assertType("(a.Pair.A, a.Pair.B) -> a.Pair<a.Pair.A,a.Pair.B>",
          method("a.Pair.Pair").getFunctionType());
      assertType("() -> a.Pair.A", method("a.Pair.first").getFunctionType());
      assertType("(int, java.lang.String...) -> a.Names", method("a.Names.Names").getFunctionType());
      assertTrue(method("a.Names.Names").isIsElliptic());
      assertType("() -> java.lang.String[]", method("a.Names.names").getFunctionType());
    }

    /**
     * Explicitly declared accessors and canonical constructors replace the implicit ones; a
     * compact constructor declares the canonical constructor (JLS 8.10.3, 8.10.4).
     */
    @Test
    void explicitAccessorsAndCanonicalConstructors() {
      SymbolTables.fromSource("""
          package a;
          record Explicit(int x, String name) {
            Explicit(int x, String name) { this.x = x; this.name = name.trim(); }
            Explicit(int x) { this(x, ""); }
            public int x() { return x; }
          }
          record Compact(int x) {
            public Compact { if (x < 0) throw new IllegalArgumentException(); }
          }
          record Names(String... names) {
            Names(String... names) { this.names = names.clone(); }
          }
          """);

      assertEquals(List.of("(int, java.lang.String) -> a.Explicit", "int -> a.Explicit"),
          methods("a.Explicit.Explicit").stream()
              .map(m -> m.getFunctionType().printFullName()).toList());
      assertEquals(1, methods("a.Explicit.x").size(), "accessors of x");
      assertTrue(method("a.Explicit.x").isPresentAstNode(), "declared accessor of x");
      assertFalse(method("a.Explicit.name").isPresentAstNode(), "implicit accessor of name");

      JavaMethodSymbol compact = method("a.Compact.Compact");
      assertType("int -> a.Compact", compact.getFunctionType());
      assertTrue(compact.isIsPublic(), "access modifier of the compact constructor");

      assertEquals(1, methods("a.Names.Names").size(), "canonical constructors of Names");
    }

    /**
     * Records have the methods equals, hashCode, and toString, unless they are declared
     * explicitly (JLS 8.10.3).
     */
    @Test
    void implicitObjectMethodsOfRecord() {
      SymbolTables.fromSource("""
          package a;
          record Implicit(int x) { }
          record Explicit(int x) {
            public String toString() { return "explicit"; }
            public boolean equals(Object other) { return false; }
            public boolean equals(Explicit other) { return true; }
          }
          """);

      assertType("java.lang.Object -> boolean", method("a.Implicit.equals").getFunctionType());
      assertType("() -> int", method("a.Implicit.hashCode").getFunctionType());
      assertType("() -> java.lang.String", method("a.Implicit.toString").getFunctionType());
      assertTrue(method("a.Implicit.toString").isIsPublic());

      assertTrue(method("a.Explicit.toString").isPresentAstNode(), "declared toString");
      assertEquals(2, methods("a.Explicit.equals").size(), "declared equals methods only");
      assertFalse(method("a.Explicit.hashCode").isPresentAstNode(), "implicit hashCode");
    }

    @Test
    void explicitMembersOfRecord() {
      SymbolTables.fromSource("""
          package a;
          record Range(int lo, int hi) {
            static final Range EMPTY = new Range(0, 0);
            Range {
              if (lo > hi) throw new IllegalArgumentException();
            }
            int length() { return hi - lo; }
          }
          """);

      assertType("a.Range", field("a.Range.EMPTY").getType());
      assertType("() -> int", method("a.Range.length").getFunctionType());
    }

    @Test
    void annotationElements() {
      SymbolTables.fromSource("""
          package a;
          @interface Info {
            String value();
            int priority() default 1;
            String[] tags() default {};
            Class<?> type() default Object.class;
          }
          """);

      assertType("java.lang.String", method("a.Info.value").getType());
      assertType("int", method("a.Info.priority").getType());
      assertType("java.lang.String[]", method("a.Info.tags").getType());
      assertType("java.lang.Class<?>", method("a.Info.type").getType());
    }
  }

  private static void assertTypes(List<String> expected, List<SymTypeExpression> actual) {
    assertEquals(expected, actual.stream().map(SymTypeExpression::printFullName).toList());
  }

  @Test
  void typeSymbolIsAlsoResolvableAsOOType() {
    SymbolTables.fromSource("package a; class C { }");
    OOTypeSymbol ooType = type("a.C");
    assertSame(typeDeclaration("a.C"), ooType);
  }
}
