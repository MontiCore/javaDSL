package de.monticore.codeAdaption.handler.multiIncarnation;

import de.monticore.cd4codebasis._ast.ASTCDMethod;
import de.monticore.cdbasis._ast.ASTCDAttribute;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.cdconformance.CDConfParameter;
import de.monticore.codeAdaption.utils.CDModelIndex;
import de.monticore.codeAdaption.utils.CDTypeRelations;
import de.monticore.codeAdaption.utils.JavaSourceNames;
import de.monticore.symboltable.ISymbol;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Builds incarnation contexts without invoking cdconcretization or the conformance checker.
 *
 * <p>The builder intentionally accepts only explicit stereotype mappings and deterministic
 * same-name mappings. More advanced inferred repair behavior belongs to the cdconcretization-backed
 * path.
 */
public class ManualIncarnationContextBuilder {
  private final ASTCDCompilationUnit referenceCD;
  private final ASTCDCompilationUnit concreteCD;
  private final Set<CDConfParameter> confParams;
  private final CDModelIndex referenceModelIndex;
  private final CDModelIndex concreteModelIndex;

  public ManualIncarnationContextBuilder(
      ASTCDCompilationUnit referenceCD,
      ASTCDCompilationUnit concreteCD,
      Set<CDConfParameter> confParams) {
    this.referenceCD = referenceCD;
    this.concreteCD = concreteCD;
    this.confParams = confParams;
    this.referenceModelIndex = CDModelIndex.of(referenceCD);
    this.concreteModelIndex = CDModelIndex.of(concreteCD);
  }

  /**
   * Builds one manual context for a mapping name without invoking cdconcretization or the
   * conformance checker.
   */
  public IncarnationContext buildContextForMapping(String mapping) {
    Map<ISymbol, List<ISymbol>> referenceToIncarnations = new LinkedHashMap<>();
    ReferenceIndex referenceIndex = ReferenceIndex.of(referenceModelIndex);

    collectTypeMappings(mapping, referenceIndex, referenceToIncarnations);
    collectMemberMappings(mapping, referenceIndex, referenceToIncarnations);
    collectForEachMappings(referenceIndex, referenceToIncarnations);

    Map<ISymbol, List<ISymbol>> interfaceToImplementers = extractInterfaceImplementersFromAST();
    Map<ISymbol, StableElementKey> symbolKeys = buildSymbolKeyMap();
    ResolvedIncarnationContext resolved =
        buildResolvedContext(mapping, referenceToIncarnations, symbolKeys);

    IncarnationContext context =
        new IncarnationContext(
            mapping, referenceToIncarnations, interfaceToImplementers, symbolKeys, resolved);
    context.setConcreteToGroupingType(computeGroupingMap(referenceToIncarnations, interfaceToImplementers));
    return context;
  }

  private void collectTypeMappings(
      String mapping,
      ReferenceIndex referenceIndex,
      Map<ISymbol, List<ISymbol>> referenceToIncarnations) {
    for (ASTCDType concreteType : concreteModelIndex.types()) {
      Optional<String> explicit = getStereotypeValue(concreteType, mapping);
      if (explicit.isPresent()) {
        referenceIndex.findType(explicit.get()).ifPresent(ref -> addMapping(referenceToIncarnations, ref, concreteType.getSymbol()));
        continue;
      }
      if (confParams.contains(CDConfParameter.NAME_MAPPING)) {
        referenceIndex.findType(concreteType.getName()).ifPresent(ref -> addMapping(referenceToIncarnations, ref, concreteType.getSymbol()));
      }
    }
  }

  private void collectMemberMappings(
      String mapping,
      ReferenceIndex referenceIndex,
      Map<ISymbol, List<ISymbol>> referenceToIncarnations) {
    for (ASTCDType concreteType : concreteModelIndex.types()) {
      List<ASTCDType> referenceOwners = mappedReferenceOwners(referenceToIncarnations, concreteType);
      for (ASTCDAttribute concreteAttribute : concreteType.getCDAttributeList()) {
        Optional<String> explicit = getStereotypeValue(concreteAttribute, mapping);
        if (explicit.isPresent()) {
          referenceIndex
              .findField(referenceOwners, explicit.get())
              .ifPresent(ref -> addMapping(referenceToIncarnations, ref, concreteAttribute.getSymbol()));
        } else if (confParams.contains(CDConfParameter.NAME_MAPPING)) {
          findSameNameField(referenceOwners, concreteAttribute)
              .ifPresent(ref -> addMapping(referenceToIncarnations, ref, concreteAttribute.getSymbol()));
        }
      }

      for (ASTCDMethod concreteMethod : concreteType.getCDMethodList()) {
        Optional<String> explicit = getStereotypeValue(concreteMethod, mapping);
        if (explicit.isPresent()) {
          referenceIndex
              .findMethod(referenceOwners, explicit.get())
              .ifPresent(ref -> addMapping(referenceToIncarnations, ref, concreteMethod.getSymbol()));
        } else if (confParams.contains(CDConfParameter.NAME_MAPPING)) {
          findSameNameMethod(referenceOwners, concreteMethod)
              .ifPresent(ref -> addMapping(referenceToIncarnations, ref, concreteMethod.getSymbol()));
        }
      }
    }
  }

