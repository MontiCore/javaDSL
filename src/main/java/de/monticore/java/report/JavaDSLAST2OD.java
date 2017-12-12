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
package de.monticore.java.report;

import de.monticore.assignmentexpressions._od.AssignmentExpressions2OD;
import de.monticore.commonexpressions._od.CommonExpressions2OD;
import de.monticore.expressionsbasis._od.ExpressionsBasis2OD;
import de.monticore.generating.templateengine.reporting.commons.ReportingRepository;
import de.monticore.java.javadsl._od.JavaDSL2OD;
import de.monticore.java.javadsl._visitor.CommonJavaDSLDelegatorVisitor;
import de.monticore.java.javadsl._visitor.JavaDSLVisitor;
import de.monticore.javaclassexpressions._od.JavaClassExpressions2OD;
import de.monticore.lexicals.lexicals._od.Lexicals2OD;
import de.monticore.literals.literals._od.Literals2OD;
import de.monticore.mcbasics._od.MCBasics2OD;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.shiftexpressions._od.ShiftExpressions2OD;
import de.monticore.types.types._od.Types2OD;

public class JavaDSLAST2OD extends JavaDSL2OD {
  
  private JavaDSLVisitor realThis = this;
  
  protected CommonJavaDSLDelegatorVisitor visitor;
      
  public JavaDSLAST2OD(IndentPrinter printer, ReportingRepository reporting) {
    super(printer, reporting);
    visitor = new CommonJavaDSLDelegatorVisitor();
    visitor.set_de_monticore_literals_literals__visitor_LiteralsVisitor(
        new Literals2OD(printer, reporting));
    visitor.set_de_monticore_types_types__visitor_TypesVisitor(new Types2OD(printer, reporting));
    visitor.set_de_monticore_expressionsbasis__visitor_ExpressionsBasisVisitor(new ExpressionsBasis2OD(printer, reporting));
    visitor.set_de_monticore_java_javadsl__visitor_JavaDSLVisitor(new JavaDSL2OD(printer, reporting));
    visitor.set_de_monticore_assignmentexpressions__visitor_AssignmentExpressionsVisitor(new AssignmentExpressions2OD(printer, reporting));
    visitor.set_de_monticore_commonexpressions__visitor_CommonExpressionsVisitor(new CommonExpressions2OD(printer, reporting));
    visitor.set_de_monticore_javaclassexpressions__visitor_JavaClassExpressionsVisitor(new JavaClassExpressions2OD(printer, reporting));
    visitor.set_de_monticore_shiftexpressions__visitor_ShiftExpressionsVisitor(new ShiftExpressions2OD(printer, reporting));
    visitor.set_de_monticore_mcbasics__visitor_MCBasicsVisitor(new MCBasics2OD(printer, reporting));
    visitor.set_de_monticore_lexicals_lexicals__visitor_LexicalsVisitor(new Lexicals2OD(printer, reporting));
    setPrintEmptyList(true);
    setPrintEmptyOptional(true);
  }
    
  /**
   * @see de.monticore.java.javadsl._od.JavaDSL2OD#getRealThis()
   */
  @Override
  public void setRealThis(JavaDSLVisitor realThis) {
    if (this.realThis != realThis) {
      this.realThis = realThis;
      visitor.setRealThis(realThis);
    }
  }
  
  @Override
  public JavaDSLVisitor getRealThis() {
    return realThis;
  }
}
