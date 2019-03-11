/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.prettyprint;

import java.util.Iterator;

import de.monticore.expressions.expressionsbasis._ast.ASTExpressionsBasisNode;
import de.monticore.java.javadsl._ast.*;
import de.monticore.java.javadsl._visitor.JavaDSLVisitor;
import de.monticore.prettyprint.CommentPrettyPrinter;
import de.monticore.prettyprint.IndentPrinter;

import de.monticore.types.mcbasictypes._ast.ASTMCBasicTypesNode;
import de.monticore.types.mccollectiontypes._ast.ASTMCTypeArgument;
import de.monticore.types.mcfullgenerictypes._ast.ASTMCArrayType;
import de.monticore.types.mcfullgenerictypes._ast.ASTMCFullGenericTypesNode;
import de.se_rwth.commons.Names;

/*
 * $Id: JavaDSLWriterVisitor.java,v 1.4 2008-07-17 08:34:01 cficek Exp $
 */

public class JavaDSLPrettyPrinter implements
    JavaDSLVisitor {
  
  protected IndentPrinter printer = null;

  private boolean WRITE_COMMENTS = false;
  
  private JavaDSLVisitor realThis = this;

  public JavaDSLPrettyPrinter(IndentPrinter out) {
    this.printer = out;
    setWriteComments(true);
  }
  
  public IndentPrinter getPrinter() {
    return this.printer;
  }
  
  protected void printNode(String s) {
    getPrinter().print(s);
  }
  
  protected void printList(Iterator<? extends ASTMCBasicTypesNode> iter, String seperator) {
    // print by iterate through all items
    String sep = "";
    while (iter.hasNext()) {
      getPrinter().print(sep);
      iter.next().accept(getRealThis());
      sep = seperator;
    }
  }
  
  protected void printExpressionsList(Iterator<? extends ASTExpressionsBasisNode> iter, String separator) {
    // print by iterate through all items
    String sep = "";
    while (iter.hasNext()) {
      getPrinter().print(sep);
      iter.next().accept(getRealThis());
      sep = separator;
    }
  }

  /**
   * @see de.monticore.java.javadsl._visitor.JavaDSLVisitor#handle(de.monticore.java.javadsl._ast.ASTASTStatement)
   */
  @Override
  public void handle(ASTASTStatement node) {
    getPrinter().println("de.monticore.ast.Comment _comment = new de.monticore.ast.Comment(getText());");
  }

  @Override
  public void handle(ASTCompilationUnit a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    if (a.isPresentPackageDeclaration()) {
      a.getPackageDeclaration().accept(getRealThis());
    }
    printSeparated(a.getImportDeclarationList().iterator(), "");
    printSeparated(a.getTypeDeclarationList().iterator(), "");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTPackageDeclaration a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    printSeparated(a.getAnnotationList().iterator(), "");
    getPrinter().print("package ");
    getPrinter().print(Names.getQualifiedName(a.getMCQualifiedName().getPartList()));
    getPrinter().println(";");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTMCArrayType a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    a.getMCType().accept(getRealThis());
    printDimensions(a.getDimensions());
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTInterfaceDeclaration a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    printSeparated(a.getModifierList().iterator(), " ");
    getPrinter().print("interface ");
    printNode(a.getName());
    if (a.isPresentMCTypeParameters()) {
      a.getMCTypeParameters().accept(getRealThis());
    }
    if (!a.getExtendedInterfaceList().isEmpty()) {
      getPrinter().print(" extends ");
      printList(a.getExtendedInterfaceList().iterator(), ", ");
    }
    a.getInterfaceBody().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTInterfaceBody a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().println(" {");
    getPrinter().indent();
    printSeparated(a.getInterfaceBodyDeclarationList().iterator(), "");
    getPrinter().unindent();
    getPrinter().println("}");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTClassBlock a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    if (a.isStatic()) {
      getPrinter().print("static ");
    }
    a.getJavaBlock().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTClassDeclaration a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    printSeparated(a.getModifierList().iterator(), " ");
    getPrinter().print("class ");
    printNode(a.getName());
    if (a.isPresentMCTypeParameters()) {
      a.getMCTypeParameters().accept(getRealThis());
    }
    if (a.isPresentSuperClass()) {
      getPrinter().print(" extends ");
      a.getSuperClass().accept(getRealThis());
    }
    if (!a.getImplementedInterfaceList().isEmpty()) {
      getPrinter().print(" implements ");
      printList(a.getImplementedInterfaceList().iterator(), ", ");
    }

    a.getClassBody().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTClassBody a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().println("{");
    getPrinter().indent();
    printSeparated(a.getClassBodyDeclarationList().iterator(), "");
    getPrinter().unindent();
    getPrinter().println("}");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTAnnotationTypeDeclaration a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    printSeparated(a.getModifierList().iterator(), " ");
    getPrinter().print("@ interface ");
    printNode(a.getName());
    a.getAnnotationTypeBody().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTAnnotationTypeBody a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().println(" {");
    getPrinter().indent();
    printSeparated(a.getAnnotationTypeElementDeclarationList().iterator(), "");
    getPrinter().unindent();
    getPrinter().println("}");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTEnumDeclaration a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    printSeparated(a.getModifierList().iterator(), " ");
    getPrinter().print("enum ");
    printNode(a.getName());
    if (!a.getImplementedInterfaceList().isEmpty()) {
      getPrinter().print(" implements ");
      printList(a.getImplementedInterfaceList().iterator(), "");
    }

    getPrinter().println(" {");
    getPrinter().indent();
    printSeparated(a.getEnumConstantDeclarationList().iterator(), ", ");
    if (a.isPresentEnumBody()) {
      a.getEnumBody().accept(getRealThis());
    }
    getPrinter().unindent();
    getPrinter().println("}");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTEnumBody a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().println(";");
    printSeparated(a.getClassBodyDeclarationList().iterator(), "");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTEnumConstantDeclaration a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    printSeparated(a.getAnnotationList().iterator(), "");
    printNode(a.getName());
    if (a.isPresentArguments()) {
      a.getArguments().accept(getRealThis());
    }
    if (a.isPresentClassBody()) {
      a.getClassBody().accept(getRealThis());
    }
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTVariableDeclarator a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    a.getDeclaratorId().accept(getRealThis());
    if (a.isPresentVariableInititializerOrExpression()) {
      getPrinter().print(" = ");
      a.getVariableInititializerOrExpression().accept(getRealThis());
    }
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTDeclaratorId a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    printNode(a.getName());
    for (int i = 0; i < a.getDimList().size(); i++) {
      getPrinter().print("[]");
    }
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTArrayInitializer a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().print("{");
    printSeparated(a.getVariableInititializerOrExpressionList().iterator(), ", ");
    getPrinter().print("}");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTAnnotationMethod a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    printSeparated(a.getModifierList().iterator(), " ");
    a.getMCType().accept(getRealThis());
    getPrinter().print(" ");
    printNode(a.getName());
    getPrinter().print("()");
    if (a.isPresentDefaultValue()) {
      a.getDefaultValue().accept(getRealThis());
    }
    getPrinter().print(";");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTAnnotationConstant a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    printSeparated(a.getModifierList().iterator(), " ");
    a.getMCType().accept(getRealThis());
    printSeparated(a.getVariableDeclaratorList().iterator(), ", ");
    getPrinter().println("");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTDefaultValue a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().print(" default ");
    a.getElementValueOrExpr().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTLocalVariableDeclarationStatement a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    a.getLocalVariableDeclaration().accept(getRealThis());
    getPrinter().println(";");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTLocalVariableDeclaration a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    printSeparated(a.getPrimitiveModifierList().iterator(), " ");
    getPrinter().print(" ");
    a.getMCType().accept(getRealThis());
    getPrinter().print(" ");
    printSeparated(a.getVariableDeclaratorList().iterator(), ", ");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTJavaBlock a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().println("{");
    getPrinter().indent();
    printSeparated(a.getBlockStatementList().iterator(), "");
    getPrinter().unindent();
    getPrinter().println("}");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTMethodDeclaration a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    a.getMethodSignature().accept(getRealThis());
    if (a.isPresentMethodBody()) {
      a.getMethodBody().accept(getRealThis());
    }
    else {
      getPrinter().println(";");
    }
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTMethodSignature a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    printSeparated(a.getModifierList().iterator(), " ");
    if (a.isPresentMCTypeParameters()) {
      a.getMCTypeParameters().accept(getRealThis());
    }
    a.getMCReturnType().accept(getRealThis());
    getPrinter().print(" ");
    printNode(a.getName());
    a.getFormalParameters().accept(getRealThis());
    for (int i = 0; i > a.getDimList().size(); i++) {
      getPrinter().print("[]");
    }
    if (a.isPresentThrows()) {
      getPrinter().print(" throws ");
      a.getThrows().accept(getRealThis());
    }
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTThrows a) {
    printList(a.getMCQualifiedNameList().iterator(), ", ");
  }

  @Override
  public void handle(ASTIfStatement a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().print("if (");
    a.getCondition().accept(getRealThis());
    getPrinter().print(") ");
    a.getThenStatement().accept(getRealThis());
    if (a.isPresentElseStatement()) {
      getPrinter().println("else ");
      a.getElseStatement().accept(getRealThis());
    }
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTLabeledStatement a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    printNode(a.getLabel());
    getPrinter().print(": ");
    a.getStatement().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTForStatement a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().print("for (");
    a.getForControl().accept(getRealThis());
    getPrinter().print(")");
    a.getStatement().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTWhileStatement a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().print("while (");
    a.getCondition().accept(getRealThis());
    getPrinter().print(")");
    a.getStatement().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTDoWhileStatement a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().print("do ");
    a.getStatement().accept(getRealThis());
    getPrinter().print("while (");
    a.getCondition().accept(getRealThis());
    getPrinter().println(");");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTTryStatement a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().println("try ");
    a.getJavaBlock().accept(getRealThis());
    getPrinter().println();
    a.getExceptionHandler().accept(getRealThis());
    getPrinter().println();
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTCatchExceptionsHandler a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    printSeparated(a.getCatchClauseList().iterator(), "");
    if (a.isPresentFinallyBlock()) {
      getPrinter().println();
      getPrinter().println("finally");
      getPrinter().indent();
      a.getFinallyBlock().accept(getRealThis());
      getPrinter().unindent();
    }
    getPrinter().println();
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTFinallyBlockOnlyHandler a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().println("finally");
    getPrinter().indent();
    a.getFinallyBlock().accept(getRealThis());
    getPrinter().unindent();
    getPrinter().println();
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTTryStatementWithResources a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().print("try (");
    printSeparated(a.getResourceList().iterator(), ";");
    getPrinter().println(") ");
    a.getJavaBlock().accept(getRealThis());
    printSeparated(a.getCatchClauseList().iterator(), "");
    if (a.isPresentFinallyBlock()) {
      getPrinter().println();
      getPrinter().println("finally");
      getPrinter().indent();
      a.getFinallyBlock().accept(getRealThis());
      getPrinter().unindent();
    }
    getPrinter().println();
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTResource a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    printSeparated(a.getPrimitiveModifierList().iterator(), " ");
    a.getMCType().accept(getRealThis());
    a.getDeclaratorId().accept(getRealThis());
    getPrinter().print(" = ");
    a.getExpression().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTIdentifierAndTypeArgument a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    printNode(a.getName());
    if (a.isPresentTypeArguments()) {
      a.getTypeArguments().accept(getRealThis());
    }
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTTypeArguments a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().print(("<"));
    String sep = "";
    for (ASTMCTypeArgument typeArg : a.getMCTypeArgumentList()) {
      getPrinter().print(sep);
      typeArg.accept(getRealThis());
    }
    getPrinter().print((">"));
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTCommonForControl a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    if (a.isPresentForInit()) {
      a.getForInit().accept(getRealThis());
    }
    getPrinter().print(";");
    if (a.isPresentCondition()) {
      a.getCondition().accept(getRealThis());
    }
    getPrinter().print(";");
    printExpressionsList(a.getExpressionList().iterator(), ",");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTForInitByExpressions a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().print(" ");
    printExpressionsList(a.getExpressionList().iterator(), ", ");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTEnhancedForControl a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    a.getFormalParameter().accept(getRealThis());
    getPrinter().print(": ");
    a.getExpression().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTConstructorDeclaration a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().println();
    printSeparated(a.getModifierList().iterator(), " ");
    if (a.isPresentMCTypeParameters()) {
      a.getMCTypeParameters().accept(getRealThis());
    }
    printNode(a.getName());
    a.getFormalParameters().accept(getRealThis());

    if (a.isPresentThrows()) {
      getPrinter().print(" throws ");
      a.getThrows().accept(getRealThis());
    }
    getPrinter().print(" ");
    a.getConstructorBody().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTFieldDeclaration a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    printSeparated(a.getModifierList().iterator(), " ");
    a.getMCType().accept(getRealThis());
    getPrinter().print(" ");
    printSeparated(a.getVariableDeclaratorList().iterator(), ", ");
    getPrinter().println(";");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTConstDeclaration a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    printSeparated(a.getModifierList().iterator(), " ");
    a.getMCType().accept(getRealThis());
    printSeparated(a.getConstantDeclaratorList().iterator(), ", ");
    getPrinter().println(";");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTConstantDeclarator a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    printNode(a.getName());
    for (int i = 0; i < a.getDimList().size(); i++) {
      getPrinter().print("[] ");
    }
    getPrinter().print(" = ");
    a.getVariableInititializerOrExpression().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTFormalParameters a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().print("(");
    if (a.isPresentFormalParameterListing()) {
      a.getFormalParameterListing().accept(getRealThis());
    }
    getPrinter().print(")");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTFormalParameterListing a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    printSeparated(a.getFormalParameterList().iterator(), ",");
    if (!a.getFormalParameterList().isEmpty() && a.isPresentLastFormalParameter()) {
      getPrinter().print(",");
    }
    if (a.isPresentLastFormalParameter()) {
      a.getLastFormalParameter().accept(getRealThis());
    }
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTFormalParameter a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    printSeparated(a.getPrimitiveModifierList().iterator(), " ");
    a.getMCType().accept(getRealThis());
    getPrinter().print(" ");
    a.getDeclaratorId().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTLastFormalParameter a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    printSeparated(a.getPrimitiveModifierList().iterator(), " ");
    a.getMCType().accept(getRealThis());
    getPrinter().print(" ... ");
    a.getDeclaratorId().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTAnnotation a) {
    getPrinter().print("@");
    a.getAnnotationName().accept(getRealThis());
    if (a.isPresentAnnotationArguments()) {
      getPrinter().print("(");
      a.getAnnotationArguments().accept(getRealThis());
      getPrinter().print(");");
    }
  }

  protected void printDimensions(int dims) {
    for (int i = 0; i < dims; i++) {
      getPrinter().print("[]");
    }
  }

  @Override
  public void handle(ASTAnnotationPairArguments a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    printSeparated(a.getElementValuePairList().iterator(), ", ");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTElementValuePair a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    printNode(a.getName());
    getPrinter().print(" = ");
    a.getElementValueOrExpr().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }


  @Override
  public void handle(ASTPrimitiveModifier a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().print(" " + printModifier(a.getModifier()) + " ");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTImportDeclaration a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().print("import ");
    if (a.isStatic()) {
      getPrinter().print("static ");
    }
    a.getMCQualifiedName().accept(getRealThis());
    if (a.isSTAR()) {
      getPrinter().print(".*");
    }
    getPrinter().println(";");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTEmptyDeclaration a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().print(");");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTAssertStatement a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().print("assert ");
    a.getAssertion().accept(getRealThis());
    if (a.isPresentMessage()) {
      getPrinter().print(" : ");
      a.getMessage().accept(getRealThis());
    }
    getPrinter().println(";");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTBreakStatement a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().print("break");
    if (a.isPresentLabel()) {
      printNode(a.getLabel());
    }
    getPrinter().println(";");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTContinueStatement a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().print("continue");
    if (a.isPresentLabel()) {
      printNode(a.getLabel());
    }
    getPrinter().println(";");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTSynchronizedStatement a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().print("synchronized (");
    a.getExpression().accept(getRealThis());
    getPrinter().print(") ");
    a.getJavaBlock().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTReturnStatement a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().print("return ");
    if (a.isPresentExpression()) {
      a.getExpression().accept(getRealThis());
    }
    getPrinter().println(";");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTThrowStatement a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().print("throw ");
    a.getExpression().accept(getRealThis());
    getPrinter().println(";");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTEmptyStatement a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().println(";");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTExpressionStatement a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    a.getExpression().accept(getRealThis());
    getPrinter().println(";");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTSwitchStatement a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().print("switch (");
    a.getExpression().accept(getRealThis());
    getPrinter().println(") {");
    getPrinter().indent();
    printSeparated(a.getSwitchBlockStatementGroupList().iterator(), "");
    printSeparated(a.getSwitchLabelList().iterator(), "");
    getPrinter().unindent();
    getPrinter().println("}");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTConstantExpressionSwitchLabel a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().println("case ");
    a.getConstantExpression().accept(getRealThis());
    getPrinter().println(":");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTEnumConstantSwitchLabel a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().println("case ");
    printNode(a.getEnumConstantName());
    getPrinter().println(":");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTDefaultSwitchLabel a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().println("default:");
  }

  @Override
  public void handle(ASTCatchClause a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().print("catch (");
    printSeparated(a.getPrimitiveModifierList().iterator(), " ");
    a.getCatchType().accept(getRealThis());
    getPrinter().print(" ");
    printNode(a.getName());
    getPrinter().print(") ");
    a.getJavaBlock().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTCatchType a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    printList(a.getMCQualifiedNameList().iterator(), "|");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTAnonymousClass a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().print("new ");
    if (a.isPresentTypeArguments()) {
      a.getTypeArguments().accept(getRealThis());
    }
    a.getCreatedName().accept(getRealThis());
    a.getClassCreatorRest().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTArrayCreator a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().print("new ");
    a.getCreatedName().accept(getRealThis());
    a.getArrayDimensionSpecifier().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTArrayDimensionByInitializer a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    for (int i = 0; i < a.getDimList().size(); i++) {
      getPrinter().print("[]");
    }
    getPrinter().print(" ");
    a.getArrayInitializer().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTArrayDimensionByExpression a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().print("[");
    printExpressionsList(a.getExpressionList().iterator(), "");
    getPrinter().print("]");
    for (int i = 0; i < a.getDimList().size(); i++) {
      getPrinter().print("[]");
    }
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTCreatedName a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    printSeparated(a.getIdentifierAndTypeArgumentList().iterator(), ".");
    if (a.isPresentMCPrimitiveType()) {
      a.getMCPrimitiveType().accept(getRealThis());
    }
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTInnerCreator a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().print("new ");
    if (a.isPresentFirstTypeArguments()) {
      a.getFirstTypeArguments().accept(getRealThis());
    }
    printNode(a.getName());
    if (a.isPresentSecondTypeArguments()) {
      a.getSecondTypeArguments().accept(getRealThis());
    }
    a.getClassCreatorRest().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTExtType node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getMCType().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTExtLiteral node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getLiteral().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTExtReturnType node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getMCReturnType().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTExtTypeArguments node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getTypeArguments().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  protected void printSeparated(Iterator<? extends ASTJavaDSLNode> iter, String separator) {
    // print by iterate through all items
    String sep = "";
    while (iter.hasNext()) {
      getPrinter().print(sep);
      iter.next().accept(getRealThis());
      sep = separator;
    }
  }

  public void setWriteComments(boolean wc) {
    WRITE_COMMENTS = wc;
  }

  public boolean isWriteCommentsEnabeled() {
    return WRITE_COMMENTS;
  }

  /**
   * This method prettyprints a given node from Java.
   *
   * @param a A node from Java.
   * @return String representation.
   */
  public String prettyprint(ASTJavaDSLNode a) {
    getPrinter().clearBuffer();
    a.accept(getRealThis());
    return getPrinter().getContent();
  }

  private String printModifier(int constant) {

    switch (constant) {
      case ASTConstantsJavaDSL.PRIVATE:
        return "private";
      case ASTConstantsJavaDSL.PUBLIC:
        return "public";
      case ASTConstantsJavaDSL.PROTECTED:
        return "protected";
      case ASTConstantsJavaDSL.STATIC:
        return "static";
      case ASTConstantsJavaDSL.TRANSIENT:
        return "transient";
      case ASTConstantsJavaDSL.FINAL:
        return "final";
      case ASTConstantsJavaDSL.ABSTRACT:
        return "abstract";
      case ASTConstantsJavaDSL.NATIVE:
        return "native";
      case ASTConstantsJavaDSL.THREADSAFE:
        return "threadsafe";
      case ASTConstantsJavaDSL.SYNCHRONIZED:
        return "synchronized";
      case ASTConstantsJavaDSL.VOLATILE:
        return "volatile";
      case ASTConstantsJavaDSL.STRICTFP:
        return "strictfp";
      default:
        return null;
    }

  }

  /**
   * @see de.monticore.java.javadsl._visitor.JavaDSLVisitor#setRealThis(de.monticore.java.javadsl._visitor.JavaDSLVisitor)
   */
  @Override
  public void setRealThis(JavaDSLVisitor realThis) {
    this.realThis = realThis;
  }

  /**
   * @see de.monticore.java.javadsl._visitor.JavaDSLVisitor#getRealThis()
   */
  @Override
  public JavaDSLVisitor getRealThis() {
    return realThis;
  }
  
  
}
