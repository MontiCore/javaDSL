package typeSystemTestModels.monticore.invalid;

import de.monticore.symboltable.*;
import de.monticore.symboltable.types.TypeSymbolKind;

import java.util.ArrayList;

public interface TypeSymbol extends ArrayList{
  TypeSymbolKind KIND = new TypeSymbolKind();
}
