package typeSystemTestModels.monticore.invalid;

import java.lang.String;

public interface SymbolKind {
  SymbolKind KIND = new SymbolKind() {
  };

  String getName();

  String getName();

  boolean isKindOf(SymbolKind kind);

  public interface SymbolKind1{

    public class SymbolKind1 {
      }
  }

}
