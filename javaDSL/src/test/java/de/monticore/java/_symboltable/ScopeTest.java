/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java._symboltable;

import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._symboltable.EnumConstantDeclarationSymbol;
import de.monticore.java.javadsl._symboltable.IJavaDSLScope;
import de.monticore.java.util.SymbolTables;
import de.monticore.javalight._symboltable.JavaMethodSymbol;
import de.monticore.runtime.junit.TestWithMCLanguage;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static de.monticore.java.JavaDSLAssertions.assertPresent;
import static de.monticore.java.JavaDSLAssertions.assertType;
import static de.monticore.java.util.Symbols.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * The scope tree and the resolution of names from within the scope tree, i.e., which declaration
 * a simple name refers to at a certain position (JLS 6.3, 6.4).
 */
@TestWithMCLanguage(JavaDSLMill.class)
public class ScopeTest {

  @Test
  void scopeTree() {
    SymbolTables.fromSource("""
        package a;
        class C {
          int field;
          void m(int parameter) {
            int local;
            {
              int inBlock;
            }
            for (int i = 0; i < 1; i++) {
              int inLoop;
            }
          }
        }
        """);

    IJavaDSLScope classScope = (IJavaDSLScope) typeDeclaration("a.C").getSpannedScope();
    JavaMethodSymbol m = method("a.C.m");
    assertSame(classScope, m.getEnclosingScope());
    assertSame(classScope, field("a.C.field").getEnclosingScope());

    IJavaDSLScope methodScope = (IJavaDSLScope) m.getSpannedScope();
    assertSame(methodScope, localVariable(m, "parameter").getEnclosingScope());
    IJavaDSLScope bodyScope = (IJavaDSLScope) localVariable(m, "local").getEnclosingScope();
    assertSame(methodScope, bodyScope.getEnclosingScope());
    assertSame(bodyScope, localVariable(m, "inBlock").getEnclosingScope().getEnclosingScope());
    IJavaDSLScope forScope = (IJavaDSLScope) localVariable(m, "i").getEnclosingScope();
    assertSame(bodyScope, forScope.getEnclosingScope());
    assertSame(forScope, localVariable(m, "inLoop").getEnclosingScope().getEnclosingScope());
  }

  @Test
  void variablesOfInitializerBlocksAreNoFields() {
    SymbolTables.fromSource("""
        package a;
        class C {
          int field;
          static { int inStaticInitializer = 0; }
          { String field = ""; }
        }
        """);

    IJavaDSLScope classScope = (IJavaDSLScope) typeDeclaration("a.C").getSpannedScope();
    assertEquals(List.of("field"), classScope.getLocalFieldSymbols().stream()
        .map(VariableSymbol::getName).toList());
    assertType("int", field("a.C.field").getType());
    assertEquals(1, localVariables(classScope, "inStaticInitializer").size());
    assertEquals(List.of("int", "java.lang.String"), localVariables(classScope, "field").stream()
        .map(v -> v.getType().printFullName()).toList());
  }

  @Test
  void localVariablesAreNotVisibleOutsideOfTheirBlock() {
    SymbolTables.fromSource("""
        package a;
        class C {
          void m() {
            int outer;
            { int inner; }
            for (int i = 0; i < 1; i++) { int inLoop; }
          }
        }
        """);

    IJavaDSLScope bodyScope =
        (IJavaDSLScope) localVariable(method("a.C.m"), "outer").getEnclosingScope();
    assertPresent(bodyScope.resolveVariable("outer"), "outer");
    assertTrue(bodyScope.resolveVariable("inner").isEmpty(), "inner is not visible");
    assertTrue(bodyScope.resolveVariable("i").isEmpty(), "loop variable is not visible");
    assertTrue(bodyScope.resolveVariable("inLoop").isEmpty(), "inLoop is not visible");
  }

  @Test
  void localVariablesAndParametersAreNotVisibleOutsideOfTheMethod() {
    SymbolTables.fromSource("""
        package a;
        class C {
          void m(int parameter) { int local; }
        }
        """);

    IJavaDSLScope classScope = (IJavaDSLScope) typeDeclaration("a.C").getSpannedScope();
    assertTrue(classScope.resolveVariable("parameter").isEmpty(), "parameter");
    assertTrue(classScope.resolveVariable("local").isEmpty(), "local");
    assertTrue(classScope.resolveField("a.C.m.local").isEmpty(), "qualified local");
  }

  @Test
  void sameNameInSiblingBlocks() {
    SymbolTables.fromSource("""
        package a;
        class C {
          void m() {
            { int x = 1; }
            { String x = ""; }
            for (int i = 0; ; ) { break; }
            for (long i = 0; ; ) { break; }
          }
        }
        """);

    JavaMethodSymbol m = method("a.C.m");
    assertEquals(List.of("int", "java.lang.String"), localVariables(m, "x").stream()
        .map(v -> v.getType().printFullName()).toList());
    assertEquals(List.of("int", "long"), localVariables(m, "i").stream()
        .map(v -> v.getType().printFullName()).toList());
  }

