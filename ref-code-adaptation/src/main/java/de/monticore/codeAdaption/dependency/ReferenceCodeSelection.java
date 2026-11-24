package de.monticore.codeAdaption.dependency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Immutable, parser-independent description of the reference Java retained for one mapping.
 *
 * <p>All Java type identities are package-qualified top-level names. Nested, local, and anonymous
 * declarations are represented by their owning top-level identity because they cannot be retained
 * independently of that source unit.
 */
public final class ReferenceCodeSelection {
  private final Set<String> rootTypeIdentities;
  private final Set<String> helperTypeIdentities;
  private final Map<String, Set<String>> dependencyEdges;
  private final Map<String, Set<String>> referencedCDTypeKeys;
  private final List<String> diagnostics;

  public ReferenceCodeSelection(
      Set<String> rootTypeIdentities,
      Set<String> helperTypeIdentities,
      Map<String, Set<String>> dependencyEdges,
      Map<String, Set<String>> referencedCDTypeKeys,
      List<String> diagnostics) {
    this.rootTypeIdentities = immutableSortedSet(rootTypeIdentities);
    this.helperTypeIdentities = immutableSortedSet(helperTypeIdentities);
    this.dependencyEdges = immutableSortedMap(dependencyEdges);
    this.referencedCDTypeKeys = immutableSortedMap(referencedCDTypeKeys);
    List<String> sortedDiagnostics = new ArrayList<>(diagnostics);
    Collections.sort(sortedDiagnostics);
    this.diagnostics = List.copyOf(sortedDiagnostics);
  }

  /** Package-qualified mapped top-level Java types from which selection started. */
  public Set<String> rootTypeIdentities() {
    return rootTypeIdentities;
  }

  /** Package-qualified Java-only types reached transitively from the mapped roots. */
  public Set<String> helperTypeIdentities() {
    return helperTypeIdentities;
  }

  /** All retained top-level types, including roots and helpers. */
  public Set<String> selectedTypeIdentities() {
    Set<String> selected = new LinkedHashSet<>(rootTypeIdentities);
    selected.addAll(helperTypeIdentities);
    return Collections.unmodifiableSet(selected);
  }

  /** Directed source-local dependency edges, restricted to retained source types. */
  public Map<String, Set<String>> dependencyEdges() {
    return dependencyEdges;
  }

  /** Reference-CD type keys used by each retained source type. */
  public Map<String, Set<String>> referencedCDTypeKeysBySourceType() {
    return referencedCDTypeKeys;
  }

  /** Union of reference-CD type keys used anywhere in the retained closure. */
  public Set<String> referencedCDTypeKeys() {
    Set<String> result = new TreeSet<>();
    referencedCDTypeKeys.values().forEach(result::addAll);
    return Collections.unmodifiableSet(result);
  }

  /** Deterministically ordered non-fatal diagnostics produced while selecting the closure. */
  public List<String> diagnostics() {
    return diagnostics;
  }

  public boolean contains(String qualifiedTopLevelType) {
    return rootTypeIdentities.contains(qualifiedTopLevelType)
        || helperTypeIdentities.contains(qualifiedTopLevelType);
  }

  private static Set<String> immutableSortedSet(Set<String> values) {
    return Collections.unmodifiableSet(new LinkedHashSet<>(new TreeSet<>(values)));
  }

  private static Map<String, Set<String>> immutableSortedMap(Map<String, Set<String>> values) {
    Map<String, Set<String>> result = new LinkedHashMap<>();
    for (Map.Entry<String, Set<String>> entry : new TreeMap<>(values).entrySet()) {
      result.put(entry.getKey(), immutableSortedSet(entry.getValue()));
    }
    return Collections.unmodifiableMap(result);
  }
}
