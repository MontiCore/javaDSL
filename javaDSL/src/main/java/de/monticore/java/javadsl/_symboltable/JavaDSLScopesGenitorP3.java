package de.monticore.java.javadsl._symboltable;

import de.monticore.java.javadsl._ast.ASTEnhancedForControlFormalParameter;
import de.monticore.java.javadsl._ast.ASTLocalVariableDeclaration;
import de.monticore.java.javadsl._ast.ASTTryLocalVariableDeclaration;
import de.monticore.java.javadsl._visitor.JavaDSLVisitor2;
import de.monticore.statements.mccommonstatements._ast.ASTEnhancedForControl;
import de.monticore.statements.mccommonstatements._ast.ASTForControl;
import de.monticore.statements.mccommonstatements._ast.ASTForStatement;
import de.monticore.statements.mcvardeclarationstatements._ast.ASTDeclarator;
import de.monticore.statements.mcvardeclarationstatements._ast.ASTSimpleInit;
import de.monticore.statements.mcvardeclarationstatements._ast.ASTVariableDeclarator;
import de.monticore.statements.mcvardeclarationstatements._ast.ASTVariableInit;
import de.monticore.symbols.oosymbols._symboltable.FieldSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.monticore.types3.TypeCheck3;
import de.se_rwth.commons.logging.Log;

public class JavaDSLScopesGenitorP3 implements JavaDSLVisitor2 {
  
  public JavaDSLScopesGenitorP3() {
  }
  
  @Override
  public void endVisit(ASTLocalVariableDeclaration node) {
    if (node.isVar()) {
      // we assume that there is just a single VariableDeclarator, as a CoCo prevents the
      // declaration of multiple variables with a single var
      ASTVariableDeclarator declarator = node.getVariableDeclarator(0);
      ASTVariableInit variableInit = declarator.getVariableInit();
      
      SymTypeExpression targetType = SymTypeExpressionFactory.createObscureType();
      if (variableInit instanceof ASTSimpleInit simpleInit) {
        targetType = TypeCheck3.typeOf(simpleInit.getExpression());
      }
      else {
        Log.error("0x7A001: Unsupported ASTVariableInit type");
      }
      
      if (targetType.isObscureType()) {
        Log.error("0x7A002: Could not determine type of 'var' variable");
      }
      declarator.getDeclarator().getSymbol().setType(targetType);
    }
  }
  
  @Override
  public void endVisit(ASTEnhancedForControlFormalParameter node) {
    if (node.isVar()) {
      ASTDeclarator declarator = node.getDeclarator();
      FieldSymbol symbol = declarator.getSymbol();
      IJavaDSLScope enclosingScope = node.getEnclosingScope();
      if (enclosingScope.getAstNode() instanceof ASTForStatement forStatement) {
        ASTForControl forControl = forStatement.getForControl();
        
        if (forControl instanceof ASTEnhancedForControl enhancedForControl) {
          SymTypeExpression forExprType = TypeCheck3.typeOf(enhancedForControl.getExpression());
          symbol.setType(forExprType);
        }
        else {
          Log.error("0x7A013: Only EnhancedForControl support the 'var' keyword",
              node.get_SourcePositionStart());
        }
      }
      else {
        Log.error("0x7A014: Encountered FormalParameter with 'var' in unsupported position",
            node.get_SourcePositionStart());
      }
    }
  }
  
  @Override
  public void endVisit(ASTTryLocalVariableDeclaration node) {
    if (node.isVar()) {
      FieldSymbol symbol = node.getDeclaratorId().getSymbol();
      SymTypeExpression expressionType = TypeCheck3.typeOf(node.getExpression());
      symbol.setType(expressionType);
    }
  }
}
