/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.expressions;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.expressions.javaclassexpressions._ast.ASTGenericInvocationExpression;
import de.monticore.expressions.javaclassexpressions._ast.ASTPrimaryGenericInvocationExpression;
import de.monticore.expressions.javaclassexpressions._cocos.JavaClassExpressionsASTGenericInvocationExpressionCoCo;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 */
public class MethodGenericInvocationValid implements JavaClassExpressionsASTGenericInvocationExpressionCoCo{
  HCJavaDSLTypeResolver typeResolver;

  public MethodGenericInvocationValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  // //JLS3 15.12.1-1, JLS3 15.12.1-2, JLS3 15.12.1-3, JLS3 15.12.1-4, JLS3 15.12.1-5
  @Override
  public void check(ASTGenericInvocationExpression node) {
    List<JavaTypeSymbolReference> actualArguments = new ArrayList<>();
    List<JavaTypeSymbolReference> typeArguments = new ArrayList<>();
    String methodName = "";
    ASTPrimaryGenericInvocationExpression genericInvocation = node.getPrimaryGenericInvocationExpression();
    genericInvocation.getExtTypeArguments().accept(typeResolver);
    if (typeResolver.getResult().isPresent()) {
      typeArguments.add(typeResolver.getResult().get());
    }
    else {
      Log.error("0xA0554 type argument", node.get_SourcePositionStart());
    }

    for (ASTExpression expression : genericInvocation.getGenericInvocationSuffix()
            .getArguments().getExpressionList()) {
      expression.accept(typeResolver);
      if (typeResolver.getResult().isPresent()) {
        actualArguments.add(typeResolver.getResult().get());
      }
      else {
        Log.error("0xA0555 argument", node.get_SourcePositionStart());
      }
    }
    if (genericInvocation.getGenericInvocationSuffix().isPresentName()) {
      methodName = genericInvocation.getGenericInvocationSuffix().getName();
    }
    if (node.getExpression() instanceof ASTNameExpression) {
      ASTNameExpression primaryExpression = (ASTNameExpression) node.getExpression();
      String symbolName = primaryExpression.getName();
      typeResolver.handle(primaryExpression);
      JavaTypeSymbolReference type = typeResolver.getResult().get();
      if (node.getEnclosingScope().resolve(type.getName(), JavaTypeSymbol.KIND)
              .isPresent()) {
        // for class method
        JavaTypeSymbol expSymbol = (JavaTypeSymbol) node.getEnclosingScope()
                .resolve(type.getName(), JavaTypeSymbol.KIND).get();
        if (type.getName().endsWith(symbolName) || type.getName().equals(symbolName)) {
          HashMap<JavaMethodSymbol, JavaTypeSymbolReference> methodSymbols = JavaDSLHelper
                  .resolveManyInSuperType(methodName, true, null, expSymbol,
                          typeArguments, actualArguments);
          if (methodSymbols.isEmpty()) {
            Log.error("0xA0556 method '" + methodName + "' is not found.",
                    node.get_SourcePositionStart());
          }
          if (methodSymbols.size() > 1) {
            Log.error("0xA0557 the invocation of method '" + methodName + "' is ambiguous.",
                    node.get_SourcePositionStart());
          }
        }
        else {
          HashMap<JavaMethodSymbol, JavaTypeSymbolReference> methodSymbols = JavaDSLHelper
                  .resolveManyInSuperType(methodName, false, type, expSymbol,
                          typeArguments, actualArguments);
          if (methodSymbols.isEmpty()) {
            Log.error("0xA0558 method '" + methodName + "' is not found.",
                    node.get_SourcePositionStart());
          }
          if (methodSymbols.size() > 1) {
            Log.error("0xA0559 the invocation of method '" + methodName + "' is ambiguous",
                    node.get_SourcePositionStart());
          }
        }
      }
      else {
        Log.error("the symbol '" + primaryExpression.getName() + "' "
                + "is not found.", node.get_SourcePositionStart());
      }
    }
    else {
      typeResolver.handle(node.getExpression());
      if (typeResolver.getResult().isPresent()) {
        JavaTypeSymbolReference expType = typeResolver.getResult().get();
        if (node.getEnclosingScope().resolve(expType.getName(), JavaTypeSymbol.KIND)
                .isPresent()) {
          JavaTypeSymbol expSymbol = (JavaTypeSymbol) node.getEnclosingScope()
                  .resolve(expType.getName(), JavaTypeSymbol.KIND).get();
          HashMap<JavaMethodSymbol, JavaTypeSymbolReference> methodSymbols = JavaDSLHelper
                  .resolveManyInSuperType(methodName, false, expType, expSymbol,
                          typeArguments, actualArguments);
          if (methodSymbols.isEmpty()) {
            Log.error("0xA0560 method '" + methodName + "' is not found.",
                    node.get_SourcePositionStart());
          }
          if (methodSymbols.size() > 1) {
            Log.error("0xA0561 the invocation of method '" + methodName + "' is ambiguous",
                    node.get_SourcePositionStart());
          }
        }
      }
    }
  }
}
