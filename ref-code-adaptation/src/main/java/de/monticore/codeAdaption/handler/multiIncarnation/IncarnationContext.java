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
  public record MappedElement(StableElementKey key, ISymbol symbol) {
    public MappedElement {
      Objects.requireNonNull(key);
      Objects.requireNonNull(symbol);
    }
  }

  private final String mappingName;
  private final Map<StableElementKey, List<MappedElement>> mappings;
  private final Map<StableElementKey, MappedElement> groupingByImplementer;

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

  public String getMappingName() {
    return mappingName;
  }

  public Map<StableElementKey, List<MappedElement>> getMappings() {
    return mappings;
  }

  public List<MappedElement> getIncarnations(StableElementKey referenceKey) {
    return referenceKey == null ? List.of() : mappings.getOrDefault(referenceKey, List.of());
  }

  public Optional<MappedElement> getUniqueIncarnation(StableElementKey referenceKey) {
    List<MappedElement> incarnations = getIncarnations(referenceKey);
    return incarnations.size() == 1 ? Optional.of(incarnations.get(0)) : Optional.empty();
  }

  public Optional<MappedElement> getGroupingFor(StableElementKey implementer) {
    return Optional.ofNullable(groupingByImplementer.get(implementer));
  }

  public Map<StableElementKey, MappedElement> getGroupingMappings() {
    return groupingByImplementer;
  }

  private static Map<StableElementKey, List<MappedElement>> immutableMappings(
      Map<StableElementKey, List<MappedElement>> source) {
    Map<StableElementKey, List<MappedElement>> copy = new LinkedHashMap<>();
    if (source != null) {
      source.forEach((key, value) -> copy.put(key, value == null ? List.of() : List.copyOf(value)));
    }
    return Collections.unmodifiableMap(copy);
  }
}
