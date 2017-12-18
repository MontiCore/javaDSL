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
import de.monticore.java.javadsl._visitor.CommonJavaDSLDelegatorVisitor;
import de.monticore.literals.prettyprint.LiteralsPrettyPrinterConcreteVisitor;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.types.prettyprint.TypesPrettyPrinterConcreteVisitor;

/**
 * @author npichler
 */
public class JavaDSLPrettyPrinterNew extends CommonJavaDSLDelegatorVisitor{
  
  protected CommonJavaDSLDelegatorVisitor realThis;
  
  protected IndentPrinter printer = null;
  
  public JavaDSLPrettyPrinterNew(IndentPrinter printer) {
    this.realThis = this;
    this.printer = printer;
    set_de_monticore_commonexpressions__visitor_CommonExpressionsVisitor(new CommonExpressionsPrettyPrinter(printer));
    set_de_monticore_assignmentexpressions__visitor_AssignmentExpressionsVisitor(new AssignmentExpressionsPrettyPrinter(printer));
    set_de_monticore_javaclassexpressions__visitor_JavaClassExpressionsVisitor(new JavaClassExpressionsPrettyPrinter(printer));
    set_de_monticore_shiftexpressions__visitor_ShiftExpressionsVisitor(new ShiftExpressionsPrettyPrinter(printer));
    set_de_monticore_java_javadsl__visitor_JavaDSLVisitor(new JavaDSLPrettyPrinter(printer));
    set_de_monticore_literals_literals__visitor_LiteralsVisitor(new LiteralsPrettyPrinterConcreteVisitor(printer));
    set_de_monticore_types_types__visitor_TypesVisitor(new TypesPrettyPrinterConcreteVisitor(printer));
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
  public CommonJavaDSLDelegatorVisitor getRealThis() {
    return realThis;
  }

  
}
