package de.monticore.java.prettyprint;

import de.monticore.expressions.prettyprint.*;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTJavaDSLNode;
import de.monticore.java.javadsl._visitor.JavaDSLTraverser;
import de.monticore.literals.prettyprint.MCCommonLiteralsPrettyPrinter;
import de.monticore.literals.prettyprint.MCJavaLiteralsPrettyPrinter;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.prettyprint.JavaLightPrettyPrinter;
import de.monticore.prettyprint.MCBasicsPrettyPrinter;
import de.monticore.statements.prettyprint.*;
import de.monticore.types.prettyprint.*;

public class JavaDSLFullPrettyPrinter {

  protected final IndentPrinter printer;
  protected JavaDSLTraverser traverser;

  public JavaDSLFullPrettyPrinter() {
    this(new IndentPrinter());
  }

  public JavaDSLFullPrettyPrinter(IndentPrinter printer) {
    this.printer = printer;
    this.traverser = JavaDSLMill.traverser();

    AssignmentExpressionsPrettyPrinter assignmentExpressions = new AssignmentExpressionsPrettyPrinter(printer);
    this.traverser.add4AssignmentExpressions(assignmentExpressions);
    this.traverser.setAssignmentExpressionsHandler(assignmentExpressions);

    BitExpressionsPrettyPrinter bitExpressions = new BitExpressionsPrettyPrinter(printer);
    this.traverser.add4BitExpressions(bitExpressions);
    this.traverser.setBitExpressionsHandler(bitExpressions);

    CommonExpressionsPrettyPrinter commonExpressions = new CommonExpressionsPrettyPrinter(printer);
    this.traverser.add4CommonExpressions(commonExpressions);
    this.traverser.setCommonExpressionsHandler(commonExpressions);

    ExpressionsBasisPrettyPrinter expressionsBasis = new ExpressionsBasisPrettyPrinter(printer);
    this.traverser.add4ExpressionsBasis(expressionsBasis);
    this.traverser.setExpressionsBasisHandler(expressionsBasis);

    JavaClassExpressionsPrettyPrinter javaClassExpressions = new JavaClassExpressionsPrettyPrinter(printer);
    this.traverser.add4JavaClassExpressions(javaClassExpressions);
    this.traverser.setJavaClassExpressionsHandler(javaClassExpressions);

    JavaLightPrettyPrinter javaLight = new JavaLightPrettyPrinter(printer);
    this.traverser.add4JavaLight(javaLight);
    this.traverser.setJavaLightHandler(javaLight);

    MCArrayStatementsPrettyPrinter mcArrayStatements = new MCArrayStatementsPrettyPrinter(printer);
    this.traverser.add4MCArrayStatements(mcArrayStatements);
    this.traverser.setMCArrayStatementsHandler(mcArrayStatements);

    MCArrayTypesPrettyPrinter mcArrayTypes = new MCArrayTypesPrettyPrinter(printer);
    this.traverser.add4MCArrayTypes(mcArrayTypes);
    this.traverser.setMCArrayTypesHandler(mcArrayTypes);

    /*
     * TODO
     *  MCBasicsPrettyPrinter does not implement MCBasicsHandler. Figure out if
     *  this is a bug or intended.
     */
    MCBasicsPrettyPrinter mcBasics = new MCBasicsPrettyPrinter(printer);
    this.traverser.add4MCBasics(mcBasics);
//    this.traverser.setMCBasicsHandler(mcBasics);

    MCBasicTypesPrettyPrinter mcBasicTypes = new MCBasicTypesPrettyPrinter(printer);
    this.traverser.add4MCBasicTypes(mcBasicTypes);
    this.traverser.setMCBasicTypesHandler(mcBasicTypes);

    MCCollectionTypesPrettyPrinter mcCollectionTypes = new MCCollectionTypesPrettyPrinter(printer);
    this.traverser.add4MCCollectionTypes(mcCollectionTypes);
    this.traverser.setMCCollectionTypesHandler(mcCollectionTypes);

    MCCommonLiteralsPrettyPrinter mcCommonLiterals = new MCCommonLiteralsPrettyPrinter(printer);
    this.traverser.add4MCCommonLiterals(mcCommonLiterals);
    this.traverser.setMCCommonLiteralsHandler(mcCommonLiterals);

    MCCommonStatementsPrettyPrinter mcCommonStatements = new MCCommonStatementsPrettyPrinter(printer);
    this.traverser.add4MCCommonStatements(mcCommonStatements);
    this.traverser.setMCCommonStatementsHandler(mcCommonStatements);

    MCFullGenericTypesPrettyPrinter mcFullGenericTypes = new MCFullGenericTypesPrettyPrinter(printer);
    this.traverser.add4MCFullGenericTypes(mcFullGenericTypes);
    this.traverser.setMCFullGenericTypesHandler(mcFullGenericTypes);

    MCAssertStatementsPrettyPrinter mcAssertStatements = new MCAssertStatementsPrettyPrinter(printer);
    this.traverser.add4MCAssertStatements(mcAssertStatements);
    this.traverser.setMCAssertStatementsHandler(mcAssertStatements);

    MCExceptionStatementsPrettyPrinter mcExceptionStatements = new MCExceptionStatementsPrettyPrinter(printer);
    this.traverser.add4MCExceptionStatements(mcExceptionStatements);
    this.traverser.setMCExceptionStatementsHandler(mcExceptionStatements);

    MCJavaLiteralsPrettyPrinter mcJavaLiterals = new MCJavaLiteralsPrettyPrinter(printer);
    this.traverser.add4MCJavaLiterals(mcJavaLiterals);
    this.traverser.setMCJavaLiteralsHandler(mcJavaLiterals);

    MCLowLevelStatementsPrettyPrinter mcLowLevelStatements = new MCLowLevelStatementsPrettyPrinter(printer);
    this.traverser.add4MCLowLevelStatements(mcLowLevelStatements);
    this.traverser.setMCLowLevelStatementsHandler(mcLowLevelStatements);

    MCReturnStatementsPrettyPrinter mcReturnStatements = new MCReturnStatementsPrettyPrinter(printer);
    this.traverser.add4MCReturnStatements(mcReturnStatements);
    this.traverser.setMCReturnStatementsHandler(mcReturnStatements);

    MCSimpleGenericTypesPrettyPrinter mcSimpleGenericTypes = new MCSimpleGenericTypesPrettyPrinter(printer);
    this.traverser.add4MCSimpleGenericTypes(mcSimpleGenericTypes);
    this.traverser.setMCSimpleGenericTypesHandler(mcSimpleGenericTypes);

    MCSynchronizedStatementsPrettyPrinter mcSynchronizedStatements = new MCSynchronizedStatementsPrettyPrinter(printer);
    this.traverser.add4MCSynchronizedStatements(mcSynchronizedStatements);
    this.traverser.setMCSynchronizedStatementsHandler(mcSynchronizedStatements);

    MCVarDeclarationStatementsPrettyPrinter mcVarDeclarationStatements = new MCVarDeclarationStatementsPrettyPrinter(printer);
    this.traverser.add4MCVarDeclarationStatements(mcVarDeclarationStatements);
    this.traverser.setMCVarDeclarationStatementsHandler(mcVarDeclarationStatements);


    JavaDSLPrettyPrinter javaDSL = new JavaDSLPrettyPrinter(printer);
    this.traverser.add4JavaDSL(javaDSL);
    this.traverser.setJavaDSLHandler(javaDSL);
  }

  public IndentPrinter getPrinter() {
    return this.printer;
  }

  public JavaDSLTraverser getTraverser() {
    return this.traverser;
  }

  public void setTraverser(JavaDSLTraverser traverser) {
    this.traverser = traverser;
  }

  public String prettyprint(ASTJavaDSLNode node) {
    getPrinter().clearBuffer();
    node.accept(getTraverser());
    return getPrinter().getContent();
  }

}