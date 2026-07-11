package de.monticore.codeAdaption.context;

import de.monticore.codeAdaption.handler.multiIncarnation.IncarnationContext;
import java.util.LinkedHashMap;
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
    context.getGroupingMappings().entrySet().stream()
        .sorted(Map.Entry.comparingByKey(java.util.Comparator.comparing(key -> key.signature())))
        .forEach(
            entry -> {
              String concrete = entry.getKey().getName();
              String grouping = entry.getValue().key().getName();
              if (!grouping.equals(concrete)) {
                putUnambiguous(result, concrete, grouping);
              }
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
