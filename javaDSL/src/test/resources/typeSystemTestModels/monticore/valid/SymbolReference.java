/* (c) https://github.com/MontiCore/monticore */

package typeSystemTestModels.monticore.valid;

import de.monticore.ast.ASTNode;
import de.monticore.symboltable.Scope;
import de.monticore.symboltable.Symbol;
import java.util.Optional;

public interface SymbolReference<T extends Symbol> {
  String getName();

  Optional<ASTNode> getAstNode();

  void setAstNode(ASTNode var1);

  T getReferencedSymbol();

  boolean existsReferencedSymbol();

  boolean isReferencedSymbolLoaded();

  Scope getEnclosingScope();
}
