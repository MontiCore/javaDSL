/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.report;

import de.monticore.generating.templateengine.reporting.commons.ReportingRepository;
import de.monticore.java.javadsl._od.JavaDSL2OD;
import de.monticore.java.javadsl._visitor.JavaDSLDelegatorVisitor;
import de.monticore.java.javadsl._visitor.JavaDSLVisitor;
import de.monticore.literals.literals._od.Literals2OD;
import de.monticore.mcexpressions._od.MCExpressions2OD;
import de.monticore.prettyprint.IndentPrinter;
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
    visitor.setMCExpressionsVisitor(new MCExpressions2OD(printer, reporting));
    visitor.setJavaDSLVisitor(this);
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
