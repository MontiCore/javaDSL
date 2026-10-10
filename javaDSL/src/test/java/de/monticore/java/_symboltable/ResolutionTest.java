/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java._symboltable;

import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.util.Javac;
import de.monticore.java.util.SymbolTables;
import de.monticore.runtime.junit.MCAssertions;
import de.monticore.runtime.junit.TestWithMCLanguage;
import de.monticore.symbols.oosymbols._symboltable.OOTypeSymbol;
import de.se_rwth.commons.logging.Finding;
import org.junit.jupiter.api.Test;

import static de.monticore.java.JavaDSLAssertions.assertType;
import static de.monticore.java.util.Symbols.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Resolution of type names across compilation units, packages, and imports, as well as of JDK
 * types loaded via class2mc (JLS 6.4.1, 7.5).
 */
@TestWithMCLanguage(JavaDSLMill.class)
public class ResolutionTest {

  @Test
  void typeOfSamePackageInOtherCompilationUnit() {
    SymbolTables.fromSources(
        "package a; class A { B b; }",
        "package a; class B { A a; }");

    assertType("a.B", field("a.A.b").getType());
    assertType("a.A", field("a.B.a").getType());
    assertSame(typeDeclaration("a.B"), field("a.A.b").getType().getTypeInfo());
  }

  @Test
  void typeOfOtherPackage() {
    SymbolTables.fromSources(
        "package a; public class A { }",
        "package a.sub; public class S { }",
        """
            package b;
            import a.A;
            import a.sub.*;
            class B {
              A single;
              S onDemand;
              a.A qualified;
              a.sub.S qualifiedSubPackage;
            }
            """);

    assertType("a.A", field("b.B.single").getType());
    assertType("a.sub.S", field("b.B.onDemand").getType());
    assertType("a.A", field("b.B.qualified").getType());
    assertType("a.sub.S", field("b.B.qualifiedSubPackage").getType());
  }

  @Test
  void importOfNestedType() {
    SymbolTables.fromSources(
        "package a; public class Outer { public static class Nested { } }",
        """
            package b;
            import a.Outer;
            import a.Outer.Nested;
            class B {
              Nested viaSingleTypeImport;
              Outer.Nested viaOuterType;
            }
            """,
        """
            package c;
            import a.Outer.*;
            class C { Nested viaOnDemandImport; }
            """);

    assertType("a.Outer.Nested", field("b.B.viaSingleTypeImport").getType());
    assertType("a.Outer.Nested", field("b.B.viaOuterType").getType());
    assertType("a.Outer.Nested", field("c.C.viaOnDemandImport").getType());
  }

  @Test
  void typesOfJavaLangAreImplicitlyImported() {
    SymbolTables.fromSource("""
        package a;
        class C {
          String s;
          Integer i;
          Object o;
          Thread.State state;
          java.lang.Runnable runnable;
        }
        """);

    assertType("java.lang.String", field("a.C.s").getType());
    assertType("java.lang.Integer", field("a.C.i").getType());
    assertType("java.lang.Object", field("a.C.o").getType());
    assertType("java.lang.Thread.State", field("a.C.state").getType());
    assertType("java.lang.Runnable", field("a.C.runnable").getType());
  }

  @Test
  void jdkTypesAreLoadedViaClass2MC() {
    SymbolTables.fromSource("""
        package a;
        import java.util.*;
        class C { List<String> list; }
        """);

    OOTypeSymbol list = type("java.util.List");
    assertTrue(list.isIsInterface());
    assertEquals(1, list.getTypeParameterList().size());
    assertSame(list, field("a.C.list").getType().getTypeInfo());
  }

  /** A type declared in the same package shadows a type imported on demand (JLS 6.4.1). */
  @Test
  void typeOfSamePackageShadowsOnDemandImport() {
    SymbolTables.fromSources(
        "package a; class List { }",
        """
            package a;
            import java.util.*;
            class C { List list; }
            """);
    assertType("a.List", field("a.C.list").getType());
  }

  /** A single type import shadows a type of the same package (JLS 6.4.1). */
  @Test
  void singleTypeImportShadowsTypeOfSamePackage() {
    SymbolTables.fromSources(
        "package a; class List { }",
        """
            package a;
            import java.util.List;
            class C { List<String> list; }
            """);
    assertType("java.util.List<java.lang.String>", field("a.C.list").getType());
  }

  /** A nested type shadows a type of the same package (JLS 6.4.1). */
  @Test
  void nestedTypeShadowsTypeOfSamePackage() {
    SymbolTables.fromSources(
        "package a; class Entry { }",
        """
            package a;
            class C {
              static class Entry { }
              Entry entry;
            }
            """);
    assertType("a.C.Entry", field("a.C.entry").getType());
  }

  @Test
  void typeOfSamePackageShadowsJavaLang() {
    SymbolTables.fromSources(
        "package a; class Override { }",
        "package a; class C { Override o; }");
    assertType("a.Override", field("a.C.o").getType());
  }

  /** Types with the same simple name in different packages are distinguished by imports. */
  @Test
  void sameSimpleNameInDifferentPackages() {
    SymbolTables.fromSources(
        "package a; public class Node { }",
        "package b; public class Node { }",
        "package c; import a.Node; class C { Node node; b.Node other; }");

    assertType("a.Node", field("c.C.node").getType());
    assertType("b.Node", field("c.C.other").getType());
  }

  @Test
  void inheritedMembersAreResolvableInSubClassOfOtherCompilationUnit() {
    SymbolTables.fromSources(
        "package a; public class Base { protected int inherited; public static class Helper { } }",
        "package b; class Sub extends a.Base { Helper helper; }");

    assertType("a.Base.Helper", field("b.Sub.helper").getType());
  }

  @Test
  void unresolvableTypeIsReported() {
    String model = "package a; class C { Unknown field; }";
    Javac.assertDoesNotCompile(model);
    SymbolTables.fromUncheckedSources(model);

    MCAssertions.assertHasFindings(Finding::isError);
    assertTrue(field("a.C.field").getType().isObscureType());
  }

  @Test
  void typeOfNonImportedPackageIsNotResolvable() {
    String[] models = {"package a; public class A { }", "package b; class B { A a; }"};
    Javac.assertDoesNotCompile(models);
    SymbolTables.fromUncheckedSources(models);

    MCAssertions.assertHasFindings(Finding::isError);
    assertTrue(field("b.B.a").getType().isObscureType());
  }

  @Test
  void privateNestedTypeOfOtherClassIsNotResolvable() {
    String model = """
        package a;
        class A { private static class Hidden { } }
        class B { A.Hidden hidden; }
        """;
    Javac.assertDoesNotCompile(model);
    SymbolTables.fromUncheckedSources(model);

    MCAssertions.assertHasFindings(Finding::isError);
    assertTrue(field("a.B.hidden").getType().isObscureType());
  }
}
