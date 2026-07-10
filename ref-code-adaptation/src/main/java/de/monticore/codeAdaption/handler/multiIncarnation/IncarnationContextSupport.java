package de.monticore.codeAdaption.handler.multiIncarnation;

import de.monticore.cd4codebasis._ast.ASTCDMethod;
import de.monticore.cdbasis._ast.ASTCDAttribute;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.codeAdaption.utils.CDModelIndex;
import de.monticore.codeAdaption.utils.JavaSourceNames;
import de.monticore.codeAdaption.utils.JavaMethodSignatures;
import de.monticore.symboltable.ISymbol;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Shared, identity-aware infrastructure for the two incarnation-context construction modes. */
final class IncarnationContextSupport {
  private final CDModelIndex referenceIndex;
  private final CDModelIndex concreteIndex;
  private final Map<ISymbol, StableElementKey> symbolKeys = new IdentityHashMap<>();
  private final ReferenceElementIndex referenceElements;

  IncarnationContextSupport(
      ASTCDCompilationUnit referenceCD, ASTCDCompilationUnit concreteCD) {
    this.referenceIndex = CDModelIndex.of(referenceCD);
    this.concreteIndex = CDModelIndex.of(concreteCD);
    registerSymbolKeys(referenceIndex);
    registerSymbolKeys(concreteIndex);
    this.referenceElements = new ReferenceElementIndex(referenceIndex);
  }

  CDModelIndex referenceIndex() {
    return referenceIndex;
  }

  CDModelIndex concreteIndex() {
    return concreteIndex;
  }

  Map<ISymbol, List<ISymbol>> newMapping() {
    return new LinkedHashMap<>();
  }

  void addMapping(
      Map<ISymbol, List<ISymbol>> target, ISymbol reference, ISymbol concrete) {
    if (reference == null || concrete == null) {
      return;
    }
    List<ISymbol> incarnations = target.computeIfAbsent(reference, ignored -> new ArrayList<>());
    StableElementKey candidateKey = symbolKeys.get(concrete);
    boolean duplicate =
        incarnations.stream()
            .anyMatch(existing -> sameElement(existing, concrete, candidateKey));
    if (!duplicate) {
      incarnations.add(concrete);
    }
  }

  void mergeMappings(
      Map<ISymbol, List<ISymbol>> target, Map<ISymbol, List<ISymbol>> additions) {
    for (Map.Entry<ISymbol, List<ISymbol>> entry : additions.entrySet()) {
      if (entry.getValue() == null) {
        continue;
      }
      for (ISymbol incarnation : entry.getValue()) {
        addMapping(target, entry.getKey(), incarnation);
      }
    }
  }

  IncarnationContext assembleContext(
      String mapping, Map<ISymbol, List<ISymbol>> referenceToIncarnations) {
    Map<ISymbol, List<ISymbol>> interfaceToImplementers = interfaceImplementers();
    ResolvedIncarnationContext resolved = new ResolvedIncarnationContext(mapping);
    for (Map.Entry<ISymbol, List<ISymbol>> entry : referenceToIncarnations.entrySet()) {
      StableElementKey referenceKey = symbolKeys.get(entry.getKey());
      if (referenceKey == null || entry.getValue() == null) {
        continue;
      }
      for (ISymbol incarnation : entry.getValue()) {
        StableElementKey incarnationKey = symbolKeys.get(incarnation);
        if (incarnationKey != null) {
          resolved.addMapping(referenceKey, incarnationKey, incarnation);
        }
      }
    }
    IncarnationContext context =
        new IncarnationContext(
            mapping,
            referenceToIncarnations,
            interfaceToImplementers,
            new IdentityHashMap<>(symbolKeys),
            resolved);
    context.setConcreteToGroupingType(groupingTypes(referenceToIncarnations));
    return context;
  }

