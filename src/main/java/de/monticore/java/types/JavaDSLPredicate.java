/*
 * Copyright (c) 2017 RWTH Aachen. All rights reserved.
 *
 * http://www.se-rwth.de/
 */
package de.monticore.java.types;

import java.util.Optional;
import java.util.function.Predicate;

import de.monticore.ast.ASTNode;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.symboltable.ScopeSpanningSymbol;
import de.monticore.symboltable.Symbol;

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
    Optional<? extends ScopeSpanningSymbol> scopeSpanningSymbol = symbol.getEnclosingScope().getSpanningSymbol();
    if(scopeSpanningSymbol.isPresent()) {
      if(scopeSpanningSymbol.get().isKindOf(JavaTypeSymbol.KIND)) {
        return true;
        
      }
    }
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
