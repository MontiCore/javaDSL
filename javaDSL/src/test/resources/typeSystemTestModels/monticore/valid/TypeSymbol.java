/* (c) https://github.com/MontiCore/monticore */

package typeSystemTestModels.monticore.valid;

import de.monticore.symboltable.*;
import de.monticore.symboltable.types.TypeSymbolKind;

public interface TypeSymbol extends Symbol {
  TypeSymbolKind KIND = new TypeSymbolKind();
}