  Optional<String> stereotypeValue(ASTCDType type, String name) {
    if (type == null || type.getModifier() == null || !type.getModifier().isPresentStereotype()) {
      return Optional.empty();
    }
    for (var value : type.getModifier().getStereotype().getValuesList()) {
      if (name.equals(value.getName())) {
        try {
          return Optional.ofNullable(value.getValue());
        } catch (RuntimeException ignored) {
          return Optional.empty();
        }
      }
    }
    return Optional.empty();
  }

  Optional<String> stereotypeValue(ASTCDAttribute attribute, String name) {
    if (attribute == null
        || attribute.getModifier() == null
        || !attribute.getModifier().isPresentStereotype()) {
      return Optional.empty();
    }
    for (var value : attribute.getModifier().getStereotype().getValuesList()) {
      if (name.equals(value.getName())) {
        try {
          return Optional.ofNullable(value.getValue());
        } catch (RuntimeException ignored) {
          return Optional.empty();
        }
      }
    }
    return Optional.empty();
  }

  Optional<String> stereotypeValue(ASTCDMethod method, String name) {
    if (method == null
        || method.getModifier() == null
        || !method.getModifier().isPresentStereotype()) {
      return Optional.empty();
    }
    for (var value : method.getModifier().getStereotype().getValuesList()) {
      if (name.equals(value.getName())) {
        try {
          return Optional.ofNullable(value.getValue());
        } catch (RuntimeException ignored) {
          return Optional.empty();
        }
      }
    }
    return Optional.empty();
  }

  Optional<ISymbol> findReferenceType(String name) {
    return referenceElements.findType(name);
  }

  Optional<ISymbol> findReferenceField(List<ASTCDType> owners, String name) {
    return referenceElements.findField(owners, name);
  }

  Optional<ISymbol> findReferenceMethod(List<ASTCDType> owners, String name) {
    return referenceElements.findMethod(owners, name);
  }

  Optional<ISymbol> findSameNameField(
      List<ASTCDType> owners, ASTCDAttribute concreteAttribute) {
    List<ISymbol> matches = new ArrayList<>();
    for (ASTCDType owner : owners) {
      for (ASTCDAttribute candidate : owner.getCDAttributeList()) {
        if (candidate.getName().equals(concreteAttribute.getName())) {
          matches.add(candidate.getSymbol());
        }
      }
    }
    return unique(matches);
  }

  Optional<ISymbol> findSameSignatureMethod(
      List<ASTCDType> owners, ASTCDMethod concreteMethod) {
    String signature = JavaSourceNames.methodSignature(concreteMethod);
    List<ISymbol> matches = new ArrayList<>();
    for (ASTCDType owner : owners) {
      for (ASTCDMethod candidate : owner.getCDMethodList()) {
        if (JavaSourceNames.methodSignature(candidate).equals(signature)) {
          matches.add(candidate.getSymbol());
        }
      }
    }
    return unique(matches);
  }

  List<ASTCDType> mappedReferenceOwners(
      Map<ISymbol, List<ISymbol>> mappings, ASTCDType concreteType) {
    StableElementKey concreteKey = symbolKeys.get(concreteType.getSymbol());
    List<ASTCDType> owners = new ArrayList<>();
    for (Map.Entry<ISymbol, List<ISymbol>> entry : mappings.entrySet()) {
      if (!(entry.getKey().getAstNode() instanceof ASTCDType referenceType)) {
        continue;
      }
      if (entry.getValue().stream()
          .anyMatch(symbol -> sameElement(symbol, concreteType.getSymbol(), concreteKey))) {
        owners.add(referenceType);
      }
    }
    return owners;
  }

  Optional<String> mappedInterfaceReference(ASTCDType concreteType, String mapping) {
    for (String interfaceName : concreteIndex.directParentNames(concreteType.getName())) {
      Optional<ASTCDType> possibleInterface = concreteIndex.type(interfaceName);
      if (possibleInterface.isEmpty() || !isInterface(possibleInterface.get().getName())) {
        continue;
      }
      Optional<String> reference = stereotypeValue(possibleInterface.get(), mapping);
      if (reference.isPresent() && findReferenceType(reference.get()).isPresent()) {
        return reference;
      }
    }
    return Optional.empty();
  }

