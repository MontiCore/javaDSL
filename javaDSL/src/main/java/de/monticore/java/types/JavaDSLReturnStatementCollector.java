/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.types;

import java.util.ArrayList;
import java.util.List;

import de.monticore.java.javadsl._ast.ASTReturnStatement;
import de.monticore.java.javadsl._visitor.JavaDSLVisitor;

/**
 * TODO
 *
 */
public class JavaDSLReturnStatementCollector implements JavaDSLVisitor {

  public JavaDSLReturnStatementCollector getRealThis() {
    return this;
  }

  private List<ASTReturnStatement> returnStatementList = new ArrayList<ASTReturnStatement>();

  public List<ASTReturnStatement> getReturnStatementList() {
    return this.returnStatementList;
  }

  @Override
  public void visit(ASTReturnStatement node) {
      returnStatementList.add(node);
  }
}
