/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.expressions;

import de.monticore.mcexpressions._ast.ASTQualifiedNameExpression;
import de.monticore.mcexpressions._cocos.MCExpressionsASTQualifiedNameExpressionCoCo;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.symboltable.Scope;
import de.se_rwth.commons.logging.Log;

import java.util.Collection;

public class QualifiedNameValid implements MCExpressionsASTQualifiedNameExpressionCoCo {

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