  private void registerSymbolKeys(CDModelIndex index) {
    for (ASTCDType type : index.types()) {
      symbolKeys.put(type.getSymbol(), StableElementKey.type(type));
      for (ASTCDAttribute attribute : type.getCDAttributeList()) {
        symbolKeys.put(attribute.getSymbol(), StableElementKey.field(type, attribute));
      }
      for (ASTCDMethod method : type.getCDMethodList()) {
        symbolKeys.put(method.getSymbol(), StableElementKey.method(type, method));
      }
    }
  }

  private boolean sameElement(
      ISymbol existing, ISymbol candidate, StableElementKey candidateKey) {
    if (existing == candidate) {
      return true;
    }
    StableElementKey existingKey = symbolKeys.get(existing);
    return existingKey != null
        && candidateKey != null
        && (existingKey.equals(candidateKey)
            || (existingKey.getKind() == StableElementKey.Kind.METHOD
                && existingKey.sameSignatureIgnoringReturn(candidateKey)));
  }

  private Optional<ISymbol> unique(List<ISymbol> candidates) {
    if (candidates.isEmpty()) {
      return Optional.empty();
    }
    ISymbol first = candidates.get(0);
    StableElementKey firstKey = symbolKeys.get(first);
    for (int i = 1; i < candidates.size(); i++) {
      if (!sameElement(candidates.get(i), first, firstKey)) {
        return Optional.empty();
      }
    }
    return Optional.of(first);
  }

  private Map<ISymbol, List<ISymbol>> interfaceImplementers() {
    Map<ISymbol, List<ISymbol>> result = new LinkedHashMap<>();
    for (ASTCDType iface : concreteIndex.interfaces()) {
      for (ASTCDType type : concreteIndex.classes()) {
        if (concreteIndex.isSubtypeOf(type.getName(), iface.getName())) {
          addMapping(result, iface.getSymbol(), type.getSymbol());
        }
      }
    }
    return result;
  }

  private Map<String, String> groupingTypes(
      Map<ISymbol, List<ISymbol>> referenceToIncarnations) {
    Map<String, String> result = new LinkedHashMap<>();
    for (List<ISymbol> incarnations : referenceToIncarnations.values()) {
      Set<String> targets = typeNames(incarnations);
      if (targets.size() < 2) {
        continue;
      }
      String best = null;
      int bestDistance = Integer.MAX_VALUE;
      for (ASTCDType iface : concreteIndex.interfaces()) {
        Set<String> expectedImplementers = new LinkedHashSet<>(targets);
        expectedImplementers.remove(iface.getName());
        Set<String> actualImplementers = new LinkedHashSet<>();
        for (ASTCDType type : concreteIndex.classes()) {
          if (concreteIndex.isSubtypeOf(type.getName(), iface.getName())) {
            actualImplementers.add(type.getName());
          }
        }
        if (!actualImplementers.equals(expectedImplementers)) {
          continue;
        }
        int distance = maximumDistance(targets, iface.getName());
        if (distance < bestDistance
            || (distance == bestDistance && (best == null || iface.getName().compareTo(best) < 0))) {
          best = iface.getName();
          bestDistance = distance;
        }
      }
      if (best != null) {
        for (String target : targets) {
          result.put(target, best);
        }
      }
    }
    return result;
  }

  private Set<String> typeNames(List<ISymbol> symbols) {
    Set<String> result = new LinkedHashSet<>();
    if (symbols == null) {
      return result;
    }
    for (ISymbol symbol : symbols) {
      StableElementKey key = symbolKeys.get(symbol);
      if (key != null && key.getKind() == StableElementKey.Kind.TYPE) {
        result.add(key.getName());
      }
    }
    return result;
  }

