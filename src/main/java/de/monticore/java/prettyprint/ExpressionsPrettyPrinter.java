/*
 * ******************************************************************************
 * MontiCore Language Workbench
 * Copyright (c) 2015, MontiCore, All rights reserved.
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
package de.monticore.java.prettyprint;

import java.util.Iterator;

import de.monticore.java.expressions._ast.ASTAddExpression;
import de.monticore.java.expressions._ast.ASTArguments;
import de.monticore.java.expressions._ast.ASTArrayExpression;
import de.monticore.java.expressions._ast.ASTAssignmentExpression;
import de.monticore.java.expressions._ast.ASTBinaryAndOpExpression;
import de.monticore.java.expressions._ast.ASTBinaryOrOpExpression;
import de.monticore.java.expressions._ast.ASTBinaryXorOpExpression;
import de.monticore.java.expressions._ast.ASTBooleanAndOpExpression;
import de.monticore.java.expressions._ast.ASTBooleanNotExpression;
import de.monticore.java.expressions._ast.ASTBooleanOrOpExpression;
import de.monticore.java.expressions._ast.ASTCallExpression;
import de.monticore.java.expressions._ast.ASTComparisonExpression;
import de.monticore.java.expressions._ast.ASTConditionalExpression;
import de.monticore.java.expressions._ast.ASTExplicitGenericInvocation;
import de.monticore.java.expressions._ast.ASTExplicitGenericInvocationExpression;
import de.monticore.java.expressions._ast.ASTExplicitGenericInvocationSuffix;
import de.monticore.java.expressions._ast.ASTExpressionsNode;
import de.monticore.java.expressions._ast.ASTIdentityExpression;
import de.monticore.java.expressions._ast.ASTInstanceofExpression;
import de.monticore.java.expressions._ast.ASTMultExpression;
import de.monticore.java.expressions._ast.ASTPrefixExpression;
import de.monticore.java.expressions._ast.ASTPrimaryExpression;
import de.monticore.java.expressions._ast.ASTQualifiedNameExpression;
import de.monticore.java.expressions._ast.ASTShiftExpression;
import de.monticore.java.expressions._ast.ASTSuffixExpression;
import de.monticore.java.expressions._ast.ASTSuperExpression;
import de.monticore.java.expressions._ast.ASTSuperSuffix;
import de.monticore.java.expressions._ast.ASTThisExpression;
import de.monticore.java.expressions._ast.ASTTypeCastExpression;
import de.monticore.java.expressions._visitor.ExpressionsVisitor;
import de.monticore.prettyprint.CommentPrettyPrinter;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.types.prettyprint.TypesPrettyPrinterConcreteVisitor;

public class ExpressionsPrettyPrinter extends TypesPrettyPrinterConcreteVisitor implements
ExpressionsVisitor {

  private boolean WRITE_COMMENTS = false;
  
  private ExpressionsVisitor realThis = this;

  public ExpressionsPrettyPrinter(IndentPrinter out) {
    super(out);
    setWriteComments(true);
  }

  protected void printDimensions(int dims) {
    for (int i = 0; i < dims; i++) {
      getPrinter().print("[]");
    }
  }

  @Override
  public void handle(ASTPrimaryExpression a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    if (a.getExpression().isPresent()) {
      getPrinter().print("(");
      a.getExpression().get().accept(getRealThis());
      getPrinter().print(")");
    }
    if (a.isThis()) {
      getPrinter().print("this");
    }
    if (a.isSuper()) {
      getPrinter().print("super");
    }
    if (a.getLiteral().isPresent()) {
      a.getLiteral().get().accept(getRealThis());
    }
    if (a.getName().isPresent()) {
      printNode(a.getName().get());
    }
    if (a.getReturnType().isPresent()) {
      a.getReturnType().get().accept(getRealThis());
      getPrinter().print(".class");
    }
    if (a.getExplicitGenericInvocation().isPresent()) {
      a.getExplicitGenericInvocation().get().accept(getRealThis());
    }
    if (a.getTypeArguments().isPresent()) {
      a.getTypeArguments().get().accept(getRealThis());
      getPrinter().print("this");
      a.getArguments().get().accept(getRealThis());
    }
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }

  /**
   * @see de.monticore.java.expressions._visitor.ExpressionsVisitor#handle(de.monticore.java.expressions._ast.ASTQualifiedNameExpression)
   */
  @Override
  public void handle(ASTQualifiedNameExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getExpression().accept(getRealThis());
    getPrinter().print(".");
    printNode(node.getName());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  /**
   * @see de.monticore.java.expressions._visitor.ExpressionsVisitor#handle(de.monticore.java.expressions._ast.ASTThisExpression)
   */
  @Override
  public void handle(ASTThisExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getExpression().accept(getRealThis());
    getPrinter().print(".this ");
    CommentPrettyPrinter.printPostComments(node, getPrinter());

  }

  /**
   * @see de.monticore.java.expressions._visitor.ExpressionsVisitor#handle(de.monticore.java.expressions._ast.ASTSuperExpression)
   */
  @Override
  public void handle(ASTSuperExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getExpression().accept(getRealThis());
    getPrinter().print(".super ");
    node.getSuperSuffix().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  /**
   * @see de.monticore.java.expressions._visitor.ExpressionsVisitor#handle(de.monticore.java.expressions._ast.ASTExplicitGenericInvocationExpression)
   */
  @Override
  public void handle(ASTExplicitGenericInvocationExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getExpression().accept(getRealThis());
    getPrinter().print(".");
    node.getExplicitGenericInvocation().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  /**
   * @see de.monticore.java.expressions._visitor.ExpressionsVisitor#handle(de.monticore.java.expressions._ast.ASTArrayExpression)
   */
  @Override
  public void handle(ASTArrayExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getExpression().accept(getRealThis());
    getPrinter().print("[");
    node.getIndexExpression().accept(getRealThis());
    getPrinter().print("]");
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  /**
   * @see de.monticore.java.expressions._visitor.ExpressionsVisitor#handle(de.monticore.java.expressions._ast.ASTCallExpression)
   */
  @Override
  public void handle(ASTCallExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getExpression().accept(getRealThis());
    getPrinter().print("(");
    printExpressionsList(node.getParameterExpression().iterator(), ",");
    getPrinter().print(")");
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  /**
   * @see de.monticore.java.expressions._visitor.ExpressionsVisitor#handle(de.monticore.java.expressions._ast.ASTTypeCastExpression)
   */
  @Override
  public void handle(ASTTypeCastExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("(");
    node.getTypeCastType().accept(getRealThis());
    getPrinter().print(")");
    node.getExpression().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  /**
   * @see de.monticore.java.expressions._visitor.ExpressionsVisitor#handle(de.monticore.java.expressions._ast.ASTSuffixExpression)
   */
  @Override
  public void handle(ASTSuffixExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getExpression().accept(getRealThis());
    getPrinter().print(node.getSuffixOp().orElse(""));
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  /**
   * @see de.monticore.java.expressions._visitor.ExpressionsVisitor#handle(de.monticore.java.expressions._ast.ASTPrefixExpression)
   */
  @Override
  public void handle(ASTPrefixExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print(node.getPrefixOp().orElse(""));
    node.getExpression().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  /**
   * @see de.monticore.java.expressions._visitor.ExpressionsVisitor#handle(de.monticore.java.expressions._ast.ASTBooleanNotExpression)
   */
  @Override
  public void handle(ASTBooleanNotExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("!");
    node.getExpression().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  /**
   * @see de.monticore.java.expressions._visitor.ExpressionsVisitor#handle(de.monticore.java.expressions._ast.ASTMultExpression)
   */
  @Override
  public void handle(ASTMultExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getLeftExpression().accept(getRealThis());
    getPrinter().print(node.getMultiplicativeOp().orElse(""));
    node.getRightExpression().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  /**
   * @see de.monticore.java.expressions._visitor.ExpressionsVisitor#handle(de.monticore.java.expressions._ast.ASTAddExpression)
   */
  @Override
  public void handle(ASTAddExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getLeftExpression().accept(getRealThis());
    getPrinter().print(node.getAdditiveOp().orElse(""));
    node.getRightExpression().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  /**
   * @see de.monticore.java.expressions._visitor.ExpressionsVisitor#handle(de.monticore.java.expressions._ast.ASTShiftExpression)
   */
  @Override
  public void handle(ASTShiftExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getLeftExpression().accept(getRealThis());
    getPrinter().print(node.getShiftOp().orElse(""));
    node.getRightExpression().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  /**
   * @see de.monticore.java.expressions._visitor.ExpressionsVisitor#handle(de.monticore.java.expressions._ast.ASTComparisonExpression)
   */
  @Override
  public void handle(ASTComparisonExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getLeftExpression().accept(getRealThis());
    getPrinter().print(node.getComparison().orElse(""));
    node.getRightExpression().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  /**
   * @see de.monticore.java.expressions._visitor.ExpressionsVisitor#handle(de.monticore.java.expressions._ast.ASTInstanceofExpression)
   */
  @Override
  public void handle(ASTInstanceofExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getExpression().accept(getRealThis());
    getPrinter().print(" instanceof ");
    node.getInstanceofType().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  /**
   * @see de.monticore.java.expressions._visitor.ExpressionsVisitor#handle(de.monticore.java.expressions._ast.ASTIdentityExpression)
   */
  @Override
  public void handle(ASTIdentityExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getLeftExpression().accept(getRealThis());
    getPrinter().print(node.getIdentityTest().orElse(""));
    node.getRightExpression().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  /**
   * @see de.monticore.java.expressions._visitor.ExpressionsVisitor#handle(de.monticore.java.expressions._ast.ASTBinaryAndOpExpression)
   */
  @Override
  public void handle(ASTBinaryAndOpExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getLeftExpression().accept(getRealThis());
    getPrinter().print(node.getBinaryAndOp());
    node.getRightExpression().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  /**
   * @see de.monticore.java.expressions._visitor.ExpressionsVisitor#handle(de.monticore.java.expressions._ast.ASTBinaryXorOpExpression)
   */
  @Override
  public void handle(ASTBinaryXorOpExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getLeftExpression().accept(getRealThis());
    getPrinter().print(node.getBinaryXorOp());
    node.getRightExpression().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  /**
   * @see de.monticore.java.expressions._visitor.ExpressionsVisitor#handle(de.monticore.java.expressions._ast.ASTBinaryOrOpExpression)
   */
  @Override
  public void handle(ASTBinaryOrOpExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getLeftExpression().accept(getRealThis());
    getPrinter().print(node.getBinaryOrOp());
    node.getRightExpression().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  /**
   * @see de.monticore.java.expressions._visitor.ExpressionsVisitor#handle(de.monticore.java.expressions._ast.ASTBooleanAndOpExpression)
   */
  @Override
  public void handle(ASTBooleanAndOpExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getLeftExpression().accept(getRealThis());
    getPrinter().print(node.getBooleanAndOp());
    node.getRightExpression().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  /**
   * @see de.monticore.java.expressions._visitor.ExpressionsVisitor#handle(de.monticore.java.expressions._ast.ASTBooleanOrOpExpression)
   */
  @Override
  public void handle(ASTBooleanOrOpExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getLeftExpression().accept(getRealThis());
    getPrinter().print(node.getBooleanOrOp());
    node.getRightExpression().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  /**
   * @see de.monticore.java.expressions._visitor.ExpressionsVisitor#handle(de.monticore.java.expressions._ast.ASTConditionalExpression)
   */
  @Override
  public void handle(ASTConditionalExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getCondition().accept(getRealThis());
    getPrinter().print("?");
    node.getTrueExpression().accept(getRealThis());
    getPrinter().print(": ");
    node.getFalseExpression().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  /**
   * @see de.monticore.java.expressions._visitor.ExpressionsVisitor#handle(de.monticore.java.expressions._ast.ASTAssignmentExpression)
   */
  @Override
  public void handle(ASTAssignmentExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getLeftExpression().accept(getRealThis());
    getPrinter().print(node.getAssignment().orElse(""));
    node.getRightExpression().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  /**
   * @see de.monticore.java.expressions._visitor.ExpressionsVisitor#handle(de.monticore.java.expressions._ast.ASTExplicitGenericInvocation)
   */
  @Override
  public void handle(ASTExplicitGenericInvocation node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getTypeArguments().accept(getRealThis());
    node.getExplicitGenericInvocationSuffix().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  /**
   * @see de.monticore.java.expressions._visitor.ExpressionsVisitor#handle(de.monticore.java.expressions._ast.ASTExplicitGenericInvocationSuffix)
   */
  @Override
  public void handle(ASTExplicitGenericInvocationSuffix node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    if (node.getSuperSuffix().isPresent()) {
      getPrinter().print(" super ");
      node.getSuperSuffix().get().accept(getRealThis());
    }
    if (node.getName().isPresent()) {
      printNode(node.getName().get());
      node.getArguments().get().accept(getRealThis());    
    }
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }

  /**
   * @see de.monticore.java.expressions._visitor.ExpressionsVisitor#handle(de.monticore.java.expressions._ast.ASTSuperSuffix)
   */
  @Override
  public void handle(ASTSuperSuffix node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    if (node.getName().isPresent()) {
      getPrinter().print(".");
      printNode(node.getName().get());
    }
    if (node.getArguments().isPresent()) {
      node.getArguments().get().accept(getRealThis());
    }
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }


  @Override
  public void handle(ASTArguments a) {
    CommentPrettyPrinter.printPreComments(a, getPrinter());
    getPrinter().print("(");
    printExpressionsList(a.getExpressions().iterator(), ", ");
    getPrinter().print(")");
    CommentPrettyPrinter.printPostComments(a, getPrinter());
  }
  
  protected void printNode(String s) {
    getPrinter().print(s);
  }

  protected void printExpressionsList(Iterator<? extends ASTExpressionsNode> iter, String separator) {
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
  public String prettyprint(ASTExpressionsNode a) {
    getPrinter().clearBuffer();
    a.accept(getRealThis());
    return getPrinter().getContent();
  }

  @Override
  public void setRealThis(ExpressionsVisitor realThis) {
    this.realThis = realThis;
  }

  @Override
  public ExpressionsVisitor getRealThis() {
    return realThis;
  }
  
  
}
