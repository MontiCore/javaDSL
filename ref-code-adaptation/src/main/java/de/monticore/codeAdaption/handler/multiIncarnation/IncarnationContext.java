package de.monticore.codeAdaption.handler.multiIncarnation;

import de.monticore.symboltable.ISymbol;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable incarnation mappings for one adaptation mapping. Stable keys are the only lookup
 * identity; MontiCore symbols are retained only as payload for AST operations.
 */
public class IncarnationContext {
  /**
   * Stable concrete identity plus the MontiCore symbol needed for AST operations.
   *
   * @param key stable key of the concrete CD element, for example the type key {@code CreditCard}
   * @param symbol concrete MontiCore symbol whose AST node is used during adaptation
   */
  public record MappedElement(StableElementKey key, ISymbol symbol) {
    public MappedElement {
      Objects.requireNonNull(key);
      Objects.requireNonNull(symbol);
    }
  }

  private final String mappingName;
  private final Map<StableElementKey, List<MappedElement>> mappings;
  private final Map<StableElementKey, MappedElement> groupingByImplementer;

  /**
   * Creates an immutable context for one mapping.
   *
   * <p>Example: for mapping {@code "shop"}, {@code mappings} may associate reference type
   * {@code Payment} with concrete types {@code CreditCard} and {@code Invoice}. If both concrete
   * types must be used through {@code PaymentMethod}, {@code groupingByImplementer} contains
   * {@code CreditCard -> PaymentMethod} and {@code Invoice -> PaymentMethod}.
   *
   * @param mappingName name of the CD stereotype that identifies this mapping, such as {@code shop}
   * @param mappings stable reference-element keys to one or more concrete {@link MappedElement}s;
   *     for example {@code Payment -> [CreditCard, Invoice]}
   * @param groupingByImplementer concrete type keys to the common type through which generated Java
   *     should reference them; for example {@code CreditCard -> PaymentMethod}; may be {@code null}
   *     when no grouping is required
   */
  public IncarnationContext(
      String mappingName,
      Map<StableElementKey, List<MappedElement>> mappings,
      Map<StableElementKey, MappedElement> groupingByImplementer) {
    this.mappingName = Objects.requireNonNull(mappingName);
    this.mappings = immutableMappings(mappings);
    this.groupingByImplementer =
        Collections.unmodifiableMap(
            new LinkedHashMap<>(
                groupingByImplementer == null ? Map.of() : groupingByImplementer));
  }

  /** Returns the mapping stereotype name represented by this context. */
  public String getMappingName() {
    return mappingName;
  }

  /**
   * Returns all immutable stable-key incarnation mappings, for example {@code Payment ->
   * [CreditCard, Invoice]}.
   */
  public Map<StableElementKey, List<MappedElement>> getMappings() {
    return mappings;
  }

  /** Returns the concrete incarnations of one stable reference key. */
  public List<MappedElement> getIncarnations(StableElementKey referenceKey) {
    return referenceKey == null ? List.of() : mappings.getOrDefault(referenceKey, List.of());
  }

  /** Returns the incarnation only when exactly one concrete target exists. */
  public Optional<MappedElement> getUniqueIncarnation(StableElementKey referenceKey) {
    List<MappedElement> incarnations = getIncarnations(referenceKey);
    return incarnations.size() == 1 ? Optional.of(incarnations.get(0)) : Optional.empty();
  }

  /** Returns the grouping type selected for a concrete implementer. */
  public Optional<MappedElement> getGroupingFor(StableElementKey implementer) {
    return Optional.ofNullable(groupingByImplementer.get(implementer));
  }

  /**
   * Returns the complete immutable implementer-to-grouping index, for example {@code CreditCard ->
   * PaymentMethod}.
   */
  public Map<StableElementKey, MappedElement> getGroupingMappings() {
    return groupingByImplementer;
  }

  /** Copies both map and incarnation lists so callers cannot mutate context state after creation. */
  private static Map<StableElementKey, List<MappedElement>> immutableMappings(
      Map<StableElementKey, List<MappedElement>> source) {
    Map<StableElementKey, List<MappedElement>> copy = new LinkedHashMap<>();
    if (source != null) {
      source.forEach((key, value) -> copy.put(key, value == null ? List.of() : List.copyOf(value)));
    }
    return Collections.unmodifiableMap(copy);
  }
}
