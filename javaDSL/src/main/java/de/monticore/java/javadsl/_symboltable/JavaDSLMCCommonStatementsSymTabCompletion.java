package de.monticore.java.javadsl._symboltable;

import de.monticore.java.javadsl._ast.ASTEnhancedForControlFormalParameter;
import de.monticore.java.javadsl._visitor.JavaDSLVisitor2;
import de.monticore.statements.mccommonstatements._symboltable.MCCommonStatementsSymTabCompletion;
import de.monticore.symbols.oosymbols._symboltable.FieldSymbol;
import de.monticore.types3.TypeCheck3;

public class JavaDSLMCCommonStatementsSymTabCompletion extends MCCommonStatementsSymTabCompletion
    implements JavaDSLVisitor2 {
  
  @Override
  public void endVisit(ASTEnhancedForControlFormalParameter node) {
    if (!node.isVar()) {
      FieldSymbol symbol = node.getDeclarator().getSymbol();
      symbol.setType(TypeCheck3.symTypeFromAST(node.getMCType()));
    }
  }
}
