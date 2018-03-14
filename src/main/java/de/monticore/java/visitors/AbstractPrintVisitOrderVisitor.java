/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.visitors;

import java.io.PrintStream;

import de.monticore.ast.ASTNode;

public class AbstractPrintVisitOrderVisitor {

  private final PrintStream stream;

  int indent = 0;

  public AbstractPrintVisitOrderVisitor(final PrintStream stream) {
    this.stream = stream;
  }

  public AbstractPrintVisitOrderVisitor() {
    this(System.out);
  }

  private static String spaces(final int indent) {
    String s = "";
    for (int i = 0; i < indent; i++) {
      s += "|   ";
    }
    return s;
  }

  public void visit(ASTNode node) {
    stream.println(spaces(indent) + "/"
        + node.getClass().getSimpleName());
    indent++;
  }

  public void endVisit(ASTNode node) {
    indent--;
    stream.println(spaces(indent) + "\\"
        + node.getClass().getSimpleName());
  }

  protected void print(String s) {
    stream.println(spaces(indent) + s);
  }
}
