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

import de.monticore.MCBasicLiteralsPrettyPrinter;
import de.monticore.MCJavaLiteralsPrettyPrinter;
import de.monticore.expressions.prettyprint2.*;
import de.monticore.java.javadsl._ast.ASTJavaDSLNode;
import de.monticore.java.javadsl._visitor.JavaDSLDelegatorVisitor;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.prettyprint.MCBasicsPrettyPrinter;
import de.monticore.types.prettyprint.MCBasicTypesPrettyPrinter;
import de.monticore.types.prettyprint.MCCollectionTypesPrettyPrinter;
import de.monticore.types.prettyprint.MCFullGenericTypesPrettyPrinter;
import de.monticore.types.prettyprint.MCSimpleGenericTypesPrettyPrinter;

/**
 * @author npichler
 */
public class JavaDSLPrettyPrinterDelegator extends JavaDSLDelegatorVisitor {
  
  protected JavaDSLDelegatorVisitor realThis;
  
  protected IndentPrinter printer = null;
  
  public JavaDSLPrettyPrinterDelegator(IndentPrinter printer) {
    this.realThis = this;
    this.printer = printer;
    setExpressionsBasisVisitor(new ExpressionsBasisPrettyPrinter(printer));
    setCommonExpressionsVisitor(new CommonExpressionsPrettyPrinter(printer));
    setAssignmentExpressionsVisitor(new AssignmentExpressionsPrettyPrinter(printer));
    setJavaClassExpressionsVisitor(new JavaClassExpressionsPrettyPrinter(printer));
    setBitExpressionsVisitor(new BitExpressionsPrettyPrinter(printer));
    setJavaDSLVisitor(new JavaDSLPrettyPrinter(printer));
    setMCBasicLiteralsVisitor(new MCBasicLiteralsPrettyPrinter(printer));
    setMCBasicsVisitor(new MCBasicsPrettyPrinter(printer));
    setMCBasicTypesVisitor(new MCBasicTypesPrettyPrinter(printer));
    setMCCollectionTypesVisitor(new MCCollectionTypesPrettyPrinter(printer));
    setMCFullGenericTypesVisitor(new MCFullGenericTypesPrettyPrinter(printer));
    setMCJavaLiteralsVisitor(new MCJavaLiteralsPrettyPrinter(printer));
    setMCSimpleGenericTypesVisitor(new MCSimpleGenericTypesPrettyPrinter(printer));
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
