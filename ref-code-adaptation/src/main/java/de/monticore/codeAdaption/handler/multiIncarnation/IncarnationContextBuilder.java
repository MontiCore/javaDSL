package de.monticore.codeAdaption.handler.multiIncarnation;

import de.monticore.cdbasis._ast.ASTCDAttribute;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.cd4codebasis._ast.ASTCDMethod;
import de.monticore.cdconformance.CDConformanceChecker;
import de.monticore.codeAdaption.utils.CDModelIndex;
import de.monticore.codeAdaption.utils.CDTypeRelations;
import de.monticore.codeAdaption.utils.JavaSourceNames;
import de.monticore.symboltable.ISymbol;
import java.util.*;
import java.util.function.BiFunction;

import de.se_rwth.commons.logging.Log;

/**
 * Collects incarnation contexts from conformance checker results.
 * Analyzes the incarnation mappings and creates IncarnationContext objects.
 */
public class IncarnationContextBuilder {
  private final CDConformanceChecker conformanceChecker;
  private final ASTCDCompilationUnit referenceCD;
  private final ASTCDCompilationUnit concreteCD;
  private final CDModelIndex referenceIndex;
  private final CDModelIndex concreteIndex;

  public IncarnationContextBuilder(CDConformanceChecker conformanceChecker,
                                   ASTCDCompilationUnit referenceCD,
                                   ASTCDCompilationUnit concreteCD) {
    this.conformanceChecker = conformanceChecker;
    this.referenceCD = referenceCD;
    this.concreteCD = concreteCD;
    this.referenceIndex = CDModelIndex.of(referenceCD);
    this.concreteIndex = CDModelIndex.of(concreteCD);
  }

  /**
   * Builds incarnation context for a specific mapping.
   * First tries conformance checker mapping, then falls back to stereotype-based extraction
   * from the concrete CD (which contains explicit stereotypes - mapping="RefName").
   */
  public IncarnationContext buildContextForMapping(String mapping) {
    return buildContextForMapping(mapping, true);
  }

  public IncarnationContext buildContextForMapping(String mapping, boolean overlayStereotypeMappings) {
    Map<ISymbol, List<ISymbol>> referenceToIncarnations = new HashMap<>();
    Map<ISymbol, List<ISymbol>> interfaceToImplementers = new HashMap<>();

    // Query conformance checker for incarnation mappings
    var incarnationMapping = conformanceChecker.getIncarnationMapping();

    if (incarnationMapping != null) {
      // Extract reference symbols and their corresponding incarnations
      // The incarnation mapping contains the relationships between reference and concrete elements
      referenceToIncarnations = extractIncarnations(incarnationMapping, mapping);
      // Try to extract interface->implementers relationships: prefer AST-based detection, then augment with
      // conformance-based candidates to be resilient across tooling differences
      Map<ISymbol, List<ISymbol>> astImpl = extractInterfaceImplementersFromAST();
      Map<ISymbol, List<ISymbol>> confImpl = extractInterfaceImplementersFromConformance(incarnationMapping);
      // merge: start with AST results (more precise), then add any additional entries from conformance mapping
      interfaceToImplementers.putAll(astImpl);
      for (Map.Entry<ISymbol, List<ISymbol>> e : confImpl.entrySet()) {
        interfaceToImplementers.computeIfAbsent(e.getKey(), k -> new ArrayList<>()).addAll(e.getValue());
      }
    }

    if (overlayStereotypeMappings) {
      // The conformance checker can be partial for pattern adaptations, so stereotypes
      // are the authoritative fallback for gaps in the manual mapping path.
      Log.info(
          "Overlaying stereotype context for mapping '" + mapping + "'",
          "IncarnationContextBuilder");
      mergeMappings(referenceToIncarnations, extractFromStereotypes(mapping));
    }
    if (interfaceToImplementers.isEmpty()) {
      interfaceToImplementers = extractInterfaceImplementersFromAST();
    }

    Map<ISymbol, StableElementKey> symbolKeys = buildSymbolKeyMap(referenceCD, concreteCD);
    ResolvedIncarnationContext resolvedContext =
        buildResolvedContext(mapping, referenceToIncarnations, symbolKeys);

    IncarnationContext ctx =
        new IncarnationContext(
            mapping, referenceToIncarnations, interfaceToImplementers, symbolKeys, resolvedContext);
    // Compute grouping/interface types for concrete implementers (interfaces over classes)
    Map<String, String> concreteToGrouping = computeGroupingMap(referenceToIncarnations);
    ctx.setConcreteToGroupingType(concreteToGrouping);
    return ctx;
  }

