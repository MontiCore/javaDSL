package de.monticore.codeAdaption.context;

import de.monticore.codeAdaption.handler.multiIncarnation.IncarnationContext;
import de.monticore.symboltable.ISymbol;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Computes concrete-type to grouping-type replacements from incarnation contexts. */
public final class GroupingMappingService {

  public Map<String, String> compute(Map<String, IncarnationContext> mappingContexts) {
    Map<String, String> result = new LinkedHashMap<>();
    mappingContexts.keySet().stream().sorted().forEach(mapping -> merge(result, compute(mappingContexts.get(mapping))));
    return result;
  }

  /** Computes replacements for one mapping only, preventing one mapping from leaking into another. */
  public Map<String, String> compute(IncarnationContext context) {
    Map<String, String> result = new LinkedHashMap<>();
    context.getReferenceToIncarnations().entrySet().stream()
        .sorted(Map.Entry.comparingByKey(java.util.Comparator.comparing(ISymbol::getName)))
        .forEach(entry -> {
        List<ISymbol> incarnations = entry.getValue();
        if (incarnations == null) {
          return;
        }
        incarnations.stream().filter(java.util.Objects::nonNull).sorted(java.util.Comparator.comparing(ISymbol::getName)).forEach(incarnation -> {
          if (incarnation == null) {
            return;
          }
          var groupingType = context.findGroupingTypeForImplementer(incarnation.getName());
          if (groupingType.isPresent() && !groupingType.get().equals(incarnation.getName())) {
            putUnambiguous(result, incarnation.getName(), groupingType.get());
          }
        });
      });
    return result;
  }

  private static void merge(Map<String, String> target, Map<String, String> additions) {
    additions.forEach((concrete, grouping) -> putUnambiguous(target, concrete, grouping));
  }

  private static void putUnambiguous(Map<String, String> result, String concrete, String grouping) {
    String previous = result.putIfAbsent(concrete, grouping);
    if (previous != null && !previous.equals(grouping)) {
      throw new IllegalStateException(
          "Ambiguous grouping for concrete type '" + concrete + "': '" + previous + "' and '" + grouping + "'");
    }
  }
}
