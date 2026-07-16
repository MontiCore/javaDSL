package de.monticore.codeAdaption.context;

import de.monticore.codeAdaption.handler.multiIncarnation.IncarnationContext;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Derives the type-name replacements required when several concrete incarnations share a grouping
 * type.
 */
public final class GroupingMappingService {

  /**
   * Computes replacements for one mapping only, preventing grouping decisions from leaking into
   * another mapping.
   *
   * <p>Each entry maps a concrete type's simple name to the simple name of its grouping type.
   * Identity replacements are omitted.
   *
   * <p>For example, if the context groups concrete types {@code Professor} and {@code Student}
   * under {@code Person}, the result is {@code {Professor=Person, Student=Person}}. A context entry
   * {@code Person -> Person} would not be included because it does not require a rewrite.
   *
   * @param context the mapping-local incarnation and grouping context
   * @return insertion-ordered concrete-simple-name to grouping-simple-name replacements
   * @throws IllegalStateException if one concrete simple name resolves to different grouping types
   */
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

  /** Adds one rewrite while rejecting collisions caused by equal concrete simple names. */
  private static void putUnambiguous(Map<String, String> result, String concrete, String grouping) {
    String previous = result.putIfAbsent(concrete, grouping);
    if (previous != null && !previous.equals(grouping)) {
      throw new IllegalStateException(
          "Ambiguous grouping for concrete type '" + concrete + "': '" + previous + "' and '" + grouping + "'");
    }
  }
}
