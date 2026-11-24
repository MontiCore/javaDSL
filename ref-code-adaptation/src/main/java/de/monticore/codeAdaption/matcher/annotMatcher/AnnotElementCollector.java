package de.monticore.codeAdaption.matcher.annotMatcher;

import de.monticore.codeAdaption.utils.Constants;
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

/**
 * Decodes the literal attributes of one JavaDSL {@code @Adapt} annotation.
 *
 * <p>{@code ref} values are collected in declaration order because template placeholders consume
 * them from left to right. {@code template} and {@code genTemplate} must each contain exactly one
 * string literal, while {@code ignore} must contain exactly one boolean literal. Structural and
 * reference-resolution validation is performed later by {@code ValidAnnotation}.
 */
public class AnnotElementCollector implements JavaLightVisitor2 {

  private String template;
  private String genTemplate;
  private final List<String> references = new ArrayList<>();
  private boolean ignore;

  @Override
  public void visit(ASTElementValuePair node) {

    if (node.getName().equals(Constants.TEMPLATE)) {
      template = readSingleString(node.getElementValueOrExpr(), Constants.TEMPLATE);
    }

    if (node.getName().equals(Constants.IGNORE)) {
      ignore = readSingleBoolean(node.getElementValueOrExpr(), Constants.IGNORE);
    }

    if (node.getName().equals(Constants.REFERENCE)) {
      references.addAll(readStrings(node.getElementValueOrExpr()));
    }

    if (node.getName().equals(Constants.GENERATE_TEMPLATE)) {
      genTemplate = readSingleString(node.getElementValueOrExpr(), Constants.GENERATE_TEMPLATE);
    }
  }

  private static String readSingleString(ASTElementValueOrExpr value, String attribute) {
    List<String> strings = readStrings(value);
    if (strings.size() != 1) {
      throw new IllegalArgumentException(
          "@Adapt attribute '" + attribute + "' requires exactly one string literal");
    }
    return strings.get(0);
  }

  private static boolean readSingleBoolean(ASTElementValueOrExpr value, String attribute) {
    List<Boolean> booleans = new ArrayList<>();
    MCCommonLiteralsVisitor2 visitor =
        new MCCommonLiteralsVisitor2() {
          @Override
          public void visit(ASTBooleanLiteral node) {
            booleans.add(node.getValue());
          }
        };
    traverse(value, visitor);
    if (booleans.size() != 1) {
      throw new IllegalArgumentException(
          "@Adapt attribute '" + attribute + "' requires exactly one boolean literal");
    }
    return booleans.get(0);
  }

  private static List<String> readStrings(ASTElementValueOrExpr value) {
    List<String> strings = new ArrayList<>();
    MCCommonLiteralsVisitor2 visitor =
        new MCCommonLiteralsVisitor2() {
          @Override
          public void visit(ASTStringLiteral node) {
            strings.add(node.getValue());
          }
        };
    traverse(value, visitor);
    return strings;
  }

  private static void traverse(ASTElementValueOrExpr value, MCCommonLiteralsVisitor2 visitor) {
    JavaDSLTraverser traverser = JavaDSLMill.traverser();
    traverser.add4MCCommonLiterals(visitor);
    value.accept(traverser);
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
