package de.monticore.java.utils;

import de.monticore.ast.ASTNode;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.lambdaexpressions._ast.ASTLambdaExpression;
import de.monticore.expressions.lambdaexpressions._visitor.LambdaExpressionsHandler;
import de.monticore.expressions.lambdaexpressions._visitor.LambdaExpressionsTraverser;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.*;
import de.monticore.java.javadsl._visitor.JavaDSLHandler;
import de.monticore.java.javadsl._visitor.JavaDSLTraverser;
import de.monticore.javalight._ast.ASTConstructorDeclaration;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.javalight._visitor.JavaLightHandler;
import de.monticore.javalight._visitor.JavaLightTraverser;
import de.monticore.javalight.cocos.ReturnStatementCollectionVisitor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Collects the return statements of the body of a method, constructor, or lambda. The return
 * statements of nested lambdas, methods, and types (local, anonymous, and inner classes) belong
 * to them and are not collected: the traverser does not traverse into these sub scopes.
 */
public final class ReturnStatementCollector {

  private ReturnStatementCollector() {
  }

  /**
   * @param body the body, e.g., the block of a method or lambda (not the method or lambda
   *             itself, as it would not be traversed)
   * @return the return statements in source order, each with its optional expression
   */
  public static Map<ASTNode, Optional<ASTExpression>> getReturnExpressions(ASTNode body) {
    Map<ASTNode, Optional<ASTExpression>> returnExpressions = new LinkedHashMap<>();
    JavaDSLTraverser traverser = JavaDSLMill.traverser();
    traverser.add4MCReturnStatements(new ReturnStatementCollectionVisitor(returnExpressions));
    traverser.setJavaDSLHandler(new SkipNestedTypes());
    traverser.setJavaLightHandler(new SkipNestedMethods());
    traverser.setLambdaExpressionsHandler(new SkipNestedLambdas());
    body.accept(traverser);
    return returnExpressions;
  }

  /** Does not traverse into type declarations and anonymous classes. */
  protected static class SkipNestedTypes implements JavaDSLHandler {

    protected JavaDSLTraverser traverser;

    @Override
    public JavaDSLTraverser getTraverser() {
      return traverser;
    }

    @Override
    public void setTraverser(JavaDSLTraverser traverser) {
      this.traverser = traverser;
    }

    @Override
    public void traverse(ASTClassDeclaration node) {
    }

    @Override
    public void traverse(ASTInterfaceDeclaration node) {
    }

    @Override
    public void traverse(ASTEnumDeclaration node) {
    }

    @Override
    public void traverse(ASTRecordDeclaration node) {
    }

    @Override
    public void traverse(ASTAnnotationTypeDeclaration node) {
    }

    @Override
    public void traverse(ASTAnonymousClass node) {
    }
  }

  /**
   * Does not traverse into methods and constructors, e.g., of the class body of
   * {@code outer.new Inner() { ... }}.
   */
  protected static class SkipNestedMethods implements JavaLightHandler {

    protected JavaLightTraverser traverser;

    @Override
    public JavaLightTraverser getTraverser() {
      return traverser;
    }

    @Override
    public void setTraverser(JavaLightTraverser traverser) {
      this.traverser = traverser;
    }

    @Override
    public void traverse(ASTMethodDeclaration node) {
    }

    @Override
    public void traverse(ASTConstructorDeclaration node) {
    }
  }

  /** Does not traverse into lambdas. */
  protected static class SkipNestedLambdas implements LambdaExpressionsHandler {

    protected LambdaExpressionsTraverser traverser;

    @Override
    public LambdaExpressionsTraverser getTraverser() {
      return traverser;
    }

    @Override
    public void setTraverser(LambdaExpressionsTraverser traverser) {
      this.traverser = traverser;
    }

    @Override
    public void traverse(ASTLambdaExpression node) {
    }
  }
}
