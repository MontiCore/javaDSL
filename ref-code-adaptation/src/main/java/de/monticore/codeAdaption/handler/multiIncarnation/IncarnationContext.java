package de.monticore.codeAdaption.handler.multiIncarnation;

import de.monticore.cd4codebasis._ast.ASTCDMethod;
import de.monticore.cdbasis._ast.ASTCDAttribute;
import de.monticore.symboltable.ISymbol;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.codeAdaption.utils.JavaSourceNames;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Context information for handling multiple incarnations of the same pattern.
 * Tracks which incarnations are available for each reference element and which mapping they belong to.
 */
public class IncarnationContext {
  private final String mappingName;
  private final Map<ISymbol, List<ISymbol>> referenceToIncarnations;
  private final Map<ISymbol, List<ISymbol>> interfaceToImplementers;
  private final Map<ISymbol, StableElementKey> symbolKeys;
  private final ResolvedIncarnationContext resolvedContext;
  // concrete simple-name to grouping simple-name
  private Map<String, String> concreteToGroupingType = new HashMap<>();

  public IncarnationContext(String mappingName, Map<ISymbol, List<ISymbol>> referenceToIncarnations) {
    this(mappingName, referenceToIncarnations, Map.of());
  }

  public IncarnationContext(String mappingName, Map<ISymbol, List<ISymbol>> referenceToIncarnations,
      Map<ISymbol, List<ISymbol>> interfaceToImplementers) {
    this(mappingName, referenceToIncarnations, interfaceToImplementers, Map.of(), null);
  }

  public IncarnationContext(
      String mappingName,
      Map<ISymbol, List<ISymbol>> referenceToIncarnations,
      Map<ISymbol, List<ISymbol>> interfaceToImplementers,
      Map<ISymbol, StableElementKey> symbolKeys,
      ResolvedIncarnationContext resolvedContext) {
    this.mappingName = mappingName;
    this.referenceToIncarnations = referenceToIncarnations;
    this.interfaceToImplementers = interfaceToImplementers != null ? interfaceToImplementers : Map.of();
    this.symbolKeys = symbolKeys != null ? new LinkedHashMap<>(symbolKeys) : Map.of();
    this.resolvedContext =
        resolvedContext != null ? resolvedContext : new ResolvedIncarnationContext(mappingName);
  }

  public String getMappingName() {
    return mappingName;
  }

  public Map<ISymbol, List<ISymbol>> getReferenceToIncarnations() {
    return referenceToIncarnations;
  }

  public List<ISymbol> getIncarnations(ISymbol referenceSymbol) {
    if (referenceSymbol == null) {
      return null;
    }
    Optional<StableElementKey> key = getStableKey(referenceSymbol);
    if (key.isPresent()) {
      List<ISymbol> resolved =
          resolvedContext.getIncarnations(key.get()).stream()
              .map(ResolvedIncarnationContext.ResolvedElement::getSymbol)
              .filter(java.util.Objects::nonNull)
              .toList();
      if (!resolved.isEmpty()) {
        return resolved;
      }
    }
    List<ISymbol> incarnations = referenceToIncarnations.get(referenceSymbol);
    if (incarnations != null) {
      return incarnations;
    }
    return null;
  }

  public List<ResolvedIncarnationContext.ResolvedElement> getIncarnations(StableElementKey referenceKey) {
    return resolvedContext.getIncarnations(referenceKey);
  }

