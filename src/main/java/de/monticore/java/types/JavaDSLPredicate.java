/*
 * Copyright (c) 2017 RWTH Aachen. All rights reserved.
 *
 * http://www.se-rwth.de/
 */
package de.monticore.java.types;

import de.monticore.ast.ASTNode;
import de.monticore.symboltable.Symbol;

import java.util.function.Predicate;

/**
 * Predicate class for JavaDSL.
 */
public class JavaDSLPredicate implements Predicate<Symbol> {

  private ASTNode node;

  public JavaDSLPredicate(ASTNode node) {
    this.node = node;
  }

  @Override
  public boolean test(Symbol symbol) {
    int nodeLine = node.get_SourcePositionStart().getLine();
    int symbolLine = symbol.getAstNode().get().get_SourcePositionStart().getLine();
    int result = (nodeLine - symbolLine);
    if(result == 0) {
      int nodeColumn = node.get_SourcePositionStart().getColumn();
      int symbolColumn = symbol.getAstNode().get().get_SourcePositionStart().getColumn();
      result = nodeColumn - symbolColumn;
    }
    return result > 0;
  }
}
