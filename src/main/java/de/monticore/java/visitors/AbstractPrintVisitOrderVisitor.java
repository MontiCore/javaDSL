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
package de.monticore.java.visitors;

import java.io.PrintStream;

import de.monticore.ast.ASTNode;

public class AbstractPrintVisitOrderVisitor {

  private final PrintStream stream;

  int indent = 0;

  public AbstractPrintVisitOrderVisitor(final PrintStream stream) {
    this.stream = stream;
  }

  public AbstractPrintVisitOrderVisitor() {
    this(System.out);
  }

  private static String spaces(final int indent) {
    String s = "";
    for (int i = 0; i < indent; i++) {
      s += "|   ";
    }
    return s;
  }

  public void visit(ASTNode node) {
    stream.println(spaces(indent) + "/"
        + node.getClass().getSimpleName());
    indent++;
  }

  public void endVisit(ASTNode node) {
    indent--;
    stream.println(spaces(indent) + "\\"
        + node.getClass().getSimpleName());
  }

  protected void print(String s) {
    stream.println(spaces(indent) + s);
  }
}
