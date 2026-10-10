package de.monticore.java.javadsl._symboltable;

import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.javalight._symboltable.JavaMethodSymbol;
import de.monticore.symbols.oosymbols._symboltable.FieldSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;

/**
 * Creates the symbols of members that are not declared in the model, but implicitly exist in
 * Java, e.g., default constructors (JLS 8.8.9) and the accessors of records (JLS 8.10.3). These
 * symbols have no AST node.
 */
final class ImplicitMembers {

  private ImplicitMembers() {
  }

  /** Adds a method without parameters to the type. */
  static JavaMethodSymbol addMethod(TypeDeclarationSymbol type, String name,
      SymTypeExpression returnType) {
    IJavaDSLScope spannedScope = JavaDSLMill.scope();
    spannedScope.setShadowing(true);
    spannedScope.setExportingSymbols(false);
    spannedScope.setEnclosingScope(type.getSpannedScope());
    JavaMethodSymbol method = JavaDSLMill.javaMethodSymbolBuilder()
        .setName(name)
        .setType(returnType)
        .setSpannedScope(spannedScope)
        .build();
    spannedScope.setSpanningSymbol(method);
    type.getSpannedScope().add(method);
    return method;
  }

  /** Adds a constructor without parameters to the type. */
  static JavaMethodSymbol addConstructor(TypeDeclarationSymbol type) {
    JavaMethodSymbol constructor =
        addMethod(type, type.getName(), SymTypeExpressionFactory.createFromSymbol(type));
    constructor.setIsConstructor(true);
    return constructor;
  }

  /** Adds a parameter to the method; parameters must be added in their order. */
  static void addParameter(JavaMethodSymbol method, String name, SymTypeExpression type) {
    FieldSymbol parameter = JavaDSLMill.fieldSymbolBuilder()
        .setName(name)
        .setType(type)
        .build();
    ((IJavaDSLScope) method.getSpannedScope()).add(parameter);
  }
}
