package de.monticore.codeAdaption.matcher.annotMatcher;

import de.monticore.codeAdaption.utils.Constants;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTLiteralExpression;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._visitor.JavaDSLTraverser;
import de.monticore.javalight._ast.ASTElementValueOrExpr;
import de.monticore.javalight._ast.ASTElementValuePair;
import de.monticore.javalight._visitor.JavaLightVisitor2;
import de.monticore.literals.mccommonliterals._ast.ASTBooleanLiteral;
import de.monticore.literals.mccommonliterals._ast.ASTStringLiteral;
import de.monticore.literals.mccommonliterals._visitor.MCCommonLiteralsVisitor2;
import java.util.ArrayList;
import java.util.List;

/***
 * this class visits an ASTAnnotation or ASTJavaAnnotation and collects information.
 * (template, ignore-tag and references)
 */
public class AnnotElementCollector implements JavaLightVisitor2 {

  private String template;
  private String genTemplate;
  private final List<String> references = new ArrayList<>();
  private boolean ignore;

  @Override
  public void visit(ASTElementValuePair node) {

    if (node.getName().equals(Constants.TEMPLATE)) {
      ASTExpression val = node.getElementValueOrExpr().getExpression();
      template = ((ASTStringLiteral) ((ASTLiteralExpression) val).getLiteral()).getValue();
    }

    if (node.getName().equals(Constants.IGNORE)) {
      ASTExpression val = node.getElementValueOrExpr().getExpression();
      ignore = ((ASTBooleanLiteral) ((ASTLiteralExpression) val).getLiteral()).getValue();
    }

    if (node.getName().equals(Constants.REFERENCE)) {
      ASTElementValueOrExpr val = node.getElementValueOrExpr();
      MCCommonLiteralsVisitor2 visitor =
          new MCCommonLiteralsVisitor2() {
            @Override
            public void visit(ASTStringLiteral node) {
              references.add(node.getValue());
            }
          };
      JavaDSLTraverser traverser = JavaDSLMill.traverser();
      traverser.add4MCCommonLiterals(visitor);
      val.accept(traverser);
    }

    if (node.getName().equals(Constants.GENERATE_TEMPLATE)) {
      ASTExpression val = node.getElementValueOrExpr().getExpression();
      genTemplate = ((ASTStringLiteral) ((ASTLiteralExpression) val).getLiteral()).getValue();
    }
  }

  public List<String> getReferences() {
    return references;
  }

  public String getTemplate() {
    return template;
  }

  public String getGenTemplate() {
    return genTemplate;
  }

  public boolean isIgnore() {
    return ignore;
  }
}
