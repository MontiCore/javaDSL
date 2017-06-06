package typeSystemTestModels.monticore.invalid;

import de.monticore.ast.ASTNode;
import de.monticore.symboltable.MutableScope;
import de.monticore.symboltable.Scope;
import de.monticore.symboltable.SymbolKind;
import de.monticore.symboltable.modifiers.AccessModifier;
import de.se_rwth.commons.SourcePosition;
import de.se_rwth.commons.logging.Log;

import java.util.Optional;

public interface Symbol {


  static String getName();

  native String getPackageName();

  final String getFullName();

  SymbolKind getKind();

//  default boolean isKindOf(SymbolKind kind) {
//    return this.getKind().isKindOf((SymbolKind) Log.errorIfNull(kind));
//  }

//  default AccessModifier getAccessModifier() {
//    return AccessModifier.ALL_INCLUSION;
//  }

  void setAccessModifier(AccessModifier var1);

  void setAstNode(ASTNode var1);

  Optional<ASTNode> getAstNode();

//  default SourcePosition getSourcePosition() {
//    return this.getAstNode().isPresent()?((ASTNode)this.getAstNode().get()).get_SourcePositionStart():SourcePosition.getDefaultSourcePosition();
//  }

  Scope getEnclosingScope();

  void setEnclosingScope(MutableScope var1);
}
