/* (c) https://github.com/MontiCore/monticore */

package typeSystemTestModels.monticore.valid;

import java.lang.String;

public class TypeSymbolKind implements SymbolKind {
  private static final String NAME = "de.monticore.symboltable.types.TypeSymbolKind";

  protected TypeSymbolKind() {
  }

  public String getName() {
    return "de.monticore.symboltable.types.TypeSymbolKind";
  }

  public boolean isKindOf(SymbolKind kind) {
   // return "de.monticore.symboltable.types.TypeSymbolKind".equals(kind.getName()) || super.isKindOf(kind);
    return true;
  }
}
