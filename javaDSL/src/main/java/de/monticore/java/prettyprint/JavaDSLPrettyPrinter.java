/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.prettyprint;

import java.util.Iterator;

import de.monticore.ast.ASTNode;
import de.monticore.java.javadsl._ast.*;
import de.monticore.java.javadsl._visitor.JavaDSLHandler;
import de.monticore.java.javadsl._visitor.JavaDSLTraverser;
import de.monticore.java.javadsl._visitor.JavaDSLVisitor2;
import de.monticore.prettyprint.CommentPrettyPrinter;
import de.monticore.prettyprint.IndentPrinter;

import de.monticore.statements.mccommonstatements._ast.ASTConstantsMCCommonStatements;
import de.monticore.types.mcbasictypes._ast.ASTMCBasicTypesNode;
import de.monticore.types.mccollectiontypes._ast.ASTMCTypeArgument;
import de.se_rwth.commons.Names;

public class JavaDSLPrettyPrinter implements JavaDSLVisitor2, JavaDSLHandler {
  
  private JavaDSLTraverser traverser;

  @Override
  public JavaDSLTraverser getTraverser() {
    return this.traverser;
  }

  @Override
  public void setTraverser(JavaDSLTraverser traverser) {
    this.traverser = traverser;
  }

  private boolean WRITE_COMMENTS = false;

  protected IndentPrinter printer;

  public JavaDSLPrettyPrinter(IndentPrinter printer) {
    this.printer = printer;
  }
  
  public IndentPrinter getPrinter() {
    return this.printer;
  }

  public void setPrinter(IndentPrinter printer) {
    this.printer = printer;
  }
  
  protected void printNode(String s) {
    getPrinter().print(s);
  }
  
  protected void printList(Iterator<? extends ASTMCBasicTypesNode> iter, String seperator) {
    // print by iterate through all items
    String sep = "";
    while (iter.hasNext()) {
      getPrinter().print(sep);
      iter.next().accept(getTraverser());
      sep = seperator;
    }
  }

