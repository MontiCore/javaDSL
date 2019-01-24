/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.expressions;

import java.util.Collection;

import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.expressions.shiftexpressions._ast.ASTQualifiedNameExpression;
import de.monticore.expressions.shiftexpressions._cocos.ShiftExpressionsASTQualifiedNameExpressionCoCo;
import de.monticore.symboltable.Scope;
import de.se_rwth.commons.logging.Log;

public class QualifiedNameValid implements ShiftExpressionsASTQualifiedNameExpressionCoCo {

  HCJavaDSLTypeResolver typeResolver;

  public QualifiedNameValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  @Override
  public void check(ASTQualifiedNameExpression node) {
    String name = node.getName();
    Scope scope = node.getEnclosingScope();
    typeResolver.handle(node.getExpression());
    if(!typeResolver.getResult().isPresent()) {
      // expression could be a package. try to resolve the name
      Collection<JavaTypeSymbol> resolvedTypes = scope.resolveMany(name, JavaTypeSymbol.KIND);
      if(resolvedTypes.size() != 1) {
        Log.error("symbol " + name + " not found.", node.get_SourcePositionStart());
      }
    }
    else {
      typeResolver.handle(node);
      if(!typeResolver.getResult().isPresent()) {
        Log.error("symbol " + name + " not found.", node.get_SourcePositionStart());
      }
    }
  }

}