  private void mergeMappings(Map<ISymbol, List<ISymbol>> target, Map<ISymbol, List<ISymbol>> fallback) {
    for (Map.Entry<ISymbol, List<ISymbol>> entry : fallback.entrySet()) {
      List<ISymbol> values = target.computeIfAbsent(entry.getKey(), k -> new ArrayList<>());
      for (ISymbol candidate : entry.getValue()) {
        boolean exists =
            values.stream()
                .anyMatch(existing -> existing.getName().equals(candidate.getName()));
        if (!exists) {
          values.add(candidate);
        }
      }
    }
  }

  private ResolvedIncarnationContext buildResolvedContext(
      String mapping,
      Map<ISymbol, List<ISymbol>> referenceToIncarnations,
      Map<ISymbol, StableElementKey> symbolKeys) {
    ResolvedIncarnationContext resolved = new ResolvedIncarnationContext(mapping);
    for (Map.Entry<ISymbol, List<ISymbol>> entry : referenceToIncarnations.entrySet()) {
      StableElementKey referenceKey = symbolKeys.get(entry.getKey());
      if (referenceKey == null || entry.getValue() == null) {
        continue;
      }
      for (ISymbol incarnation : entry.getValue()) {
        StableElementKey concreteKey = symbolKeys.get(incarnation);
        if (concreteKey != null) {
          resolved.addMapping(referenceKey, concreteKey, incarnation);
        }
      }
    }
    return resolved;
  }

  private Map<ISymbol, StableElementKey> buildSymbolKeyMap(
      ASTCDCompilationUnit referenceCD, ASTCDCompilationUnit concreteCD) {
    Map<ISymbol, StableElementKey> result = new IdentityHashMap<>();
    registerSymbolKeys(referenceCD, result);
    registerSymbolKeys(concreteCD, result);
    return result;
  }

  private void registerSymbolKeys(ASTCDCompilationUnit cd, Map<ISymbol, StableElementKey> result) {
    CDModelIndex index = cd == referenceCD ? referenceIndex : concreteIndex;
    for (ASTCDType type : index.types()) {
      result.put(type.getSymbol(), StableElementKey.type(type));
      for (ASTCDAttribute attribute : type.getCDAttributeList()) {
        result.put(attribute.getSymbol(), StableElementKey.field(type, attribute));
      }
      for (ASTCDMethod method : type.getCDMethodList()) {
        result.put(method.getSymbol(), StableElementKey.method(type, method));
      }
    }
  }