  @Override
  public void handle(ASTOrdinaryCompilationUnit a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    if (a.isPresentPackageDeclaration()) {
      a.getPackageDeclaration().accept(getTraverser());
    }
    printSeparated(a.getImportDeclarationList().iterator(), "");
    printSeparated(a.getTypeDeclarationList().iterator(), "");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTModularCompilationUnit node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    printSeparated(node.getImportDeclarationList().iterator(), "");
    node.getModuleDeclaration().accept(getTraverser());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTPackageDeclaration a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    printSeparated(a.getAnnotationList().iterator(), "");
    getPrinter().print("package ");
    getPrinter().print(Names.getQualifiedName(a.getMCQualifiedName().getPartsList()));
    getPrinter().println(";");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTInterfaceDeclaration a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    printSeparated(a.getJavaModifierList().iterator(), " ");
    getPrinter().print("interface ");
    printNode(a.getName());
    if (a.isPresentTypeParameters()) {
      a.getTypeParameters().accept(getTraverser());
    }
    if (!a.getExtendedInterfaceList().isEmpty()) {
      getPrinter().print(" extends ");
      printList(a.getExtendedInterfaceList().iterator(), ", ");
    }
    a.getInterfaceBody().accept(getTraverser());
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
    a.getJavaBlock().accept(getTraverser());
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTClassDeclaration a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    printSeparated(a.getJavaModifierList().iterator(), " ");
    getPrinter().print("class ");
    printNode(a.getName());
    if (a.isPresentTypeParameters()) {
      a.getTypeParameters().accept(getTraverser());
    }
    if (a.isPresentSuperClass()) {
      getPrinter().print(" extends ");
      a.getSuperClass().accept(getTraverser());
    }
    if (!a.getImplementedInterfaceList().isEmpty()) {
      getPrinter().print(" implements ");
      printList(a.getImplementedInterfaceList().iterator(), ", ");
    }

    a.getClassBody().accept(getTraverser());
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
    printSeparated(a.getJavaModifierList().iterator(), " ");
    getPrinter().print("@ interface ");
    printNode(a.getName());
    a.getAnnotationTypeBody().accept(getTraverser());
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
    printSeparated(a.getJavaModifierList().iterator(), " ");
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
      a.getEnumBody().accept(getTraverser());
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
      a.getArguments().accept(getTraverser());
    }
    if (a.isPresentClassBody()) {
      a.getClassBody().accept(getTraverser());
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
    printSeparated(a.getJavaModifierList().iterator(), " ");
    a.getMCType().accept(getTraverser());
    getPrinter().print(" ");
    printNode(a.getName());
    getPrinter().print("()");
    if (a.isPresentDefaultValue()) {
      a.getDefaultValue().accept(getTraverser());
    }
    getPrinter().print(";");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTAnnotationConstant a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    printSeparated(a.getJavaModifierList().iterator(), " ");
    a.getMCType().accept(getTraverser());
    printSeparated(a.getVariableDeclaratorList().iterator(), ", ");
    getPrinter().println("");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTDefaultValue a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().print(" default ");
    a.getElementValueOrExpr().accept(getTraverser());
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTJavaBlock a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().println("{");
    getPrinter().indent();
    printSeparated(a.getMCBlockStatementList().iterator(), "");
    getPrinter().unindent();
    getPrinter().println("}");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTLabeledStatement a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    printNode(a.getLabel());
    getPrinter().print(": ");
    a.getStatement().accept(getTraverser());
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTTryStatement a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().println("try ");
    a.getJavaBlock().accept(getTraverser());
    getPrinter().println();
    a.getExceptionHandler().accept(getTraverser());
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
      a.getFinallyBlock().accept(getTraverser());
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
    a.getFinallyBlock().accept(getTraverser());
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
    a.getJavaBlock().accept(getTraverser());
    printSeparated(a.getCatchClauseList().iterator(), "");
    if (a.isPresentFinallyBlock()) {
      getPrinter().println();
      getPrinter().println("finally");
      getPrinter().indent();
      a.getFinallyBlock().accept(getTraverser());
      getPrinter().unindent();
    }
    getPrinter().println();
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTResource a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    printSeparated(a.getJavaModifierList().iterator(), " ");
    a.getMCType().accept(getTraverser());
    a.getDeclaratorId().accept(getTraverser());
    getPrinter().print(" = ");
    a.getExpression().accept(getTraverser());
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTIdentifierAndTypeArgument a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    printNode(a.getName());
    if (a.isPresentTypeArguments()) {
      a.getTypeArguments().accept(getTraverser());
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
      typeArg.accept(getTraverser());
    }
    getPrinter().print((">"));
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTFieldDeclaration a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    printSeparated(a.getJavaModifierList().iterator(), " ");
    a.getMCType().accept(getTraverser());
    getPrinter().print(" ");
    printSeparated(a.getVariableDeclaratorList().iterator(), ", ");
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
    a.getVariableInititializerOrExpression().accept(getTraverser());
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTImportDeclaration a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().print("import ");
    if (a.isStatic()) {
      getPrinter().print("static ");
    }
    a.getMCQualifiedName().accept(getTraverser());
    if (a.isSTAR()) {
      getPrinter().print(".*");
    }
    getPrinter().println(";");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

//  @Override
//  public void handle(ASTEmptyDeclaration a) {
//    CommentPrettyPrinter.printPreComments(a, getPrinter());
//    getPrinter().print(");");
//    CommentPrettyPrinter.printPostComments(a, getPrinter());
//  }

//  @Override
//  public void handle(ASTAssertStatement a) {
//    CommentPrettyPrinter.printPreComments(a, getPrinter());
//    getPrinter().print("assert ");
//    a.getAssertion().accept(getTraverser());
//    if (a.isPresentMessage()) {
//      getPrinter().print(" : ");
//      a.getMessage().accept(getTraverser());
//    }
//    getPrinter().println(";");
//    CommentPrettyPrinter.printPostComments(a, getPrinter());
//  }

//  @Override
//  public void handle(ASTContinueStatement a) {
//    CommentPrettyPrinter.printPreComments(a, getPrinter());
//    getPrinter().print("continue");
//    if (a.isPresentLabel()) {
//      printNode(a.getLabel());
//    }
//    getPrinter().println(";");
//    CommentPrettyPrinter.printPostComments(a, getPrinter());
//  }

//  @Override
//  public void handle(ASTSynchronizedStatement a) {
//    CommentPrettyPrinter.printPreComments(a, getPrinter());
//    getPrinter().print("synchronized (");
//    a.getExpression().accept(getTraverser());
//    getPrinter().print(") ");
//    a.getJavaBlock().accept(getTraverser());
//    CommentPrettyPrinter.printPostComments(a, getPrinter());
//  }

//  @Override
//  public void handle(ASTThrowStatement a) {
//    CommentPrettyPrinter.printPreComments(a, getPrinter());
//    getPrinter().print("throw ");
//    a.getExpression().accept(getTraverser());
//    getPrinter().println(";");
//    CommentPrettyPrinter.printPostComments(a, getPrinter());
//  }

//  @Override
//  public void handle(ASTCatchClause a) {
//    CommentPrettyPrinter.printPreComments(a, getPrinter());
//    getPrinter().print("catch (");
//    printSeparated(a.getPrimitiveModifierList().iterator(), " ");
//    a.getCatchType().accept(getTraverser());
//    getPrinter().print(" ");
//    printNode(a.getName());
//    getPrinter().print(") ");
//    a.getJavaBlock().accept(getTraverser());
//    CommentPrettyPrinter.printPostComments(a, getPrinter());
//  }
//
//  @Override
//  public void handle(ASTCatchType a) {
//    CommentPrettyPrinter.printPreComments(a, getPrinter());
//    printList(a.getMCQualifiedNameList().iterator(), "|");
//    CommentPrettyPrinter.printPostComments(a, getPrinter());
//  }

  @Override
  public void handle(ASTCreatedName a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    printSeparated(a.getIdentifierAndTypeArgumentList().iterator(), ".");
    if (a.isPresentMCPrimitiveType()) {
      a.getMCPrimitiveType().accept(getTraverser());
    }
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTInnerCreator a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().print("new ");
    if (a.isPresentFirstTypeArguments()) {
      a.getFirstTypeArguments().accept(getTraverser());
    }
    printNode(a.getName());
    if (a.isPresentSecondTypeArguments()) {
      a.getSecondTypeArguments().accept(getTraverser());
    }
    a.getClassCreatorRest().accept(getTraverser());
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  @Override
  public void handle(ASTExtType node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getMCType().accept(getTraverser());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTExtLiteral node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getLiteral().accept(getTraverser());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTExtReturnType node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getMCReturnType().accept(getTraverser());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  @Override
  public void handle(ASTMethodReferenceExpression node) {
    if (node.isPresentExpression()) {
      node.getExpression().accept(getTraverser());
    } else if (node.isPresentMCType()) {
      node.getMCType().accept(getTraverser());
    }

    getPrinter().print("::");

    if (node.isPresentTypeArguments()) {
      node.getTypeArguments().accept(getTraverser());
    }

    node.getMethodReferenceTarget().accept(getTraverser());
  }

  @Override
  public void visit(ASTConstructorReferenceTarget node) {
    getPrinter().print("new");
  }

  @Override
  public void handle(ASTMethodNameReferenceTarget node) {
    getPrinter().print(node.getName());
  }

  protected void printSeparated(Iterator<? extends ASTNode> iter, String separator) {
    // print by iterate through all items
    String sep = "";
    while (iter.hasNext()) {
      getPrinter().print(sep);
      iter.next().accept(getTraverser());
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
    a.accept(getTraverser());
    return getPrinter().getContent();
  }

  private String printModifier(int constant) {
    switch (constant) {
      case ASTConstantsMCCommonStatements.PRIVATE:
        return "private";
      case ASTConstantsMCCommonStatements.PUBLIC:
        return "public";
      case ASTConstantsMCCommonStatements.PROTECTED:
        return "protected";
      case ASTConstantsMCCommonStatements.STATIC:
        return "static";
      case ASTConstantsMCCommonStatements.TRANSIENT:
        return "transient";
      case ASTConstantsMCCommonStatements.FINAL:
        return "final";
      case ASTConstantsMCCommonStatements.ABSTRACT:
        return "abstract";
      case ASTConstantsMCCommonStatements.NATIVE:
        return "native";
      case ASTConstantsMCCommonStatements.THREADSAFE:
        return "threadsafe";
      case ASTConstantsMCCommonStatements.SYNCHRONIZED:
        return "synchronized";
      case ASTConstantsMCCommonStatements.VOLATILE:
        return "volatile";
      case ASTConstantsMCCommonStatements.STRICTFP:
        return "strictfp";
      default:
        return null;
    }

  }
  
}
