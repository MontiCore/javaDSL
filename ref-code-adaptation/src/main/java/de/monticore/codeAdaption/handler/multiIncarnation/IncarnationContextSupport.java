package de.monticore.codeAdaption.handler.multiIncarnation;

import de.monticore.cd4codebasis._ast.ASTCDMethod;
import de.monticore.cdbasis._ast.ASTCDAttribute;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.codeAdaption.utils.CDModelIndex;
import de.monticore.codeAdaption.utils.JavaSourceNames;
import de.monticore.codeAdaption.utils.JavaMethodSignatures;
import de.monticore.symboltable.ISymbol;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** Shared stable-key infrastructure for the two incarnation-context construction modes. */
final class IncarnationContextSupport {
  private final CDModelIndex referenceIndex;
  private final CDModelIndex concreteIndex;
  private final ReferenceElementIndex referenceElements;

  IncarnationContextSupport(CDModelIndex referenceIndex, CDModelIndex concreteIndex) {
    this.referenceIndex = Objects.requireNonNull(referenceIndex, "referenceIndex");
    this.concreteIndex = Objects.requireNonNull(concreteIndex, "concreteIndex");
    this.referenceElements = new ReferenceElementIndex(referenceIndex);
  }

  CDModelIndex referenceIndex() {
    return referenceIndex;
  }

  CDModelIndex concreteIndex() {
    return concreteIndex;
  }

  Map<StableElementKey, List<IncarnationContext.MappedElement>> newMapping() {
    return new LinkedHashMap<>();
  }

  void addMapping(
      Map<StableElementKey, List<IncarnationContext.MappedElement>> target,
      StableElementKey reference,
      ISymbol concrete) {
    Optional<StableElementKey> concreteKey = StableElementKey.fromSymbol(concrete, concreteIndex);
    if (reference == null || concreteKey.isEmpty()) {
      return;
    }
    List<IncarnationContext.MappedElement> incarnations =
        target.computeIfAbsent(reference, ignored -> new ArrayList<>());
    boolean duplicate =
        incarnations.stream().anyMatch(existing -> existing.key().equals(concreteKey.get()));
    if (!duplicate) {
      incarnations.add(new IncarnationContext.MappedElement(concreteKey.get(), concrete));
    }
  }

  void mergeMappings(
      Map<StableElementKey, List<IncarnationContext.MappedElement>> target,
      Map<StableElementKey, List<IncarnationContext.MappedElement>> additions) {
    for (Map.Entry<StableElementKey, List<IncarnationContext.MappedElement>> entry : additions.entrySet()) {
      if (entry.getValue() == null) {
        continue;
      }
      for (IncarnationContext.MappedElement incarnation : entry.getValue()) {
        addMapping(target, entry.getKey(), incarnation.symbol());
      }
    }
  }

  IncarnationContext assembleContext(
      String mapping,
      Map<StableElementKey, List<IncarnationContext.MappedElement>> mappings) {
    return new IncarnationContext(mapping, mappings, groupingTypes(mappings));
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

  Optional<StableElementKey> findReferenceType(String name) {
    return referenceElements.findType(name);
  }

  Optional<StableElementKey> findReferenceField(List<ASTCDType> owners, String name) {
    return referenceElements.findField(owners, name);
  }

  Optional<StableElementKey> findReferenceMethod(List<ASTCDType> owners, String name) {
    return referenceElements.findMethod(owners, name);
  }

  Optional<StableElementKey> findSameNameField(
      List<ASTCDType> owners, ASTCDAttribute concreteAttribute) {
    List<StableElementKey> matches = new ArrayList<>();
    for (ASTCDType owner : owners) {
      for (ASTCDAttribute candidate : owner.getCDAttributeList()) {
        if (candidate.getName().equals(concreteAttribute.getName())) {
          matches.add(StableElementKey.field(owner, candidate));
        }
      }
    }
    return unique(matches);
  }

  Optional<StableElementKey> findSameSignatureMethod(
      List<ASTCDType> owners, ASTCDMethod concreteMethod) {
    String signature = JavaSourceNames.methodSignature(concreteMethod);
    List<StableElementKey> matches = new ArrayList<>();
    for (ASTCDType owner : owners) {
      for (ASTCDMethod candidate : owner.getCDMethodList()) {
        if (JavaSourceNames.methodSignature(candidate).equals(signature)) {
          matches.add(StableElementKey.method(owner, candidate));
        }
      }
    }
    return unique(matches);
  }

  List<ASTCDType> mappedReferenceOwners(
      Map<StableElementKey, List<IncarnationContext.MappedElement>> mappings,
      ASTCDType concreteType) {
    StableElementKey concreteKey = StableElementKey.type(concreteType);
    List<ASTCDType> owners = new ArrayList<>();
    for (Map.Entry<StableElementKey, List<IncarnationContext.MappedElement>> entry : mappings.entrySet()) {
      if (entry.getKey().getKind() != StableElementKey.Kind.TYPE) {
        continue;
      }
      if (entry.getValue().stream()
          .anyMatch(element -> element.key().equals(concreteKey))) {
        referenceIndex.type(entry.getKey().getName()).ifPresent(owners::add);
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

  private <T> Optional<T> unique(List<T> candidates) {
    if (candidates.isEmpty()) {
      return Optional.empty();
    }
    T first = candidates.get(0);
    for (int i = 1; i < candidates.size(); i++) {
      if (!Objects.equals(candidates.get(i), first)) {
        return Optional.empty();
      }
    }
    return Optional.of(first);
  }

  private Map<StableElementKey, IncarnationContext.MappedElement> groupingTypes(
      Map<StableElementKey, List<IncarnationContext.MappedElement>> mappings) {
    Map<StableElementKey, IncarnationContext.MappedElement> result = new LinkedHashMap<>();
    for (List<IncarnationContext.MappedElement> incarnations : mappings.values()) {
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
        ASTCDType groupingType = concreteIndex.type(best).orElseThrow();
        IncarnationContext.MappedElement grouping =
            new IncarnationContext.MappedElement(
                StableElementKey.type(groupingType), groupingType.getSymbol());
        for (String target : targets) {
          result.put(StableElementKey.type(target), grouping);
        }
      }
    }
    return result;
  }

  private Set<String> typeNames(List<IncarnationContext.MappedElement> elements) {
    Set<String> result = new LinkedHashSet<>();
    if (elements == null) {
      return result;
    }
    for (IncarnationContext.MappedElement element : elements) {
      StableElementKey key = element.key();
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

    Optional<StableElementKey> findType(String requested) {
      return index.type(requested).map(StableElementKey::type);
    }

    Optional<StableElementKey> findField(List<ASTCDType> owners, String requested) {
      String normalized = requested == null ? "" : requested.trim();
      List<ASTCDAttribute> matches = new ArrayList<>();
      String fieldName = JavaSourceNames.simpleName(normalized);
      String explicitOwner = ownerPart(normalized);
      for (ASTCDType owner : searchOwners(owners, explicitOwner)) {
        index.attribute(owner.getName(), fieldName).ifPresent(matches::add);
      }
      if (matches.size() != 1) {
        return Optional.empty();
      }
      ASTCDAttribute match = matches.get(0);
      return index.ownerOf(match).map(owner -> StableElementKey.field(owner, match));
    }

    Optional<StableElementKey> findMethod(List<ASTCDType> owners, String requested) {
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
      if (matches.size() != 1) {
        return Optional.empty();
      }
      ASTCDMethod match = matches.get(0);
      return index.ownerOf(match).map(owner -> StableElementKey.method(owner, match));
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