  /**
   * Compute a mapping from concrete implementer simple name -> chosen grouping type simple name.
   * Preference: choose an interface candidate when multiple candidates match; otherwise choose the
   * candidate with minimal maximum inheritance distance to the implementers; deterministic tie-breaker: lexical order.
   */
  private Map<String, String> computeGroupingMap(Map<ISymbol, List<ISymbol>> referenceToIncarnations) {
    Map<String, String> result = new HashMap<>();
    // build parent map: typeName -> immediate parent names (interfaces/supertypes)
    Map<String, Set<String>> parentMap = new HashMap<>();
    List<ASTCDType> allTypes = concreteIndex.types();
    Set<String> allTypeNames = new HashSet<>();
    for (ASTCDType t : allTypes) allTypeNames.add(t.getName());

    for (ASTCDType t : allTypes) {
      Set<String> parents = new HashSet<>();
      for (String name : getImplementedInterfaceNames(t)) {
        if (allTypeNames.contains(name)) {
          parents.add(name);
        }
      }
      parentMap.put(t.getName(), parents);
    }

    Set<String> interfaceLike = new HashSet<>();
    for (ASTCDType t : allTypes) {
      interfaceLike.addAll(getImplementedInterfaceNames(t));
    }

    // helper: is candidate reachable from descendant via parent links
    BiFunction<String, String, Boolean> isReachable = (desc, cand) -> {
      if (desc == null || cand == null) return false;
      if (desc.equals(cand)) return true;
      Deque<String> queue = new ArrayDeque<>();
      Set<String> visited = new HashSet<>();
      queue.add(desc);
      visited.add(desc);
      while (!queue.isEmpty()) {
        String cur = queue.removeFirst();
        Set<String> ps = parentMap.getOrDefault(cur, Collections.emptySet());
        for (String p : ps) {
          if (p.equals(cand)) return true;
          if (!visited.contains(p)) { visited.add(p); queue.addLast(p); }
        }
      }
      return false;
    };

    // gather candidate types (all types) by name
    Set<String> candidateNames = new HashSet<>();
    for (ASTCDType t : allTypes) candidateNames.add(t.getName());

    // For each reference -> incarnations entry compute grouping
    for (Map.Entry<ISymbol, List<ISymbol>> e : referenceToIncarnations.entrySet()) {
      List<ISymbol> incs = e.getValue();
      if (incs == null || incs.isEmpty()) continue;
      Set<String> targetSet = new HashSet<>();
      for (ISymbol s : incs) targetSet.add(s.getName());

      List<String> matches = new ArrayList<>();
      for (String cand : candidateNames) {
        // compute descendants of cand among all types and check equality with targetSet
        Set<String> descendants = new HashSet<>();
        for (ASTCDType t : allTypes) {
          String tn = t.getName();
          if (isReachable.apply(tn, cand)) {
            // we only consider concrete classes as incarnations (exclude interface-like types)
            if (!interfaceLike.contains(tn)) descendants.add(tn);
          }
        }
        if (descendants.equals(targetSet)) {
          matches.add(cand);
        } else if (targetSet.contains(cand)) {
          Set<String> targetWithoutCandidate = new HashSet<>(targetSet);
          targetWithoutCandidate.remove(cand);
          if (descendants.equals(targetWithoutCandidate)) {
            matches.add(cand);
          }
        }
      }

      Set<String> interfaceSet = new HashSet<>();
      for (ASTCDType t : allTypes) {
        interfaceSet.addAll(getImplementedInterfaceNames(t));
      }

      List<String> ifaceMatches = new ArrayList<>();
      for (String m2 : matches) if (interfaceSet.contains(m2)) ifaceMatches.add(m2);
      List<String> pool = ifaceMatches.isEmpty() ? matches : ifaceMatches;

      // compute minimal max-distance metric
      String best = null;
      int bestMetric = Integer.MAX_VALUE;
      for (String cand : pool) {
        int maxDist = 0;
        for (String tn : targetSet) {
          // BFS distance from tn up to cand
          Deque<String> q = new ArrayDeque<>(); q.add(tn);
          Map<String, Integer> dist = new HashMap<>(); dist.put(tn, 0);
          int found = Integer.MAX_VALUE;
          while (!q.isEmpty()) {
            String cur = q.removeFirst();
            int d = dist.get(cur);
            if (cur.equals(cand)) { found = d; break; }
            for (String p : parentMap.getOrDefault(cur, Collections.emptySet())) {
              if (!dist.containsKey(p)) { dist.put(p, d+1); q.addLast(p); }
            }
          }
          if (found == Integer.MAX_VALUE) { maxDist = Integer.MAX_VALUE; break; }
          if (found > maxDist) maxDist = found;
        }
        if (maxDist < bestMetric || (maxDist == bestMetric && (best == null || cand.compareTo(best) < 0))) {
          bestMetric = maxDist; best = cand;
        }
      }

      if (best != null) {
        for (String inc : targetSet) result.put(inc, best);
      }
    }

    return result;
  }

  /** Extracts interface-to-implementer relationships from the concrete CD. */
  private Map<ISymbol, List<ISymbol>> extractInterfaceImplementersFromAST() {
    Map<ISymbol, List<ISymbol>> result = new HashMap<>();

    for (ASTCDType concreteType : concreteIndex.types()) {
      for (String implName : getImplementedInterfaceNames(concreteType)) {
        concreteIndex
            .type(implName)
            .ifPresent(
                possibleInterface -> {
                  ISymbol ifaceSym = possibleInterface.getSymbol();
                  result.computeIfAbsent(ifaceSym, k -> new ArrayList<>()).add(concreteType.getSymbol());
                });
      }
    }

    return result;
  }