  public Optional<StableElementKey> getStableKey(ISymbol symbol) {
    StableElementKey registered = symbolKeys.get(symbol);
    if (registered != null) {
      return Optional.of(registered);
    }
    // Matchers may hold symbols from a separately initialized AST. Type identity is still stable
    // across those loads and must not fall back to reference names during multi-incarnation runs.
    if (symbol != null && symbol.getAstNode() instanceof ASTCDType type) {
      return Optional.of(StableElementKey.type(type));
    }
    if (symbol != null && symbol.getAstNode() instanceof ASTCDAttribute attribute) {
      List<StableElementKey> matches =
          resolvedContext.getFieldMappings().keySet().stream()
              .filter(key -> key.getName().equals(attribute.getName()))
              .filter(
                  key ->
                      key.getFieldKind()
                          .map(JavaSourceNames.printNormalizedFieldType(attribute)::equals)
                          .orElse(false))
              .toList();
      return matches.size() == 1 ? Optional.of(matches.get(0)) : Optional.empty();
    }
    if (symbol != null && symbol.getAstNode() instanceof ASTCDMethod method) {
      List<String> parameterTypes =
          method.getCDParameterList().stream()
              .map(parameter -> JavaSourceNames.printNormalizedType(parameter.getMCType()))
              .toList();
      String returnType = JavaSourceNames.printNormalizedReturnType(method);
      List<StableElementKey> matches =
          resolvedContext.getMethodMappings().keySet().stream()
              .filter(key -> key.getName().equals(method.getName()))
              .filter(key -> key.getParameterTypes().equals(parameterTypes))
              .filter(key -> key.getReturnType().map(returnType::equals).orElse(true))
              .toList();
      return matches.size() == 1 ? Optional.of(matches.get(0)) : Optional.empty();
    }
    return Optional.empty();
  }

  public ResolvedIncarnationContext getResolvedContext() {
    return resolvedContext;
  }

  public boolean hasMultipleIncarnations(ISymbol referenceSymbol) {
    List<ISymbol> incarnations = getIncarnations(referenceSymbol);
    return incarnations != null && incarnations.size() > 1;
  }

  /**
   * Set the computed concrete->grouping mapping (simple names).
   */
  public void setConcreteToGroupingType(Map<String, String> map) {
    if (map == null) this.concreteToGroupingType = new HashMap<>();
    else this.concreteToGroupingType = new HashMap<>(map);
    for (Map.Entry<String, String> entry : this.concreteToGroupingType.entrySet()) {
      resolvedContext.setGroupingMapping(
          StableElementKey.type(entry.getKey()), StableElementKey.type(entry.getValue()));
    }
  }

  /**
   * Find a grouping type simple name for the given concrete implementer simple name.
   * Returns Optional.empty() if no grouping was computed.
   */
  public Optional<String> findGroupingTypeForImplementer(String concreteSimpleName) {
    if (concreteSimpleName == null) return Optional.empty();
    String v = concreteToGroupingType.get(concreteSimpleName);
    return v == null ? Optional.empty() : Optional.of(v);
  }

  public Map<ISymbol, List<ISymbol>> getInterfaceToImplementers() {
    return interfaceToImplementers;
  }

  /**
   * Look up the concrete type name for a reference type name.
   * Returns the first incarnation's name if found.
   */
  public Optional<String> getConcreteTypeName(String refTypeName) {
    List<ResolvedIncarnationContext.ResolvedElement> typeIncarnations =
        resolvedContext.getIncarnations(StableElementKey.type(refTypeName));
    if (!typeIncarnations.isEmpty()) {
      return Optional.of(typeIncarnations.get(0).getKey().getName());
    }
    return Optional.empty();
  }

  public Map<StableElementKey, List<StableElementKey>> getStableMappings() {
    Map<StableElementKey, List<StableElementKey>> result = new LinkedHashMap<>();
    for (Map.Entry<StableElementKey, List<ResolvedIncarnationContext.ResolvedElement>> entry :
        resolvedContext.getTypeMappings().entrySet()) {
      result.put(entry.getKey(), toKeys(entry.getValue()));
    }
    for (Map.Entry<StableElementKey, List<ResolvedIncarnationContext.ResolvedElement>> entry :
        resolvedContext.getFieldMappings().entrySet()) {
      result.put(entry.getKey(), toKeys(entry.getValue()));
    }
    for (Map.Entry<StableElementKey, List<ResolvedIncarnationContext.ResolvedElement>> entry :
        resolvedContext.getMethodMappings().entrySet()) {
      result.put(entry.getKey(), toKeys(entry.getValue()));
    }
    return result;
  }

  private List<StableElementKey> toKeys(List<ResolvedIncarnationContext.ResolvedElement> elements) {
    List<StableElementKey> keys = new ArrayList<>();
    for (ResolvedIncarnationContext.ResolvedElement element : elements) {
      keys.add(element.getKey());
    }
    return keys;
  }
}