  /**
   * Derives manual {@code <<forEach="...">>} incarnations from mappings that are already known
   * through explicit stereotypes or deterministic name matching.
   */
  private void collectForEachMappings(
      ReferenceIndex referenceIndex, Map<ISymbol, List<ISymbol>> referenceToIncarnations) {
    for (ASTCDType referenceType : referenceModelIndex.types()) {
      getStereotypeValue(referenceType, "forEach")
          .flatMap(referenceIndex::findType)
          .ifPresent(
              target ->
                  copyIncarnations(referenceToIncarnations, target, referenceType.getSymbol()));

      for (ASTCDAttribute referenceAttribute : referenceType.getCDAttributeList()) {
        getStereotypeValue(referenceAttribute, "forEach")
            .flatMap(value -> referenceIndex.findField(List.of(referenceType), value))
            .ifPresent(
                target ->
                    copyIncarnations(
                        referenceToIncarnations, target, referenceAttribute.getSymbol()));
      }

      for (ASTCDMethod referenceMethod : referenceType.getCDMethodList()) {
        Optional<String> forEachTarget = getStereotypeValue(referenceMethod, "forEach");
        if (forEachTarget.isEmpty()) {
          continue;
        }
        Optional<ISymbol> targetMethod =
            referenceIndex.findMethod(List.of(referenceType), forEachTarget.get());
        if (targetMethod.isPresent()) {
          copyIncarnations(referenceToIncarnations, targetMethod.get(), referenceMethod.getSymbol());
          continue;
        }
        Optional<ISymbol> targetAttribute =
            referenceIndex.findField(List.of(referenceType), forEachTarget.get());
        if (targetAttribute.isPresent()) {
          collectForEachMethodMappings(referenceToIncarnations, targetAttribute.get(), referenceMethod);
        }
      }
    }
  }

  /**
   * Maps a forEach method over already-mapped attribute incarnations by deriving deterministic
   * concrete method names from exact or camel-case placeholder segments only.
   */
  private void collectForEachMethodMappings(
      Map<ISymbol, List<ISymbol>> referenceToIncarnations,
      ISymbol targetAttribute,
      ASTCDMethod referenceMethod) {
    List<ISymbol> targetIncarnations = referenceToIncarnations.get(targetAttribute);
    if (targetIncarnations == null || targetIncarnations.isEmpty()) {
      return;
    }
    for (ISymbol targetIncarnation : targetIncarnations) {
      if (!(targetIncarnation.getAstNode() instanceof ASTCDAttribute concreteAttribute)) {
        continue;
      }
      Optional<ASTCDType> concreteOwner = findConcreteOwner(concreteAttribute);
      if (concreteOwner.isEmpty()) {
        continue;
      }
      Set<String> candidateNames =
          forEachMethodNameCandidates(
              referenceMethod.getName(), targetAttribute.getName(), concreteAttribute.getName());
      for (ASTCDMethod concreteMethod : concreteOwner.get().getCDMethodList()) {
        if (candidateNames.contains(concreteMethod.getName())
            && concreteMethod.getCDParameterList().size()
                == referenceMethod.getCDParameterList().size()) {
          addMapping(referenceToIncarnations, referenceMethod.getSymbol(), concreteMethod.getSymbol());
        }
      }
    }
  }

  private void copyIncarnations(
      Map<ISymbol, List<ISymbol>> referenceToIncarnations, ISymbol target, ISymbol reference) {
    List<ISymbol> targetIncarnations = referenceToIncarnations.get(target);
    if (targetIncarnations == null) {
      return;
    }
    for (ISymbol incarnation : targetIncarnations) {
      addMapping(referenceToIncarnations, reference, incarnation);
    }
  }

