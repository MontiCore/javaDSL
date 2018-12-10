/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.prettyprint;

import java.util.Iterator;

import de.monticore.expressions.prettyprint.MCExpressionsPrettyPrinter;
import de.monticore.java.javadsl._ast.ASTASTStatement;
import de.monticore.java.javadsl._ast.ASTAnnotation;
import de.monticore.java.javadsl._ast.ASTAnnotationConstant;
import de.monticore.java.javadsl._ast.ASTAnnotationMethod;
import de.monticore.java.javadsl._ast.ASTAnnotationPairArguments;
import de.monticore.java.javadsl._ast.ASTAnnotationTypeBody;
import de.monticore.java.javadsl._ast.ASTAnnotationTypeDeclaration;
import de.monticore.java.javadsl._ast.ASTAnonymousClass;
import de.monticore.java.javadsl._ast.ASTArrayCreator;
import de.monticore.java.javadsl._ast.ASTArrayDimensionByExpression;
import de.monticore.java.javadsl._ast.ASTArrayDimensionByInitializer;
import de.monticore.java.javadsl._ast.ASTArrayInitializer;
import de.monticore.java.javadsl._ast.ASTAssertStatement;
import de.monticore.java.javadsl._ast.ASTBreakStatement;
import de.monticore.java.javadsl._ast.ASTCatchClause;
import de.monticore.java.javadsl._ast.ASTCatchExceptionsHandler;
import de.monticore.java.javadsl._ast.ASTCatchType;
import de.monticore.java.javadsl._ast.ASTClassBlock;
import de.monticore.java.javadsl._ast.ASTClassBody;
import de.monticore.java.javadsl._ast.ASTClassDeclaration;
import de.monticore.java.javadsl._ast.ASTCommonForControl;
import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._ast.ASTConstDeclaration;
import de.monticore.java.javadsl._ast.ASTConstantDeclarator;
import de.monticore.java.javadsl._ast.ASTConstantExpressionSwitchLabel;
import de.monticore.java.javadsl._ast.ASTConstantsJavaDSL;
import de.monticore.java.javadsl._ast.ASTConstructorDeclaration;
import de.monticore.java.javadsl._ast.ASTContinueStatement;
import de.monticore.java.javadsl._ast.ASTCreatedName;
import de.monticore.java.javadsl._ast.ASTDeclaratorId;
import de.monticore.java.javadsl._ast.ASTDefaultSwitchLabel;
import de.monticore.java.javadsl._ast.ASTDefaultValue;
import de.monticore.java.javadsl._ast.ASTDoWhileStatement;
import de.monticore.java.javadsl._ast.ASTElementValuePair;
import de.monticore.java.javadsl._ast.ASTEmptyDeclaration;
import de.monticore.java.javadsl._ast.ASTEmptyStatement;
import de.monticore.java.javadsl._ast.ASTEnhancedForControl;
import de.monticore.java.javadsl._ast.ASTEnumBody;
import de.monticore.java.javadsl._ast.ASTEnumConstantDeclaration;
import de.monticore.java.javadsl._ast.ASTEnumConstantSwitchLabel;
import de.monticore.java.javadsl._ast.ASTEnumDeclaration;
import de.monticore.java.javadsl._ast.ASTExpressionStatement;
import de.monticore.java.javadsl._ast.ASTFieldDeclaration;
import de.monticore.java.javadsl._ast.ASTFinallyBlockOnlyHandler;
import de.monticore.java.javadsl._ast.ASTForInitByExpressions;
import de.monticore.java.javadsl._ast.ASTForStatement;
import de.monticore.java.javadsl._ast.ASTFormalParameter;
import de.monticore.java.javadsl._ast.ASTFormalParameterListing;
import de.monticore.java.javadsl._ast.ASTFormalParameters;
import de.monticore.java.javadsl._ast.ASTIdentifierAndTypeArgument;
import de.monticore.java.javadsl._ast.ASTIfStatement;
import de.monticore.java.javadsl._ast.ASTImportDeclaration;
import de.monticore.java.javadsl._ast.ASTInnerCreator;
import de.monticore.java.javadsl._ast.ASTInterfaceBody;
import de.monticore.java.javadsl._ast.ASTInterfaceDeclaration;
import de.monticore.java.javadsl._ast.ASTJavaBlock;
import de.monticore.java.javadsl._ast.ASTJavaDSLNode;
import de.monticore.java.javadsl._ast.ASTLabeledStatement;
import de.monticore.java.javadsl._ast.ASTLastFormalParameter;
import de.monticore.java.javadsl._ast.ASTLocalVariableDeclaration;
import de.monticore.java.javadsl._ast.ASTLocalVariableDeclarationStatement;
import de.monticore.java.javadsl._ast.ASTMethodDeclaration;
import de.monticore.java.javadsl._ast.ASTMethodSignature;
import de.monticore.java.javadsl._ast.ASTPackageDeclaration;
import de.monticore.java.javadsl._ast.ASTPrimitiveModifier;
import de.monticore.java.javadsl._ast.ASTResource;
import de.monticore.java.javadsl._ast.ASTReturnStatement;
import de.monticore.java.javadsl._ast.ASTSwitchStatement;
import de.monticore.java.javadsl._ast.ASTSynchronizedStatement;
import de.monticore.java.javadsl._ast.ASTThrowStatement;
import de.monticore.java.javadsl._ast.ASTThrows;
import de.monticore.java.javadsl._ast.ASTTryStatement;
import de.monticore.java.javadsl._ast.ASTTryStatementWithResources;
import de.monticore.java.javadsl._ast.ASTVariableDeclarator;
import de.monticore.java.javadsl._ast.ASTWhileStatement;
import de.monticore.java.javadsl._visitor.JavaDSLVisitor;
import de.monticore.prettyprint.CommentPrettyPrinter;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.types.types._ast.ASTArrayType;
import de.monticore.types.types._ast.ASTTypeVariableDeclaration;
import de.monticore.types.types._ast.ASTVoidType;
import de.monticore.types.types._ast.ASTWildcardType;
import de.se_rwth.commons.Names;