  private int maximumDistance(Set<String> descendants, String ancestor) {
    int maximum = 0;
    for (String descendant : descendants) {
      int distance = inheritanceDistance(descendant, ancestor);
      if (distance == Integer.MAX_VALUE) {
        return distance;
      }
      maximum = Math.max(maximum, distance);
    }
    return maximum;
  }

  private int inheritanceDistance(String descendant, String ancestor) {
    if (descendant.equals(ancestor)) {
      return 0;
    }
    ArrayDeque<String> queue = new ArrayDeque<>();
    Map<String, Integer> distances = new LinkedHashMap<>();
    queue.add(descendant);
    distances.put(descendant, 0);
    while (!queue.isEmpty()) {
      String current = queue.removeFirst();
      int nextDistance = distances.get(current) + 1;
      for (String parent : concreteIndex.directParentNames(current)) {
        if (parent.equals(ancestor)) {
          return nextDistance;
        }
        if (!distances.containsKey(parent)) {
          distances.put(parent, nextDistance);
          queue.addLast(parent);
        }
      }
    }
    return Integer.MAX_VALUE;
  }

  private boolean isInterface(String name) {
    return concreteIndex.interfaces().stream().anyMatch(type -> type.getName().equals(name));
  }

  /** Owner- and signature-aware index. Ambiguous simple-name requests deliberately yield empty. */
  private static final class ReferenceElementIndex {
    private final CDModelIndex index;

    private ReferenceElementIndex(CDModelIndex index) {
      this.index = index;
    }

    Optional<ISymbol> findType(String requested) {
      return index.type(requested).map(ASTCDType::getSymbol);
    }

    Optional<ISymbol> findField(List<ASTCDType> owners, String requested) {
      String normalized = requested == null ? "" : requested.trim();
      List<ASTCDAttribute> matches = new ArrayList<>();
      String fieldName = JavaSourceNames.simpleName(normalized);
      String explicitOwner = ownerPart(normalized);
      for (ASTCDType owner : searchOwners(owners, explicitOwner)) {
        index.attribute(owner.getName(), fieldName).ifPresent(matches::add);
      }
      return matches.size() == 1 ? Optional.of(matches.get(0).getSymbol()) : Optional.empty();
    }

    Optional<ISymbol> findMethod(List<ASTCDType> owners, String requested) {
      String normalized = requested == null ? "" : requested.trim();
      String explicitOwner = ownerPart(normalized);
      String methodReference = explicitOwner.isEmpty() ? normalized : memberPart(normalized);
      String methodName = JavaSourceNames.simpleName(methodReference);
      List<ASTCDMethod> matches = new ArrayList<>();
      for (ASTCDType owner : searchOwners(owners, explicitOwner)) {
        for (ASTCDMethod method : index.methods(owner.getName(), methodName)) {
          if (!methodReference.contains("(")
              || JavaSourceNames.methodSignature(method)
                  .equals(JavaMethodSignatures.normalize(methodReference))) {
            matches.add(method);
          }
        }
      }
      return matches.size() == 1 ? Optional.of(matches.get(0).getSymbol()) : Optional.empty();
    }

    private List<ASTCDType> searchOwners(List<ASTCDType> owners, String explicitOwner) {
      if (!explicitOwner.isEmpty()) {
        Optional<ASTCDType> owner = index.type(explicitOwner);
        return owner.isPresent() ? List.of(owner.get()) : List.of();
      }
      return owners == null || owners.isEmpty() ? index.types() : owners;
    }

    private static String ownerPart(String reference) {
      int open = reference.indexOf('(');
      int dot = reference.lastIndexOf('.', open < 0 ? reference.length() - 1 : open);
      return dot < 0 ? "" : JavaSourceNames.simpleName(reference.substring(0, dot));
    }

    private static String memberPart(String reference) {
      int open = reference.indexOf('(');
      int dot = reference.lastIndexOf('.', open < 0 ? reference.length() - 1 : open);
      return dot < 0 ? reference : reference.substring(dot + 1);
    }
  }
}
