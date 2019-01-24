/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.report;

import de.monticore.expressions.assignmentexpressions._od.AssignmentExpressions2OD;
import de.monticore.expressions.commonexpressions._od.CommonExpressions2OD;
import de.monticore.expressions.expressionsbasis._od.ExpressionsBasis2OD;
import de.monticore.generating.templateengine.reporting.commons.ReportingRepository;
import de.monticore.java.javadsl._od.JavaDSL2OD;
import de.monticore.java.javadsl._visitor.JavaDSLDelegatorVisitor;
import de.monticore.java.javadsl._visitor.JavaDSLVisitor;
import de.monticore.expressions.javaclassexpressions._od.JavaClassExpressions2OD;
import de.monticore.mcbasicliterals._od.MCBasicLiterals2OD;
import de.monticore.mcbasics._od.MCBasics2OD;
import de.monticore.mcjavaliterals._od.MCJavaLiterals2OD;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.expressions.shiftexpressions._od.ShiftExpressions2OD;
import de.monticore.types.mcbasictypes._od.MCBasicTypes2OD;
import de.monticore.types.mccollectiontypes._od.MCCollectionTypes2OD;
import de.monticore.types.mcfullgenerictypes._od.MCFullGenericTypes2OD;

public class JavaDSLAST2OD extends JavaDSL2OD {
  
  private JavaDSLVisitor realThis = this;
  
  protected JavaDSLDelegatorVisitor visitor;
      
  public JavaDSLAST2OD(IndentPrinter printer, ReportingRepository reporting) {
    super(printer, reporting);
    visitor = new JavaDSLDelegatorVisitor();
    visitor.setExpressionsBasisVisitor(new ExpressionsBasis2OD(printer, reporting));
    visitor.setJavaDSLVisitor(new JavaDSL2OD(printer, reporting));
    visitor.setAssignmentExpressionsVisitor(new AssignmentExpressions2OD(printer, reporting));
    visitor.setCommonExpressionsVisitor(new CommonExpressions2OD(printer, reporting));
    visitor.setJavaClassExpressionsVisitor(new JavaClassExpressions2OD(printer, reporting));
    visitor.setShiftExpressionsVisitor(new ShiftExpressions2OD(printer, reporting));
    visitor.setMCBasicsVisitor(new MCBasics2OD(printer, reporting));
    visitor.setMCBasicLiteralsVisitor(new MCBasicLiterals2OD(printer, reporting));
    visitor.setMCBasicTypesVisitor(new MCBasicTypes2OD(printer, reporting));
    visitor.setMCCollectionTypesVisitor(new MCCollectionTypes2OD(printer, reporting));
    visitor.setMCFullGenericTypesVisitor(new MCFullGenericTypes2OD(printer, reporting));
    visitor.setMCJavaLiteralsVisitor(new MCJavaLiterals2OD(printer, reporting));
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
