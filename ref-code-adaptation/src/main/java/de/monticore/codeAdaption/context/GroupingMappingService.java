package de.monticore.codeAdaption.context;

import de.monticore.codeAdaption.handler.multiIncarnation.IncarnationContext;
import de.monticore.symboltable.ISymbol;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Computes concrete-type to grouping-type replacements from incarnation contexts. */
public final class GroupingMappingService {

  public Map<String, String> compute(Map<String, IncarnationContext> mappingContexts) {
    Map<String, String> result = new HashMap<>();
    for (IncarnationContext context : mappingContexts.values()) {
      for (Map.Entry<ISymbol, List<ISymbol>> entry :
          context.getReferenceToIncarnations().entrySet()) {
        List<ISymbol> incarnations = entry.getValue();
        if (incarnations == null) {
          continue;
        }
        for (ISymbol incarnation : incarnations) {
          if (incarnation == null) {
            continue;
          }
          var groupingType = context.findGroupingTypeForImplementer(incarnation.getName());
          if (groupingType.isPresent() && !groupingType.get().equals(incarnation.getName())) {
            result.put(incarnation.getName(), groupingType.get());
          }
        }
      }
    }
    return result;
  }
}