/*
 * $Id: JavaDSLWriterVisitor.java,v 1.4 2008-07-17 08:34:01 cficek Exp $
 */

public class JavaDSLPrettyPrinter extends MCExpressionsPrettyPrinter implements
    JavaDSLVisitor {

  private boolean WRITE_COMMENTS = false;
  
  private JavaDSLVisitor realThis = this;

  public JavaDSLPrettyPrinter(IndentPrinter out) {
    super(out);
    setWriteComments(true);
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
    getPrinter().print(Names.getQualifiedName(a.getQualifiedName().getPartList()));
    getPrinter().println(";");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTArrayType a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    a.getComponentType().accept(getRealThis());
    printDimensions(a.getDimensions());
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTInterfaceDeclaration a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    printSeparated(a.getModifierList().iterator(), " ");
    getPrinter().print("interface ");
    printNode(a.getName());
    if (a.isPresentTypeParameters()) {
      a.getTypeParameters().accept(getRealThis());
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
    if (a.isPresentTypeParameters()) {
      a.getTypeParameters().accept(getRealThis());
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
  public void handle(ASTTypeVariableDeclaration a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    printNode(a.getName());
    if (!a.getUpperBoundList().isEmpty()) {
      getPrinter().print(" extends ");
      printList(a.getUpperBoundList().iterator(), ".");

    }
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
    a.getType().accept(getRealThis());
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
    a.getType().accept(getRealThis());
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
    a.getType().accept(getRealThis());
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
    if (a.isPresentTypeParameters()) {
      a.getTypeParameters().accept(getRealThis());
    }
    a.getReturnType().accept(getRealThis());
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
    printList(a.getQualifiedNameList().iterator(), ", ");
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
    a.getType().accept(getRealThis());
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
    if (a.isPresentTypeParameters()) {
      a.getTypeParameters().accept(getRealThis());
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
    a.getType().accept(getRealThis());
    getPrinter().print(" ");
    printSeparated(a.getVariableDeclaratorList().iterator(), ", ");
    getPrinter().println(";");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTConstDeclaration a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    printSeparated(a.getModifierList().iterator(), " ");
    a.getType().accept(getRealThis());
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
    a.getType().accept(getRealThis());
    getPrinter().print(" ");
    a.getDeclaratorId().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTLastFormalParameter a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    printSeparated(a.getPrimitiveModifierList().iterator(), " ");
    a.getType().accept(getRealThis());
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
  public void handle(ASTVoidType a) {
    getPrinter().print("void");
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
  public void handle(ASTWildcardType a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().print("?");
    if (a.isPresentLowerBound()) {
      getPrinter().print(" super ");
      a.getLowerBound().accept(getRealThis());
      if (a.getUpperBound() != null) {
        getPrinter().print(",");
      }
    }
    if (a.isPresentUpperBound()) {
      getPrinter().print(" extends ");
      a.getUpperBound().accept(getRealThis());
    }
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
    a.getQualifiedName().accept(getRealThis());
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
    printList(a.getQualifiedNameList().iterator(), "|");
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
    if (a.isPresentPrimitiveType()) {
      a.getPrimitiveType().accept(getRealThis());
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
