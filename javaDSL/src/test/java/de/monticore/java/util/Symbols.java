/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.util;

import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._symboltable.IJavaDSLScope;
import de.monticore.java.javadsl._symboltable.TypeDeclarationSymbol;
import de.monticore.javalight._symboltable.JavaMethodSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeVarSymbol;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symbols.oosymbols._symboltable.FieldSymbol;
import de.monticore.symbols.oosymbols._symboltable.MethodSymbol;
import de.monticore.symbols.oosymbols._symboltable.OOTypeSymbol;
import de.monticore.symboltable.IScopeSpanningSymbol;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static de.monticore.java.JavaDSLAssertions.assertPresent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Looks up symbols of the current symbol table (see {@link SymbolTables}) by their fully qualified
 * name, e.g., {@code field("p.A.x")}. Local variables can not be resolved from the outside of
 * their method, hence they are searched within the scope tree of their method.
 */
public final class Symbols {

  private Symbols() {
  }

  /** A type declared in one of the models (class, interface, enum, record, annotation). */
  public static TypeDeclarationSymbol typeDeclaration(String fullName) {
    return assertPresent(JavaDSLMill.globalScope().resolveTypeDeclaration(fullName),
        "type declaration " + fullName);
  }

  /** A type declared in a model or loaded via class2mc. */
  public static OOTypeSymbol type(String fullName) {
    return assertPresent(JavaDSLMill.globalScope().resolveOOType(fullName), "type " + fullName);
  }

  public static TypeVarSymbol typeVar(String fullName) {
    return assertPresent(JavaDSLMill.globalScope().resolveTypeVar(fullName),
        "type variable " + fullName);
  }

  public static FieldSymbol field(String fullName) {
    return assertPresent(JavaDSLMill.globalScope().resolveField(fullName), "field " + fullName);
  }

  /** The only method with the given name; use {@link #methods(String)} for overloaded methods. */
  public static JavaMethodSymbol method(String fullName) {
    List<MethodSymbol> methods = methods(fullName);
    assertEquals(1, methods.size(), () -> "number of methods named " + fullName);
    return assertInstanceOf(JavaMethodSymbol.class, methods.get(0));
  }

  /**
   * All methods (including constructors) with the given name that are declared in the type, i.e.,
   * without inherited methods, ordered by source position.
   */
  public static List<MethodSymbol> methods(String fullName) {
    return JavaDSLMill.globalScope().resolveMethodMany(fullName).stream()
        .filter(m -> m.getFullName().equals(fullName))
        .sorted(Comparator.comparing(m -> m.getSourcePosition()))
        .toList();
  }

  /**
   * The only variable (local variable, parameter, lambda parameter, ...) with the given name
   * within the scope spanned by the symbol, e.g., a method, or in one of its sub scopes.
   */
  public static VariableSymbol localVariable(IScopeSpanningSymbol owner, String name) {
    List<VariableSymbol> variables = localVariables(owner, name);
    assertEquals(1, variables.size(),
        () -> "number of variables named " + name + " in " + owner.getFullName());
    return variables.get(0);
  }

  /** All variables with the given name within the scope spanned by the symbol. */
  public static List<VariableSymbol> localVariables(IScopeSpanningSymbol owner, String name) {
    return localVariables((IJavaDSLScope) owner.getSpannedScope(), name);
  }

  /** All variables with the given name within the scope or one of its sub scopes. */
  public static List<VariableSymbol> localVariables(IJavaDSLScope scope, String name) {
    List<VariableSymbol> result = new ArrayList<>();
    collectVariables(scope, name, result);
    return result;
  }

  private static void collectVariables(IJavaDSLScope scope, String name,
      List<VariableSymbol> result) {
    Stream.of(scope.getLocalVariableSymbols(), scope.getLocalFieldSymbols(),
            scope.getLocalEnumConstantDeclarationSymbols())
        .flatMap(List::stream)
        .filter(variable -> variable.getName().equals(name))
        .forEach(result::add);
    scope.getSubScopes().forEach(subScope -> collectVariables(subScope, name, result));
  }
}
