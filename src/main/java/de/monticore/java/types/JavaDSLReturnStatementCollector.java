/*
 * ******************************************************************************
 * MontiCore Language Workbench, www.monticore.de
 * Copyright (c) 2017, MontiCore, All rights reserved.
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

import java.util.ArrayList;
import java.util.List;

import de.monticore.java.javadsl._ast.ASTReturnStatement;
import de.monticore.java.javadsl._visitor.JavaDSLVisitor;

/**
 * TODO
 *
 * @author (last commit) $$Author$$
 * @since TODO
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
