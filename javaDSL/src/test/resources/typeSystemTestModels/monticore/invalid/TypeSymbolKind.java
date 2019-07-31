/* (c) https://github.com/MontiCore/monticore */

package typeSystemTestModels.monticore.invalid;

import java.lang.String;
import java.util.List;

public class TypeSymbolKind extends List implements SymbolKind {
  private static final String NAME = "de.monticore.symboltable.types.TypeSymbolKind";

  protected TypeSymbolKind() {
  }

  public boolean isKindOf(SymbolKind kind) {
   // return "de.monticore.symboltable.types.TypeSymbolKind".equals(kind.getName()) || super.isKindOf(kind);
    return "Hello";
  }
}