  @Test
  void localVariableShadowsField() {
    SymbolTables.fromSource("""
        package a;
        class C {
          int x;
          void m(String x) { }
          void n() { long x; }
        }
        """);

    VariableSymbol parameter = localVariable(method("a.C.m"), "x");
    assertSame(parameter, resolveVariableFrom(parameter.getEnclosingScope(), "x"));
    VariableSymbol local = localVariable(method("a.C.n"), "x");
    assertSame(local, resolveVariableFrom(local.getEnclosingScope(), "x"));
    assertType("long", resolveVariableFrom(local.getEnclosingScope(), "x").getType());
  }

  @Test
  void fieldOfInnerClassShadowsFieldOfOuterClass() {
    SymbolTables.fromSource("""
        package a;
        class Outer {
          int x;
          int onlyOuter;
          class Inner {
            String x;
            void m() { int local; }
          }
        }
        """);

    IJavaDSLScope methodBody =
        (IJavaDSLScope) localVariable(method("a.Outer.Inner.m"), "local").getEnclosingScope();
    assertSame(field("a.Outer.Inner.x"), resolveVariableFrom(methodBody, "x"));
    assertSame(field("a.Outer.onlyOuter"), resolveVariableFrom(methodBody, "onlyOuter"));
  }

  @Test
  void fieldsAreVisibleInMethodsAndNestedLambdasAndAnonymousClasses() {
    SymbolTables.fromSource("""
        package a;
        class C {
          int field;
          Runnable lambda = () -> { int inLambda; };
          Runnable anonymous = new Runnable() {
            public void run() { int inAnonymous; }
          };
        }
        """);

    VariableSymbol inLambda = localVariable(typeDeclaration("a.C"), "inLambda");
    assertSame(field("a.C.field"), resolveVariableFrom(inLambda.getEnclosingScope(), "field"));
    VariableSymbol inAnonymous = localVariable(typeDeclaration("a.C"), "inAnonymous");
    assertSame(field("a.C.field"),
        resolveVariableFrom(inAnonymous.getEnclosingScope(), "field"));
  }

  /** A type parameter of a class is visible in its members (JLS 6.3). */
  @Test
  void typeParametersAreVisibleInMembers() {
    SymbolTables.fromSource("""
        package a;
        class C<T> {
          T field;
          <U> void m(T t, U u) { T local; }
        }
        """);

    assertType("a.C.T", field("a.C.field").getType());
    JavaMethodSymbol m = method("a.C.m");
    assertType("a.C.T", localVariable(m, "t").getType());
    assertType("a.C.m.U", localVariable(m, "u").getType());
    assertType("a.C.T", localVariable(m, "local").getType());
  }

  /** A type parameter of a method shadows a type parameter of the class (JLS 6.4.1). */
  @Test
  void typeParameterOfMethodShadowsTypeParameterOfClass() {
    SymbolTables.fromSource("""
        package a;
        class C<T> {
          <T> T m(T t) { return t; }
        }
        """);
    assertType("a.C.m.T", localVariable(method("a.C.m"), "t").getType());
  }

  /**
   * A type parameter T shadows a member type T of the super class, but a member type T declared
   * within the class body shadows the type parameter within the body (JLS 6.4.1).
   */
  @Test
  void typeParameterAndMemberTypeWithSameName() {
    SymbolTables.fromSource("""
        package a;
        class C<T> {
          class T { }
          T field;
        }
        """);

    TypeSymbol memberType = typeDeclaration("a.C.T");
    assertSame(memberType, field("a.C.field").getType().getTypeInfo());
  }

  @Test
  void typeOfFieldRefersToNestedTypeBeforeTopLevelTypeWithSameName() {
    SymbolTables.fromSource("""
        package a;
        class Node { }
        class C {
          class Node { }
          Node field;
        }
        """);
    assertType("a.C.Node", field("a.C.field").getType());
  }

  @Test
  void inheritedNestedTypeIsVisibleInSubClass() {
    SymbolTables.fromSource("""
        package a;
        class Base { public static class Helper { } }
        class C extends Base { Helper helper; }
        """);
    assertType("a.Base.Helper", field("a.C.helper").getType());
  }

  @Test
  void enumConstantBodySpansScope() {
    SymbolTables.fromSource("""
        package a;
        enum E {
          A { int inBody; },
          B;
        }
        """);

    EnumConstantDeclarationSymbol constant =
        assertInstanceOf(EnumConstantDeclarationSymbol.class, field("a.E.A"));
    IJavaDSLScope constantScope = constant.getSpannedScope();
    assertEquals(List.of("inBody"), constantScope.getLocalFieldSymbols().stream()
        .map(VariableSymbol::getName).toList());
  }

  /** Resolves a variable from the scope as a simple name would be resolved at this position. */
  private static VariableSymbol resolveVariableFrom(
      de.monticore.symbols.basicsymbols._symboltable.IBasicSymbolsScope scope, String name) {
    Optional<VariableSymbol> variable = scope.resolveVariable(name);
    return assertPresent(variable, "variable " + name);
  }
}