  private Optional<ASTCDType> findConcreteOwner(ASTCDAttribute attribute) {
    return concreteModelIndex.ownerOf(attribute);
  }

  private Set<String> forEachMethodNameCandidates(
      String referenceMethodName, String referenceTargetName, String concreteTargetName) {
    Set<String> result = new LinkedHashSet<>();
    if (referenceMethodName.equals(referenceTargetName)) {
      result.add(concreteTargetName);
    }
    if (referenceMethodName.equals(capitalize(referenceTargetName))) {
      result.add(capitalize(concreteTargetName));
    }
    replaceNameSegment(referenceMethodName, referenceTargetName, concreteTargetName)
        .ifPresent(result::add);
    replaceNameSegment(
            referenceMethodName, capitalize(referenceTargetName), capitalize(concreteTargetName))
        .ifPresent(result::add);
    replaceNameSegment(
            referenceMethodName, uncapitalize(referenceTargetName), uncapitalize(concreteTargetName))
        .ifPresent(result::add);
    return result;
  }

  private static Optional<String> replaceNameSegment(
      String name, String referenceSegment, String concreteSegment) {
    if (name == null
        || referenceSegment == null
        || referenceSegment.isEmpty()
        || concreteSegment == null
        || concreteSegment.isEmpty()) {
      return Optional.empty();
    }
    List<String> segments = splitNameSegments(name);
    boolean changed = false;
    StringBuilder rewritten = new StringBuilder(name.length() + concreteSegment.length());
    for (String segment : segments) {
      if (segment.equals(referenceSegment)) {
        rewritten.append(concreteSegment);
        changed = true;
      } else {
        rewritten.append(segment);
      }
    }
    return changed ? Optional.of(rewritten.toString()) : Optional.empty();
  }

  private static List<String> splitNameSegments(String name) {
    List<String> segments = new ArrayList<>();
    int start = 0;
    for (int i = 1; i < name.length(); i++) {
      char previous = name.charAt(i - 1);
      char current = name.charAt(i);
      if (Character.isUpperCase(current)
          && (Character.isLowerCase(previous)
              || (i + 1 < name.length() && Character.isLowerCase(name.charAt(i + 1))))) {
        segments.add(name.substring(start, i));
        start = i;
      }
    }
    segments.add(name.substring(start));
    return segments;
  }

  private static String capitalize(String value) {
    return JavaSourceNames.capitalize(value);
  }

  private static String uncapitalize(String value) {
    return JavaSourceNames.uncapitalize(value);
  }

  private List<ASTCDType> mappedReferenceOwners(
      Map<ISymbol, List<ISymbol>> referenceToIncarnations, ASTCDType concreteType) {
    List<ASTCDType> result = new ArrayList<>();
    for (Map.Entry<ISymbol, List<ISymbol>> entry : referenceToIncarnations.entrySet()) {
      if (!(entry.getKey().getAstNode() instanceof ASTCDType refType)) {
        continue;
      }
      for (ISymbol incarnation : entry.getValue()) {
        if (incarnation.getName().equals(concreteType.getName())) {
          result.add(refType);
          break;
        }
      }
    }
    return result;
  }

  private Optional<ISymbol> findSameNameField(
      List<ASTCDType> referenceOwners, ASTCDAttribute concreteAttribute) {
    List<ASTCDAttribute> matches = new ArrayList<>();
    for (ASTCDType owner : referenceOwners) {
      for (ASTCDAttribute candidate : owner.getCDAttributeList()) {
        if (candidate.getName().equals(concreteAttribute.getName())) {
          matches.add(candidate);
        }
      }
    }
    return matches.size() == 1 ? Optional.of(matches.get(0).getSymbol()) : Optional.empty();
  }

  private Optional<ISymbol> findSameNameMethod(
      List<ASTCDType> referenceOwners, ASTCDMethod concreteMethod) {
    List<ASTCDMethod> matches = new ArrayList<>();
    for (ASTCDType owner : referenceOwners) {
      for (ASTCDMethod candidate : owner.getCDMethodList()) {
        if (candidate.getName().equals(concreteMethod.getName())
            && candidate.getCDParameterList().size() == concreteMethod.getCDParameterList().size()) {
          matches.add(candidate);
        }
      }
    }
    return matches.size() == 1 ? Optional.of(matches.get(0).getSymbol()) : Optional.empty();
  }

