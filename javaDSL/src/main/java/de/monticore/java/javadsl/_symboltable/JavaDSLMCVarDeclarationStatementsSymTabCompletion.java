package de.monticore.java.javadsl._symboltable;

import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTLocalVariableDeclaration;
import de.monticore.java.javadsl._visitor.JavaDSLVisitor2;
import de.monticore.statements.mcvardeclarationstatements._ast.ASTSimpleInit;
import de.monticore.statements.mcvardeclarationstatements._ast.ASTVariableDeclarator;
import de.monticore.statements.mcvardeclarationstatements._ast.ASTVariableInit;
import de.monticore.statements.mcvardeclarationstatements._symboltable.MCVarDeclarationStatementsSymTabCompletion;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.monticore.types3.TypeCheck3;
import de.se_rwth.commons.logging.Log;

public class JavaDSLMCVarDeclarationStatementsSymTabCompletion
    extends MCVarDeclarationStatementsSymTabCompletion implements JavaDSLVisitor2 {
  
  @Override
  public void endVisit(ASTLocalVariableDeclaration node) {
    if (node.isVar()) {
      // we assume that there is just a single VariableDeclarator, as a CoCo prevents the
      // declaration of multiple variables with a single var
      ASTVariableDeclarator declarator = node.getVariableDeclarator(0);
      ASTVariableInit variableInit = declarator.getVariableInit();
      
      SymTypeExpression targetType = SymTypeExpressionFactory.createObscureType();
      if (JavaDSLMill.typeDispatcher().isMCVarDeclarationStatementsASTSimpleInit(variableInit)) {
        ASTSimpleInit simpleInit =
            JavaDSLMill.typeDispatcher().asMCVarDeclarationStatementsASTSimpleInit(variableInit);
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
    else {
      super.endVisit(node);
    }
  }
  
  @Override
  public void endVisit(
      de.monticore.statements.mcvardeclarationstatements._ast.ASTLocalVariableDeclaration node) {
    if (!node.isEmptyVariableDeclarators()) {
      ASTVariableDeclarator declarator = node.getVariableDeclarator(0);
      if (declarator.getDeclarator().getSymbol().getType() == null) {
        super.endVisit(node);
      }
    }
  }
}
