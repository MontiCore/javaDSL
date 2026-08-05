package de.monticore.java.javadsl._symboltable;

import de.monticore.java.javadsl._visitor.JavaDSLVisitor2;
import de.monticore.statements.mcvardeclarationstatements._ast.ASTLocalVariableDeclaration;
import de.monticore.statements.mcvardeclarationstatements._ast.ASTVariableDeclarator;
import de.monticore.statements.mcvardeclarationstatements._symboltable.MCVarDeclarationStatementsSymTabCompletion;

public class JavaDSLMCVarDeclarationStatementsSymTabCompletion
    extends MCVarDeclarationStatementsSymTabCompletion implements JavaDSLVisitor2 {
  
  @Override
  public void endVisit(ASTLocalVariableDeclaration node) {
    if (!node.isEmptyVariableDeclarators()) {
      if (node instanceof de.monticore.java.javadsl._ast.ASTLocalVariableDeclaration javaVarDec) {
        if (javaVarDec.isVar()) {
          return;
        }
      }
      ASTVariableDeclarator declarator = node.getVariableDeclarator(0);
      if (declarator.getDeclarator().getSymbol().getType() == null) {
        super.endVisit(node);
      }
    }
  }
}