  /**
   * Extract implementers using conformance incarnation mapping when available.
   * If conformance maps reference interfaces to multiple concrete types, treat those
   * concrete types as implementers for the corresponding concrete interface symbols
   * if possible.
   */
  private Map<ISymbol, List<ISymbol>> extractInterfaceImplementersFromConformance(Object incarnationMapping) {
    Map<ISymbol, List<ISymbol>> result = new HashMap<>();
    try {
      for (ASTCDType refType : referenceIndex.types()) {
        var incarnations = conformanceChecker.getIncarnationMapping().getIncarnations(refType);
        if (incarnations == null) continue;
        for (var inc : incarnations) {
          ISymbol conSym = inc.getSymbol();
          String name = conSym.getName();
          concreteIndex
              .type(name)
              .ifPresent(
                  possibleInterface -> {
                    List<ISymbol> impls =
                        result.computeIfAbsent(possibleInterface.getSymbol(), k -> new ArrayList<>());
                    for (ASTCDType concreteType : concreteIndex.implementersOf(possibleInterface.getName())) {
                      impls.add(concreteType.getSymbol());
                    }
                  });
        }
      }
    } catch (Exception e) {
      // ignore and return whatever we gathered
    }
    return result;
  }

  /**
   * Extracts incarnations from the conformance checker's incarnation mapping.
   */
  private Map<ISymbol, List<ISymbol>> extractIncarnations(Object incarnationMapping, String mapping) {
    Map<ISymbol, List<ISymbol>> result = new HashMap<>();

    // Extract CD types and their incarnations
    for (var cdType : referenceIndex.types()) {
      var incarnations = conformanceChecker.getIncarnationMapping().getIncarnations(cdType);
      List<ISymbol> incarnationSymbols = new ArrayList<>();
      for (var incarnation : incarnations) {
        incarnationSymbols.add(incarnation.getSymbol());
      }
      if (!incarnationSymbols.isEmpty()) {
        result.put(cdType.getSymbol(), incarnationSymbols);
      }
    }

    // Extract CD attributes and their incarnations
    for (var cdType : referenceIndex.types()) {
      for (var cdAttribute : cdType.getCDAttributeList()) {
        var incarnations = conformanceChecker.getIncarnationMapping().getIncarnations(cdAttribute);
        List<ISymbol> incarnationSymbols = new ArrayList<>();
        for (var incarnation : incarnations) {
          incarnationSymbols.add(incarnation.getSymbol());
        }
        if (!incarnationSymbols.isEmpty()) {
          result.put(cdAttribute.getSymbol(), incarnationSymbols);
        }
      }

      for (var cdMethod : cdType.getCDMethodList()) {
        var incarnations = conformanceChecker.getIncarnationMapping().getIncarnations(cdMethod);
        List<ISymbol> incarnationSymbols = new ArrayList<>();
        for (var incarnation : incarnations) {
          incarnationSymbols.add(incarnation.getSymbol());
        }
        if (!incarnationSymbols.isEmpty()) {
          result.put(cdMethod.getSymbol(), incarnationSymbols);
        }
      }
    }

    return result;
  }

  /**
   * Extracts incarnations directly from stereotypes in the concrete CD.
   * Scans all concrete CD elements for stereotypes like <<mapping="RefElementName">>
   * and builds the mapping from reference element to concrete element.
   */
  private Map<ISymbol, List<ISymbol>> extractFromStereotypes(String mapping) {
    Map<ISymbol, List<ISymbol>> result = new HashMap<>();
    Map<String, ISymbol> refElementsByName = buildRefElementMap();

    // Extract type mappings from stereotypes
    for (ASTCDType concreteType : concreteIndex.types()) {
      Optional<String> refTypeName = getStereotypeValue(concreteType, mapping);
      if (refTypeName.isPresent()) {
        ISymbol refSymbol = refElementsByName.get(refTypeName.get());
        if (refSymbol != null) {
          result.computeIfAbsent(refSymbol, k -> new ArrayList<>()).add(concreteType.getSymbol());
        }
      }

      // Extract attribute mappings
      for (ASTCDAttribute concreteAttr : concreteType.getCDAttributeList()) {
        Optional<String> refAttrName = getStereotypeValue(concreteAttr, mapping);
        if (refAttrName.isPresent()) {
          // Look for matching attribute symbol in reference CD
          String refOwner = refTypeName.orElse("");
          String refKey = refOwner + "." + refAttrName.get();
          ISymbol refSymbol = refElementsByName.get(refKey);
          if (refSymbol == null) {
            refSymbol = findUniqueMemberBySimpleName(refElementsByName, refAttrName.get(), StableElementKey.Kind.FIELD);
          }
          if (refSymbol != null) {
            result.computeIfAbsent(refSymbol, k -> new ArrayList<>()).add(concreteAttr.getSymbol());
          }
        }
      }

      // Extract method mappings
      for (ASTCDMethod concreteMethod : concreteType.getCDMethodList()) {
        Optional<String> refMethodName = getStereotypeValue(concreteMethod, mapping);
        if (refMethodName.isPresent()) {
          // Look for matching method symbol in reference CD
          String methodOwner =
              refTypeName
                  .filter(owner -> refElementsByName.containsKey(owner))
                  .orElseGet(
                      () ->
                          findMappedInterfaceReference(concreteType, mapping, refElementsByName)
                              .orElse(refTypeName.orElse("")));
          ISymbol refSymbol = findMethodSymbol(refElementsByName, methodOwner, refMethodName.get());
          if (refSymbol != null) {
            result.computeIfAbsent(refSymbol, k -> new ArrayList<>()).add(concreteMethod.getSymbol());
          }
        }
      }
    }

    return result;
  }

