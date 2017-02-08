package typeSystemTestModels.monticore.valid;

import java.lang.String;

public interface SymbolKind {
  SymbolKind KIND = new SymbolKind() {
  };

  String getName();

  boolean isKindOf(SymbolKind kind);

}
