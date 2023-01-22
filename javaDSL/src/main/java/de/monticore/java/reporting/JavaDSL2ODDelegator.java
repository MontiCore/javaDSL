package de.monticore.java.reporting;

import de.monticore.expressions.assignmentexpressions._od.AssignmentExpressions2OD;
import de.monticore.expressions.bitexpressions._od.BitExpressions2OD;
import de.monticore.expressions.commonexpressions._od.CommonExpressions2OD;
import de.monticore.expressions.expressionsbasis._od.ExpressionsBasis2OD;
import de.monticore.expressions.javaclassexpressions._od.JavaClassExpressions2OD;
import de.monticore.generating.templateengine.reporting.commons.ReportingRepository;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._od.JavaDSL2OD;
import de.monticore.java.javadsl._visitor.JavaDSLTraverser;
import de.monticore.java.prettyprint.JavaDSLPrettyPrinter;
import de.monticore.javalight._od.JavaLight2OD;
import de.monticore.literals.mccommonliterals._od.MCCommonLiterals2OD;
import de.monticore.literals.mcjavaliterals._od.MCJavaLiterals2OD;
import de.monticore.literals.prettyprint.MCCommonLiteralsPrettyPrinter;
import de.monticore.literals.prettyprint.MCJavaLiteralsPrettyPrinter;
import de.monticore.mcbasics._od.MCBasics2OD;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.prettyprint.MCBasicsPrettyPrinter;
import de.monticore.statements.mcarraystatements._od.MCArrayStatements2OD;
import de.monticore.statements.mcassertstatements._od.MCAssertStatements2OD;
import de.monticore.statements.mccommonstatements._od.MCCommonStatements2OD;
import de.monticore.statements.mcexceptionstatements._od.MCExceptionStatements2OD;
import de.monticore.statements.mclowlevelstatements._od.MCLowLevelStatements2OD;
import de.monticore.statements.mcreturnstatements._od.MCReturnStatements2OD;
import de.monticore.statements.mcsynchronizedstatements._od.MCSynchronizedStatements2OD;
import de.monticore.statements.mcvardeclarationstatements._od.MCVarDeclarationStatements2OD;
import de.monticore.statements.prettyprint.*;
import de.monticore.types.mcarraytypes._od.MCArrayTypes2OD;
import de.monticore.types.mcbasictypes._od.MCBasicTypes2OD;
import de.monticore.types.mccollectiontypes._od.MCCollectionTypes2OD;
import de.monticore.types.mcfullgenerictypes._od.MCFullGenericTypes2OD;
import de.monticore.types.mcsimplegenerictypes._od.MCSimpleGenericTypes2OD;
import de.monticore.types.prettyprint.*;

public final class JavaDSL2ODDelegator {

  private final IndentPrinter printer;

  private final JavaDSLTraverser traverser;