  /**
   * Builds a map of reference CD elements by name for quick lookup.
   * Keys are "TypeName" for types, "TypeName.fieldName" for attributes,
   * and "TypeName.methodName(paramType1,paramType2)" for methods (signature-aware).
   */
  private Map<String, ISymbol> buildRefElementMap() {
    Map<String, ISymbol> map = new HashMap<>();

    for (ASTCDType refType : referenceIndex.types()) {
      // Add type
      map.put(refType.getName(), refType.getSymbol());

      // Add attributes
      for (ASTCDAttribute attr : refType.getCDAttributeList()) {
        map.put(refType.getName() + "." + attr.getName(), attr.getSymbol());
        map.put(
            StableElementKey.Kind.FIELD + ":" + refType.getName() + "." + attr.getName(),
            attr.getSymbol());
      }

      // Add methods with signature-aware keys to support overloading
      for (ASTCDMethod method : refType.getCDMethodList()) {
        String sigKey = buildMethodSignatureKey(method);
        map.put(refType.getName() + "." + sigKey, method.getSymbol());
        map.put(
            StableElementKey.Kind.METHOD + ":" + refType.getName() + "." + sigKey,
            method.getSymbol());
      }
    }

    Map<String, List<ISymbol>> simpleMethodsByOwner = new HashMap<>();
    for (ASTCDType refType : referenceIndex.types()) {
      for (ASTCDMethod method : refType.getCDMethodList()) {
        simpleMethodsByOwner
            .computeIfAbsent(refType.getName() + "." + method.getName(), ignored -> new ArrayList<>())
            .add(method.getSymbol());
      }
    }
    for (Map.Entry<String, List<ISymbol>> entry : simpleMethodsByOwner.entrySet()) {
      if (entry.getValue().size() == 1) {
        map.put(entry.getKey(), entry.getValue().get(0));
      }
    }

    return map;
  }

  /**
   * Builds a method signature key string including parameter types.
   * Format: methodName(paramType1,paramType2,...)
   * Example: "update(String)" or "process(double)"
   */
  private String buildMethodSignatureKey(ASTCDMethod method) {
    return JavaSourceNames.methodSignature(method);
  }

  /**
   * Extracts a simple type name from a full type string.
   * Handles generic types, qualified names, and primitive types.
   */
  private String getSimpleTypeName(String typeStr) {
    return JavaSourceNames.simpleTypeName(typeStr);
  }

  /**
   * Finds a method symbol in the reference element map.
   * First tries signature-aware lookup, then falls back to simple name
   * only if there's no ambiguity (exactly one method with that name).
   */
  private ISymbol findMethodSymbol(Map<String, ISymbol> refElementsByName, String typeName, String methodRef) {
    // First try exact lookup with the stereotype value (may include signature like "update()")
    String fullKey = typeName + "." + methodRef;
    ISymbol refSymbol = refElementsByName.get(fullKey);
    if (refSymbol != null) {
      return refSymbol;
    }

    // If not found and the ref doesn't have parentheses, try simple name fallback
    // But only if the stereotype value is a simple name (no signature)
    if (!methodRef.contains("(")) {
      // Simple name lookup - find any method with this name
      String simpleKey = typeName + "." + methodRef;
      ISymbol symbol = refElementsByName.get(simpleKey);
      if (symbol != null) {
        return symbol;
      }
      return findUniqueMemberBySimpleName(refElementsByName, methodRef, StableElementKey.Kind.METHOD);
    }

    return null;
  }

