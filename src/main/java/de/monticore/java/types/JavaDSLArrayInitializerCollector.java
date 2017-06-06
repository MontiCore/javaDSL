/*
 * ******************************************************************************
 * MontiCore Language Workbench
 * Copyright (c) 2015, MontiCore, All rights reserved.
 *
 * This project is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this project. If not, see <http://www.gnu.org/licenses/>.
 * ******************************************************************************
 */
package de.monticore.java.types;

import de.monticore.java.javadsl._ast.ASTArrayInitializer;
import de.monticore.java.javadsl._ast.ASTExpression;
import de.monticore.java.javadsl._ast.ASTVariableInitializer;
import de.monticore.java.javadsl._visitor.JavaDSLVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 *
 * @author (last commit) $$Author$$
 * @version $$Revision$$, $$Date$$
 * @since TODO
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
  public void traverse(ASTExpression node){
    // Don't traverse childs
  }
}
