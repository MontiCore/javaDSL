/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.report;

import de.monticore.assignmentexpressions._od.AssignmentExpressions2OD;
import de.monticore.commonexpressions._od.CommonExpressions2OD;
import de.monticore.expressionsbasis._od.ExpressionsBasis2OD;
import de.monticore.generating.templateengine.reporting.commons.ReportingRepository;
import de.monticore.java.javadsl._od.JavaDSL2OD;
import de.monticore.java.javadsl._visitor.JavaDSLDelegatorVisitor;
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
  
  protected JavaDSLDelegatorVisitor visitor;
      
  public JavaDSLAST2OD(IndentPrinter printer, ReportingRepository reporting) {
    super(printer, reporting);
    visitor = new JavaDSLDelegatorVisitor();
    visitor.setLiteralsVisitor(
        new Literals2OD(printer, reporting));
    visitor.setTypesVisitor(new Types2OD(printer, reporting));
    visitor.setExpressionsBasisVisitor(new ExpressionsBasis2OD(printer, reporting));
    visitor.setJavaDSLVisitor(new JavaDSL2OD(printer, reporting));
    visitor.setAssignmentExpressionsVisitor(new AssignmentExpressions2OD(printer, reporting));
    visitor.setCommonExpressionsVisitor(new CommonExpressions2OD(printer, reporting));
    visitor.setJavaClassExpressionsVisitor(new JavaClassExpressions2OD(printer, reporting));
    visitor.setShiftExpressionsVisitor(new ShiftExpressions2OD(printer, reporting));
    visitor.setMCBasicsVisitor(new MCBasics2OD(printer, reporting));
    visitor.setLexicalsVisitor(new Lexicals2OD(printer, reporting));
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