  private Optional<String> findMappedInterfaceReference(
      ASTCDType concreteType, String mapping, Map<String, ISymbol> refElementsByName) {
    for (String interfaceName : getImplementedInterfaceNames(concreteType)) {
      Optional<ASTCDType> possibleInterface = concreteIndex.type(interfaceName);
      if (possibleInterface.isEmpty()) {
        continue;
      }
      Optional<String> mappedReference = getStereotypeValue(possibleInterface.get(), mapping);
      if (mappedReference.isPresent() && refElementsByName.containsKey(mappedReference.get())) {
        return mappedReference;
      }
    }
    return Optional.empty();
  }

  private List<String> getImplementedInterfaceNames(ASTCDType type) {
    List<String> names = new ArrayList<>();
    for (String interfaceName : CDTypeRelations.interfaceNames(type)) {
      String simpleName = normalizeTypeName(interfaceName);
      if (!simpleName.isEmpty()) {
        names.add(simpleName);
      }
    }
    return names;
  }

  private String normalizeTypeName(String typeName) {
    return JavaSourceNames.simpleTypeName(typeName);
  }

  private ISymbol findUniqueMemberBySimpleName(
      Map<String, ISymbol> refElementsByName, String simpleName, StableElementKey.Kind kind) {
    ISymbol found = null;
    String prefix = kind + ":";
    for (Map.Entry<String, ISymbol> entry : refElementsByName.entrySet()) {
      if (!entry.getKey().startsWith(prefix)) {
        continue;
      }
      String key = entry.getKey().substring(prefix.length());
      int ownerSeparator = key.indexOf('.');
      if (ownerSeparator < 0) {
        continue;
      }
      String member = key.substring(ownerSeparator + 1);
      boolean matches =
          kind == StableElementKey.Kind.METHOD
              ? member.equals(simpleName) || member.startsWith(simpleName + "(")
              : member.equals(simpleName);
      if (!matches) {
        continue;
      }
      if (found != null && found != entry.getValue()) {
        throw new IllegalStateException(
            "Ambiguous stereotype mapping for " + kind + " '" + simpleName + "'");
      }
      found = entry.getValue();
    }
    return found;
  }

  /**
   * Gets the stereotype value from a CD type using MontiCore API.
   * Uses pattern: node.getModifier().isPresentStereotype() / getStereotype().getValuesList()
   * Wraps getValue() in try-catch to suppress MontiCore internal error when value is empty.
   */
  private Optional<String> getStereotypeValue(ASTCDType cdType, String mapping) {
    if (cdType.getModifier() == null || !cdType.getModifier().isPresentStereotype()) {
      return Optional.empty();
    }
    for (var s : cdType.getModifier().getStereotype().getValuesList()) {
      if (mapping.equals(s.getName())) {
        try {
          return Optional.ofNullable(s.getValue());
        } catch (Exception e) {
          // MontiCore logs error when value is empty, skip this stereotype
          return Optional.empty();
        }
      }
    }
    return Optional.empty();
  }

  /**
   * Gets the stereotype value from a CD attribute.
   */
  private Optional<String> getStereotypeValue(ASTCDAttribute cdAttr, String mapping) {
    if (cdAttr.getModifier() == null || !cdAttr.getModifier().isPresentStereotype()) {
      return Optional.empty();
    }
    for (var s : cdAttr.getModifier().getStereotype().getValuesList()) {
      if (mapping.equals(s.getName())) {
        try {
          return Optional.ofNullable(s.getValue());
        } catch (Exception e) {
          return Optional.empty();
        }
      }
    }
    return Optional.empty();
  }

  /**
   * Gets the stereotype value from a CD method.
   */
  private Optional<String> getStereotypeValue(ASTCDMethod cdMethod, String mapping) {
    if (cdMethod.getModifier() == null || !cdMethod.getModifier().isPresentStereotype()) {
      return Optional.empty();
    }
    for (var s : cdMethod.getModifier().getStereotype().getValuesList()) {
      if (mapping.equals(s.getName())) {
        try {
          return Optional.ofNullable(s.getValue());
        } catch (Exception e) {
          return Optional.empty();
        }
      }
    }
    return Optional.empty();
  }

  /**
   * Builds all incarnation contexts for multiple mappings.
   */
  public Map<String, IncarnationContext> buildAllContexts(Set<String> mappings) {
    Map<String, IncarnationContext> contexts = new HashMap<>();

    for (String mapping : mappings) {
      contexts.put(mapping, buildContextForMapping(mapping));
    }

    return contexts;
  }
}
