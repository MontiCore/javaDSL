package de.monticore.codeAdaption.handler.multiIncarnation;

import de.monticore.symboltable.ISymbol;
import java.util.List;

/**
 * Strategy for selecting the appropriate incarnation when multiple are available.
 */
public interface IncarnationSelector {
  /**
   * Selects the most appropriate incarnation from available options.
   * @param referenceSymbol the reference symbol
   * @param availableIncarnations all available incarnations for this reference
   * @param context additional context information
   * @return the selected incarnation, or null if none can be selected
   */
  ISymbol selectIncarnation(ISymbol referenceSymbol, List<ISymbol> availableIncarnations, SelectionContext context);

  /**
   * Context information available during incarnation selection.
   */
  class SelectionContext {
    private final String mappingName;
    private final ISymbol concreteSymbol;

    public SelectionContext(String mappingName, ISymbol concreteSymbol) {
      this.mappingName = mappingName;
      this.concreteSymbol = concreteSymbol;
    }

    public String getMappingName() {
      return mappingName;
    }

    public ISymbol getConcreteSymbol() {
      return concreteSymbol;
    }
  }
}