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
package de.monticore.java.prettyprint;

import de.monticore.expressions.prettyprint.AssignmentExpressionsPrettyPrinter;
import de.monticore.expressions.prettyprint.CommonExpressionsPrettyPrinter;
import de.monticore.expressions.prettyprint.JavaClassExpressionsPrettyPrinter;
import de.monticore.expressions.prettyprint.ShiftExpressionsPrettyPrinter;
import de.monticore.java.javadsl._ast.ASTJavaDSLNode;
import de.monticore.java.javadsl._visitor.JavaDSLDelegatorVisitor;
import de.monticore.literals.prettyprint.LiteralsPrettyPrinterConcreteVisitor;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.types.prettyprint.TypesPrettyPrinterConcreteVisitor;

/**
 * @author npichler
 */
public class JavaDSLPrettyPrinterNew extends JavaDSLDelegatorVisitor {
  
  protected JavaDSLDelegatorVisitor realThis;
  
  protected IndentPrinter printer = null;
  
  public JavaDSLPrettyPrinterNew(IndentPrinter printer) {
    this.realThis = this;
    this.printer = printer;
    setCommonExpressionsVisitor(new CommonExpressionsPrettyPrinter(printer));
    setAssignmentExpressionsVisitor(new AssignmentExpressionsPrettyPrinter(printer));
    setJavaClassExpressionsVisitor(new JavaClassExpressionsPrettyPrinter(printer));
    setShiftExpressionsVisitor(new ShiftExpressionsPrettyPrinter(printer));
    setJavaDSLVisitor(new JavaDSLPrettyPrinter(printer));
    setLiteralsVisitor(new LiteralsPrettyPrinterConcreteVisitor(printer));
    setTypesVisitor(new TypesPrettyPrinterConcreteVisitor(printer));
  }
  
  protected IndentPrinter getPrinter() {
    return this.printer;
  }
  
  public String prettyprint(ASTJavaDSLNode a) {
    getPrinter().clearBuffer();
    a.accept(getRealThis());
    return getPrinter().getContent();
  }
  
  @Override
  public JavaDSLDelegatorVisitor getRealThis() {
    return realThis;
  }

  
}
