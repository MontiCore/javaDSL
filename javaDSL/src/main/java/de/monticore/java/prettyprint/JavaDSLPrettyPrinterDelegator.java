/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.prettyprint;

import de.monticore.MCCommonLiteralsPrettyPrinter;
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
    setMCCommonLiteralsVisitor(new MCCommonLiteralsPrettyPrinter(printer));
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
