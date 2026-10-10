/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.types3;

import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.util.Javac;
import de.monticore.java.util.SymbolTables;
import de.monticore.runtime.junit.MCAssertions;
import de.monticore.runtime.junit.TestWithMCLanguage;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types3.TypeCheck3;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Access of members depending on their access modifiers (JLS 6.6). The expression is placed in
 * the method {@code probe} of the given class.
 */
@TestWithMCLanguage(JavaDSLMill.class)
public class AccessTest {

  /** The classes, each with a method {@code probe(Owner other)}. */
  private static final Map<String, String> CLASSES = Map.of(
      "Owner", """
          package a;
          public class Owner {
            public int pub; protected int prot; int pack; private int priv;
            public static int staticPub; static int staticPack;
            public int pubM() { return 0; }
            protected int protM() { return 0; }
            int packM() { return 0; }
            private int privM() { return 0; }
            public static class PublicNested { public PublicNested() { } }
            static class PackageNested { PackageNested() { } }
            public Api api;
            public Owner() { }
            Owner(int x) { }
            void probe(Owner other) { /*Owner*/ }
            class Inner {
              void probe(Owner other) { /*Inner*/ }
            }
          }
          """,
      "SamePackage", """
          package a;
          class SamePackage {
            void probe(Owner other) { /*SamePackage*/ }
          }
          """,
      "OtherPackage", """
          package b;
          class OtherPackage {
            void probe(a.Owner other) { /*OtherPackage*/ }
          }
          """,
      "Api", """
          package a;
          public interface Api {
            int CONSTANT = 1;
            int call();
          }
          """,
      "SubClass", """
          package b;
          class SubClass extends a.Owner {
            void probe(a.Owner other) { /*SubClass*/ }
          }
          """);

  private static final Map<String, String> PROBE_METHODS = Map.of(
      "Owner", "a.Owner.probe",
      "Inner", "a.Owner.Inner.probe",
      "SamePackage", "a.SamePackage.probe",
      "OtherPackage", "b.OtherPackage.probe",
      "SubClass", "b.SubClass.probe");

  @ParameterizedTest(name = "in {0}: {1}")
  @CsvSource(delimiterString = "=>", textBlock = """
      Owner        => other.priv
      Owner        => other.privM()
      Owner        => priv
      Inner        => other.priv
      Inner        => priv
      Inner        => privM()
      SamePackage  => other.pub
      SamePackage  => other.prot
      SamePackage  => other.pack
      SamePackage  => other.pubM()
      SamePackage  => other.protM()
      SamePackage  => other.packM()
      SamePackage  => Owner.staticPub
      SamePackage  => Owner.staticPack
      SamePackage  => new Owner.PackageNested()
      SamePackage  => new Owner(1)
      OtherPackage => other.pub
      OtherPackage => other.pubM()
      OtherPackage => a.Owner.staticPub
      OtherPackage => new a.Owner()
      OtherPackage => new a.Owner.PublicNested()
      OtherPackage => a.Api.CONSTANT
      OtherPackage => other.api.call()
      SubClass     => pub
      SubClass     => prot
      SubClass     => this.prot
      SubClass     => protM()
      SubClass     => staticPub
      SubClass     => other.pub
      """)
  void accessible(String location, String expression) {
    String[] models = models(location, expression);
    Javac.assertCompiles(models);
    SymTypeExpression type = typeOf(location, models);
    MCAssertions.assertNoFindings();
    assertFalse(type.isObscureType());
  }

  @ParameterizedTest(name = "in {0}: {1}")
  @CsvSource(delimiterString = "=>", textBlock = """
      SamePackage  => other.priv
      SamePackage  => other.privM()
      OtherPackage => other.prot
      OtherPackage => other.pack
      OtherPackage => other.priv
      OtherPackage => other.protM()
      OtherPackage => other.packM()
      OtherPackage => a.Owner.staticPack
      OtherPackage => new a.Owner(1)
      OtherPackage => new a.Owner.PackageNested()
      SubClass     => pack
      SubClass     => priv
      SubClass     => packM()
      SubClass     => other.prot
      SubClass     => other.protM()
      """)
  void notAccessible(String location, String expression) {
    String[] models = models(location, expression);
    Javac.assertDoesNotCompile(models);
    SymTypeExpression type = typeOf(location, models);
    InvalidExpressionTest.assertTypeError(expression, type);
  }

  private static String[] models(String location, String expression) {
    return CLASSES.values().stream()
        .map(model -> model.replace("/*" + location + "*/", "Object __probe = " + expression + ";"))
        .toArray(String[]::new);
  }

  private static SymTypeExpression typeOf(String location, String... models) {
    SymbolTables.fromUncheckedSources(models);
    return TypeCheck3.typeOf(ExpressionProbe.probedExpressionIn(PROBE_METHODS.get(location)));
  }
}