  public JavaDSL2ODDelegator(IndentPrinter printer, ReportingRepository reporting) {
    this.printer = printer;

    this.traverser = JavaDSLMill.traverser();

    AssignmentExpressions2OD assignmentExpressions = new AssignmentExpressions2OD(printer, reporting);
    this.traverser.add4AssignmentExpressions(assignmentExpressions);
    this.traverser.setAssignmentExpressionsHandler(assignmentExpressions);

    BitExpressions2OD bitExpressions = new BitExpressions2OD(printer, reporting);
    this.traverser.add4BitExpressions(bitExpressions);
    this.traverser.setBitExpressionsHandler(bitExpressions);

    CommonExpressions2OD commonExpressions = new CommonExpressions2OD(printer, reporting);
    this.traverser.add4CommonExpressions(commonExpressions);
    this.traverser.setCommonExpressionsHandler(commonExpressions);

    ExpressionsBasis2OD expressionsBasis = new ExpressionsBasis2OD(printer, reporting);
    this.traverser.add4ExpressionsBasis(expressionsBasis);
    this.traverser.setExpressionsBasisHandler(expressionsBasis);

    JavaClassExpressions2OD javaClassExpressions = new JavaClassExpressions2OD(printer, reporting);
    this.traverser.add4JavaClassExpressions(javaClassExpressions);
    this.traverser.setJavaClassExpressionsHandler(javaClassExpressions);

    JavaLight2OD javaLight = new JavaLight2OD(printer, reporting);
    this.traverser.add4JavaLight(javaLight);
    this.traverser.setJavaLightHandler(javaLight);

    MCArrayStatements2OD mcArrayStatements = new MCArrayStatements2OD(printer, reporting);
    this.traverser.add4MCArrayStatements(mcArrayStatements);
    this.traverser.setMCArrayStatementsHandler(mcArrayStatements);

    MCArrayTypes2OD mcArrayTypes = new MCArrayTypes2OD(printer, reporting);
    this.traverser.add4MCArrayTypes(mcArrayTypes);
    this.traverser.setMCArrayTypesHandler(mcArrayTypes);

    /*
     * TODO
     *  MCBasics2OD does not implement MCBasicsHandler. Figure out if
     *  this is a bug or intended.
     */
    MCBasics2OD mcBasics = new MCBasics2OD(printer, reporting);
    this.traverser.add4MCBasics(mcBasics);
//    this.traverser.setMCBasicsHandler(mcBasics);

    MCBasicTypes2OD mcBasicTypes = new MCBasicTypes2OD(printer, reporting);
    this.traverser.add4MCBasicTypes(mcBasicTypes);
    this.traverser.setMCBasicTypesHandler(mcBasicTypes);

    MCCollectionTypes2OD mcCollectionTypes = new MCCollectionTypes2OD(printer, reporting);
    this.traverser.add4MCCollectionTypes(mcCollectionTypes);
    this.traverser.setMCCollectionTypesHandler(mcCollectionTypes);

    MCCommonLiterals2OD mcCommonLiterals = new MCCommonLiterals2OD(printer, reporting);
    this.traverser.add4MCCommonLiterals(mcCommonLiterals);
    this.traverser.setMCCommonLiteralsHandler(mcCommonLiterals);

    MCCommonStatements2OD mcCommonStatements = new MCCommonStatements2OD(printer, reporting);
    this.traverser.add4MCCommonStatements(mcCommonStatements);
    this.traverser.setMCCommonStatementsHandler(mcCommonStatements);

    MCFullGenericTypes2OD mcFullGenericTypes = new MCFullGenericTypes2OD(printer, reporting);
    this.traverser.add4MCFullGenericTypes(mcFullGenericTypes);
    this.traverser.setMCFullGenericTypesHandler(mcFullGenericTypes);

    MCAssertStatements2OD mcAssertStatements = new MCAssertStatements2OD(printer, reporting);
    this.traverser.add4MCAssertStatements(mcAssertStatements);
    this.traverser.setMCAssertStatementsHandler(mcAssertStatements);

    MCExceptionStatements2OD mcExceptionStatements = new MCExceptionStatements2OD(printer, reporting);
    this.traverser.add4MCExceptionStatements(mcExceptionStatements);
    this.traverser.setMCExceptionStatementsHandler(mcExceptionStatements);

    MCJavaLiterals2OD mcJavaLiterals = new MCJavaLiterals2OD(printer, reporting);
    this.traverser.add4MCJavaLiterals(mcJavaLiterals);
    this.traverser.setMCJavaLiteralsHandler(mcJavaLiterals);

    MCLowLevelStatements2OD mcLowLevelStatements = new MCLowLevelStatements2OD(printer, reporting);
    this.traverser.add4MCLowLevelStatements(mcLowLevelStatements);
    this.traverser.setMCLowLevelStatementsHandler(mcLowLevelStatements);

    MCReturnStatements2OD mcReturnStatements = new MCReturnStatements2OD(printer, reporting);
    this.traverser.add4MCReturnStatements(mcReturnStatements);
    this.traverser.setMCReturnStatementsHandler(mcReturnStatements);

    MCSimpleGenericTypes2OD mcSimpleGenericTypes = new MCSimpleGenericTypes2OD(printer, reporting);
    this.traverser.add4MCSimpleGenericTypes(mcSimpleGenericTypes);
    this.traverser.setMCSimpleGenericTypesHandler(mcSimpleGenericTypes);

    MCSynchronizedStatements2OD mcSynchronizedStatements = new MCSynchronizedStatements2OD(printer, reporting);
    this.traverser.add4MCSynchronizedStatements(mcSynchronizedStatements);
    this.traverser.setMCSynchronizedStatementsHandler(mcSynchronizedStatements);

    MCVarDeclarationStatements2OD mcVarDeclarationStatements = new MCVarDeclarationStatements2OD(printer, reporting);
    this.traverser.add4MCVarDeclarationStatements(mcVarDeclarationStatements);
    this.traverser.setMCVarDeclarationStatementsHandler(mcVarDeclarationStatements);


    JavaDSL2OD javaDSL = new JavaDSL2OD(printer, reporting);
    this.traverser.add4JavaDSL(javaDSL);
    this.traverser.setJavaDSLHandler(javaDSL);
  }

  public JavaDSLTraverser getTraverser() {
    return this.traverser;
  }

  public String print(ASTCompilationUnit node) {
    this.printer.clearBuffer();
    this.printer.setIndentLength(2);

    node.accept(this.traverser);
    return this.printer.getContent();
  }

}