package de.monticore.codeAdaption.context;

import de.monticore.codeAdaption.handler.multiIncarnation.IncarnationContext;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Derives Java type-name replacements for groups of concrete incarnations.
 *
 * <p>An incarnation is a concrete CD type to which a reference type is mapped. If one reference
 * type has several incarnations, those incarnations form an incarnation group. Java declarations
 * that must work for the entire group cannot use the type of only one incarnation. They instead
 * use a common class or interface from the concrete CD that represents the group; this class calls
 * that common type the grouping type.
 */
public final class GroupingMappingService {

  /**
   * Computes replacements for one mapping only, preventing grouping decisions from leaking into
   * another mapping.
   *
   * <p>Each entry maps the simple type name of one concrete incarnation to the simple name of the
   * common type representing its incarnation group. The code updater uses these replacements in
   * shared declarations such as fields, parameters, return types, and collection element types, so
   * that the resulting Java declaration can hold or accept every incarnation in the group.
   *
   * <p>For example, assume the reference type {@code Payment} has the concrete incarnations {@code
   * CreditCard} and {@code Invoice}. If both are represented by the common concrete interface
   * {@code PaymentMethod}, this method returns {@code {CreditCard=PaymentMethod,
   * Invoice=PaymentMethod}}. A shared parameter can then be written as {@code PaymentMethod payment}
   * instead of being tied to only {@code CreditCard} or {@code Invoice}.
   *
   * <p>Identity replacements, such as {@code PaymentMethod -> PaymentMethod}, are omitted because
   * they do not require a Java type rewrite.
   *
   * @param context the mapping-local incarnation and grouping context
   * @return insertion-ordered incarnation-simple-name to grouping-type-simple-name replacements
   * @throws IllegalStateException if one incarnation simple name resolves to different grouping
   *     types
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

  /** Adds one incarnation-to-grouping-type rewrite while rejecting ambiguous simple names. */
  private static void putUnambiguous(Map<String, String> result, String concrete, String grouping) {
    String previous = result.putIfAbsent(concrete, grouping);
    if (previous != null && !previous.equals(grouping)) {
      throw new IllegalStateException(
          "Ambiguous grouping for concrete type '" + concrete + "': '" + previous + "' and '" + grouping + "'");
    }
  }
}
