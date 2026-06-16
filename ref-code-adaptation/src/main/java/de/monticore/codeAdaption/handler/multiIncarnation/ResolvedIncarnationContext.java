package de.monticore.codeAdaption.handler.multiIncarnation;

import de.monticore.symboltable.ISymbol;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** First-class resolved incarnation model split by element kind. */
public class ResolvedIncarnationContext {
  public static class ResolvedElement {
    private final StableElementKey key;
    private final ISymbol symbol;

    public ResolvedElement(StableElementKey key, ISymbol symbol) {
      this.key = key;
      this.symbol = symbol;
    }

    public StableElementKey getKey() {
      return key;
    }

    public ISymbol getSymbol() {
      return symbol;
    }
  }

  private final String mappingName;
  private final Map<StableElementKey, List<ResolvedElement>> typeMappings = new LinkedHashMap<>();
  private final Map<StableElementKey, List<ResolvedElement>> fieldMappings = new LinkedHashMap<>();
  private final Map<StableElementKey, List<ResolvedElement>> methodMappings = new LinkedHashMap<>();
  private final Map<StableElementKey, StableElementKey> groupingMappings = new LinkedHashMap<>();
  private final Map<StableElementKey, List<StableElementKey>> commonParentMappings = new LinkedHashMap<>();

  public ResolvedIncarnationContext(String mappingName) {
    this.mappingName = mappingName;
  }

  public String getMappingName() {
    return mappingName;
  }

  public void addMapping(StableElementKey reference, StableElementKey concrete, ISymbol concreteSymbol) {
    if (reference == null || concrete == null) {
      return;
    }
    Map<StableElementKey, List<ResolvedElement>> target = targetMap(reference.getKind());
    List<ResolvedElement> values = target.computeIfAbsent(reference, ignored -> new ArrayList<>());
    boolean exists = values.stream().anyMatch(existing -> existing.getKey().equals(concrete));
    if (!exists) {
      values.add(new ResolvedElement(concrete, concreteSymbol));
    }
  }

  public List<ResolvedElement> getIncarnations(StableElementKey reference) {
    if (reference == null) {
      return List.of();
    }
    return Collections.unmodifiableList(
        targetMap(reference.getKind()).getOrDefault(reference, Collections.emptyList()));
  }

  public Optional<ResolvedElement> getUniqueIncarnation(StableElementKey reference) {
    List<ResolvedElement> incarnations = getIncarnations(reference);
    return incarnations.size() == 1 ? Optional.of(incarnations.get(0)) : Optional.empty();
  }

  public Map<StableElementKey, List<ResolvedElement>> getTypeMappings() {
    return immutableMap(typeMappings);
  }

  public Map<StableElementKey, List<ResolvedElement>> getFieldMappings() {
    return immutableMap(fieldMappings);
  }

  public Map<StableElementKey, List<ResolvedElement>> getMethodMappings() {
    return immutableMap(methodMappings);
  }

  public void setGroupingMapping(StableElementKey concrete, StableElementKey grouping) {
    if (concrete != null && grouping != null) {
      groupingMappings.put(concrete, grouping);
    }
  }

  public Optional<StableElementKey> getGroupingForConcrete(StableElementKey concrete) {
    return Optional.ofNullable(groupingMappings.get(concrete));
  }

  public Map<StableElementKey, StableElementKey> getGroupingMappings() {
    return Collections.unmodifiableMap(groupingMappings);
  }

  public void setCommonParentMapping(StableElementKey parent, List<StableElementKey> children) {
    if (parent != null && children != null) {
      commonParentMappings.put(parent, List.copyOf(children));
    }
  }

  public Map<StableElementKey, List<StableElementKey>> getCommonParentMappings() {
    return Collections.unmodifiableMap(commonParentMappings);
  }

  private Map<StableElementKey, List<ResolvedElement>> targetMap(StableElementKey.Kind kind) {
    switch (kind) {
      case TYPE:
        return typeMappings;
      case FIELD:
        return fieldMappings;
      case METHOD:
        return methodMappings;
      default:
        throw new IllegalArgumentException("Unsupported key kind: " + kind);
    }
  }

  private Map<StableElementKey, List<ResolvedElement>> immutableMap(
      Map<StableElementKey, List<ResolvedElement>> source) {
    Map<StableElementKey, List<ResolvedElement>> copy = new LinkedHashMap<>();
    for (Map.Entry<StableElementKey, List<ResolvedElement>> entry : source.entrySet()) {
      copy.put(entry.getKey(), List.copyOf(entry.getValue()));
    }
    return Collections.unmodifiableMap(copy);
  }
}
