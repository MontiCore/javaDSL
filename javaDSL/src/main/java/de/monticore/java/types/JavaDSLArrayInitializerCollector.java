/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.types;

import java.util.ArrayList;
import java.util.List;

import de.monticore.java.javadsl._ast.ASTArrayInitializer;
import de.monticore.java.javadsl._ast.ASTVariableInitializer;
import de.monticore.java.javadsl._visitor.JavaDSLVisitor;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;

/**
 * TODO
 *
 */
public class JavaDSLArrayInitializerCollector implements JavaDSLVisitor {

  public JavaDSLArrayInitializerCollector getRealThis() {
    return this;
  }

  private List<ASTArrayInitializer> arrayInitializerList = new ArrayList<ASTArrayInitializer>();

  private List<ASTExpression> astExpressionList = new ArrayList<>();

  public List<ASTArrayInitializer> getArrayInitializerList() {
    return this.arrayInitializerList;
  }

  public void visit(List<ASTVariableInitializer> node){
    for (ASTVariableInitializer variableInitializer : node){
      variableInitializer.accept(this);
      handle(variableInitializer);
    }
  }

  @Override
  public void visit(ASTArrayInitializer node) {
    arrayInitializerList.add(node);
  }

  @Override
  public void visit(ASTExpression node) {
    astExpressionList.add(node);
  }

  @Override
  public void handle(ASTExpression node){
    // Don't traverse childs
  }
}