  private ResolvedIncarnationContext buildResolvedContext(
      String mapping,
      Map<ISymbol, List<ISymbol>> referenceToIncarnations,
      Map<ISymbol, StableElementKey> symbolKeys) {
    ResolvedIncarnationContext resolved = new ResolvedIncarnationContext(mapping);
    for (Map.Entry<ISymbol, List<ISymbol>> entry : referenceToIncarnations.entrySet()) {
      StableElementKey referenceKey = symbolKeys.get(entry.getKey());
      for (ISymbol incarnation : entry.getValue()) {
        resolved.addMapping(referenceKey, symbolKeys.get(incarnation), incarnation);
      }
    }
    return resolved;
  }

  private Map<ISymbol, StableElementKey> buildSymbolKeyMap() {
    Map<ISymbol, StableElementKey> result = new IdentityHashMap<>();
    registerSymbolKeys(referenceCD, result);
    registerSymbolKeys(concreteCD, result);
    return result;
  }

  private void registerSymbolKeys(ASTCDCompilationUnit cd, Map<ISymbol, StableElementKey> result) {
    CDModelIndex index = cd == referenceCD ? referenceModelIndex : concreteModelIndex;
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

  private Map<ISymbol, List<ISymbol>> extractInterfaceImplementersFromAST() {
    Map<ISymbol, List<ISymbol>> result = new LinkedHashMap<>();
    for (ASTCDType concreteType : concreteModelIndex.types()) {
      for (String implName : CDTypeRelations.interfaceNames(concreteType)) {
        String simpleImplName = simpleName(implName);
        concreteModelIndex
            .type(simpleImplName)
            .ifPresent(possibleInterface -> addMapping(result, possibleInterface.getSymbol(), concreteType.getSymbol()));
      }
    }
    return result;
  }

  private Map<String, String> computeGroupingMap(
      Map<ISymbol, List<ISymbol>> referenceToIncarnations,
      Map<ISymbol, List<ISymbol>> interfaceToImplementers) {
    Map<String, String> result = new HashMap<>();
    for (List<ISymbol> incarnations : referenceToIncarnations.values()) {
      if (incarnations == null || incarnations.size() < 2) {
        continue;
      }
      Set<String> incarnationNames = new LinkedHashSet<>();
      for (ISymbol incarnation : incarnations) {
        incarnationNames.add(incarnation.getName());
      }
      for (Map.Entry<ISymbol, List<ISymbol>> entry : interfaceToImplementers.entrySet()) {
        Set<String> implementerNames = new LinkedHashSet<>();
        for (ISymbol implementer : entry.getValue()) {
          implementerNames.add(implementer.getName());
        }
        if (implementerNames.equals(incarnationNames)) {
          for (String incarnationName : incarnationNames) {
            result.put(incarnationName, entry.getKey().getName());
          }
        }
      }
    }
    return result;
  }

  private static void addMapping(Map<ISymbol, List<ISymbol>> target, ISymbol reference, ISymbol concrete) {
    if (reference == null || concrete == null) {
      return;
    }
    List<ISymbol> values = target.computeIfAbsent(reference, ignored -> new ArrayList<>());
    boolean exists = values.stream().anyMatch(existing -> existing.getName().equals(concrete.getName()));
    if (!exists) {
      values.add(concrete);
    }
  }

  private Optional<String> getStereotypeValue(ASTCDType cdType, String mapping) {
    if (cdType.getModifier() == null || !cdType.getModifier().isPresentStereotype()) {
      return Optional.empty();
    }
    for (var stereotype : cdType.getModifier().getStereotype().getValuesList()) {
      if (mapping.equals(stereotype.getName())) {
        try {
          return Optional.ofNullable(stereotype.getValue());
        } catch (Exception ignored) {
          return Optional.empty();
        }
      }
    }
    return Optional.empty();
  }

  private Optional<String> getStereotypeValue(ASTCDAttribute cdAttribute, String mapping) {
    if (cdAttribute.getModifier() == null || !cdAttribute.getModifier().isPresentStereotype()) {
      return Optional.empty();
    }
    for (var stereotype : cdAttribute.getModifier().getStereotype().getValuesList()) {
      if (mapping.equals(stereotype.getName())) {
        try {
          return Optional.ofNullable(stereotype.getValue());
        } catch (Exception ignored) {
          return Optional.empty();
        }
      }
    }
    return Optional.empty();
  }

  private Optional<String> getStereotypeValue(ASTCDMethod cdMethod, String mapping) {
    if (cdMethod.getModifier() == null || !cdMethod.getModifier().isPresentStereotype()) {
      return Optional.empty();
    }
    for (var stereotype : cdMethod.getModifier().getStereotype().getValuesList()) {
      if (mapping.equals(stereotype.getName())) {
        try {
          return Optional.ofNullable(stereotype.getValue());
        } catch (Exception ignored) {
          return Optional.empty();
        }
      }
    }
    return Optional.empty();
  }

  private static String simpleName(String name) {
    return JavaSourceNames.simpleName(name);
  }

  private static String methodSignature(ASTCDMethod method) {
    return JavaSourceNames.methodSignature(method);
  }

  private static final class ReferenceIndex {
    private final Map<String, ASTCDType> types = new LinkedHashMap<>();
    private final Map<String, ASTCDAttribute> fields = new LinkedHashMap<>();
    private final Map<String, ASTCDMethod> methods = new LinkedHashMap<>();
    private final Map<String, List<ASTCDAttribute>> simpleFields = new LinkedHashMap<>();
    private final Map<String, List<ASTCDMethod>> simpleMethods = new LinkedHashMap<>();

    static ReferenceIndex of(CDModelIndex referenceIndex) {
      ReferenceIndex index = new ReferenceIndex();
      for (ASTCDType type : referenceIndex.types()) {
        index.types.put(type.getName(), type);
        for (ASTCDAttribute field : type.getCDAttributeList()) {
          index.fields.put(type.getName() + "." + field.getName(), field);
          index.simpleFields.computeIfAbsent(field.getName(), ignored -> new ArrayList<>()).add(field);
        }
        for (ASTCDMethod method : type.getCDMethodList()) {
          index.methods.put(type.getName() + "." + methodSignature(method), method);
          index.simpleMethods.computeIfAbsent(method.getName(), ignored -> new ArrayList<>()).add(method);
        }
      }
      return index;
    }

    Optional<ISymbol> findType(String referenceName) {
      ASTCDType type = types.get(simpleName(referenceName));
      return type == null ? Optional.empty() : Optional.of(type.getSymbol());
    }

    Optional<ISymbol> findField(List<ASTCDType> owners, String referenceName) {
      String normalized = referenceName.trim();
      ASTCDAttribute direct = fields.get(normalized);
      if (direct == null && normalized.contains(".")) {
        direct = fields.get(simpleOwner(normalized) + "." + simpleName(normalized));
      }
      if (direct != null) {
        return Optional.of(direct.getSymbol());
      }
      List<ASTCDAttribute> matches = new ArrayList<>();
      String simple = simpleName(referenceName);
      for (ASTCDType owner : owners) {
        ASTCDAttribute field = fields.get(owner.getName() + "." + simple);
        if (field != null) {
          matches.add(field);
        }
      }
      if (matches.isEmpty()) {
        matches.addAll(simpleFields.getOrDefault(simple, List.of()));
      }
      return matches.size() == 1 ? Optional.of(matches.get(0).getSymbol()) : Optional.empty();
    }

    Optional<ISymbol> findMethod(List<ASTCDType> owners, String referenceName) {
      String normalized = referenceName.trim();
      if (normalized.contains(".") && normalized.contains("(")) {
        ASTCDMethod direct = methods.get(normalized);
        if (direct != null) {
          return Optional.of(direct.getSymbol());
        }
      }
      List<ASTCDMethod> matches = new ArrayList<>();
      String simple = simpleName(referenceName);
      for (ASTCDType owner : owners) {
        if (referenceName.contains("(")) {
          ASTCDMethod method = methods.get(owner.getName() + "." + normalized);
          if (method == null && normalized.contains(".")) {
            method = methods.get(owner.getName() + "." + normalized.substring(normalized.lastIndexOf('.') + 1));
          }
          if (method != null) {
            matches.add(method);
          }
        } else {
          for (ASTCDMethod method : owner.getCDMethodList()) {
            if (method.getName().equals(simple)) {
              matches.add(method);
            }
          }
        }
      }
      if (matches.isEmpty() && !referenceName.contains("(")) {
        matches.addAll(simpleMethods.getOrDefault(simple, List.of()));
      }
      return matches.size() == 1 ? Optional.of(matches.get(0).getSymbol()) : Optional.empty();
    }

    private static String simpleOwner(String qualifiedName) {
      int lastDot = qualifiedName.lastIndexOf('.');
      if (lastDot < 0) {
        return qualifiedName;
      }
      String owner = qualifiedName.substring(0, lastDot);
      int ownerDot = owner.lastIndexOf('.');
      return ownerDot < 0 ? owner : owner.substring(ownerDot + 1);
    }
  }
}
