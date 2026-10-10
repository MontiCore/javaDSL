package de.monticore.java.javadsl.types3;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.javaclassexpressions._ast.ASTPrimarySuperExpression;
import de.monticore.expressions.javaclassexpressions._ast.ASTPrimaryThisExpression;
import de.monticore.expressions.javaclassexpressions.types3.JavaClassExpressionsTypeVisitor;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.oosymbols._symboltable.MethodSymbol;
import de.monticore.symbols.oosymbols._symboltable.OOTypeSymbol;
import de.monticore.symboltable.IScope;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.monticore.types3.util.TypeContextCalculator;
import de.se_rwth.commons.logging.Log;

import java.util.Optional;

/**
 * Extends the type visitor of JavaClassExpressions by the unqualified {@code this} and
 * {@code super}, which MontiCore does not support (yet).
 *
 * <p>Limitation: a static context is only detected within static methods, not within static
 * initializers and initializers of static fields.</p>
 */
public class JavaDSLJavaClassExpressionsTypeVisitor extends JavaClassExpressionsTypeVisitor {

  /**
   * The type of {@code this} is the type of the innermost enclosing class (JLS 15.8.3).
   */
  @Override
  public void endVisit(ASTPrimaryThisExpression expr) {
    SymTypeExpression result = getEnclosingTypeOfInstanceContext(expr, "this")
        .map(SymTypeExpressionFactory::createFromSymbol)
        .orElseGet(SymTypeExpressionFactory::createObscureType);
    getType4Ast().setTypeOfExpression(expr, result);
  }

  /**
   * {@code super} accesses the members of the direct super class of the innermost enclosing
   * class (JLS 15.11.2, 15.12.1).
   */
  @Override
  public void endVisit(ASTPrimarySuperExpression expr) {
    SymTypeExpression result = getEnclosingTypeOfInstanceContext(expr, "super")
        .flatMap(type -> getSuperClass(expr, type))
        .orElseGet(SymTypeExpressionFactory::createObscureType);
    getType4Ast().setTypeOfExpression(expr, result);
  }

  protected Optional<SymTypeExpression> getSuperClass(ASTExpression expr, TypeSymbol type) {
    if (type instanceof OOTypeSymbol ooType) {
      if (ooType.isIsInterface()) {
        Log.error("0x7A021 'super' can not be used within the interface " + type.getFullName(),
            expr.get_SourcePositionStart(), expr.get_SourcePositionEnd());
        return Optional.empty();
      }
      if (ooType.isPresentSuperClass()) {
        return Optional.of(ooType.getSuperClass());
      }
    }
    return resolveTypeOrLogError("java.lang.Object")
        .map(SymTypeExpressionFactory::createTypeObject);
  }

  /**
   * The innermost enclosing type of the expression, if the expression is not in a static
   * context; otherwise, an error is logged.
   */
  protected Optional<TypeSymbol> getEnclosingTypeOfInstanceContext(ASTExpression expr,
      String keyword) {
    Optional<TypeSymbol> enclosingType =
        TypeContextCalculator.getEnclosingType(expr.getEnclosingScope());
    if (enclosingType.isEmpty()) {
      Log.error("0x7A019 '" + keyword + "' can only be used within a type",
          expr.get_SourcePositionStart(), expr.get_SourcePositionEnd());
      return Optional.empty();
    }
    if (isInStaticMethod(expr.getEnclosingScope(), enclosingType.get())) {
      Log.error("0x7A020 '" + keyword + "' can not be used in a static context",
          expr.get_SourcePositionStart(), expr.get_SourcePositionEnd());
      return Optional.empty();
    }
    return enclosingType;
  }

  protected boolean isInStaticMethod(IScope scope, TypeSymbol enclosingType) {
    for (; scope != null && scope != enclosingType.getSpannedScope();
         scope = scope.getEnclosingScope()) {
      if (scope.isPresentSpanningSymbol()
          && scope.getSpanningSymbol() instanceof MethodSymbol method
          && method.isIsStatic()) {
        return true;
      }
    }
    return false;
  }
}
