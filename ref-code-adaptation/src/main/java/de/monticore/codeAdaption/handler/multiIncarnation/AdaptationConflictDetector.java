package de.monticore.codeAdaption.handler.multiIncarnation;

import de.monticore.cd4codebasis._ast.ASTCDMethod;
import de.monticore.cd4codebasis._ast.ASTCDParameter;
import de.monticore.cdassociation._ast.ASTCDAssocSide;
import de.monticore.cdassociation._ast.ASTCDAssociation;
import de.monticore.cdbasis._ast.ASTCDAttribute;
import de.monticore.cdbasis._ast.ASTCDClass;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.cdconformance.CDConfParameter;
import de.monticore.cdinterfaceandenum._ast.ASTCDEnum;
import de.monticore.cdinterfaceandenum._ast.ASTCDInterface;
import de.monticore.codeAdaption.CodeAdaptationException;
import de.monticore.codeAdaption.utils.CDModelIndex;
import de.monticore.codeAdaption.utils.CDTypeRelations;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.codeAdaption.utils.JavaSourceNames;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Detects manual adaptation conflicts that cdconcretization would otherwise repair or reject.
 *
 * <p>The detector works on already built incarnation contexts. It therefore reports only cases
 * where the chosen concrete incarnations cannot safely realize the reference model without
 * producing duplicate members, ambiguous associations, or incompatible inheritance/member shapes.
 */
public class AdaptationConflictDetector {
  private final Set<String> mappings;
  private final Map<String, IncarnationContext> contexts;
  private final Set<CDConfParameter> confParams;
  private final boolean useCommonParentForMultipleIncarnations;
  private final List<String> conflicts = new ArrayList<>();
  private final CDModelIndex referenceIndex;
  private final CDModelIndex concreteIndex;
  private final Map<String, ASTCDType> referenceTypes = new LinkedHashMap<>();
  private final Map<String, ASTCDType> concreteTypes = new LinkedHashMap<>();

  public AdaptationConflictDetector(
      ASTCDCompilationUnit referenceCD,
      ASTCDCompilationUnit concreteCD,
      Set<String> mappings,
      Map<String, IncarnationContext> contexts,
      Set<CDConfParameter> confParams) {
    this(referenceCD, concreteCD, mappings, contexts, confParams, false);
  }

  public AdaptationConflictDetector(
      ASTCDCompilationUnit referenceCD,
      ASTCDCompilationUnit concreteCD,
      Set<String> mappings,
      Map<String, IncarnationContext> contexts,
      Set<CDConfParameter> confParams,
      boolean useCommonParentForMultipleIncarnations) {
    this.mappings = mappings;
    this.contexts = contexts;
    this.confParams = confParams;
    this.useCommonParentForMultipleIncarnations = useCommonParentForMultipleIncarnations;
    this.referenceIndex = CDModelIndex.of(referenceCD);
    this.concreteIndex = CDModelIndex.of(concreteCD);
    referenceIndex.types().forEach(type -> referenceTypes.put(type.getName(), type));
    concreteIndex.types().forEach(type -> concreteTypes.put(type.getName(), type));
  }

  /**
   * Validates manual, non-concretizing adaptation contexts and throws one combined
   * {@link CodeAdaptationException} when unresolved risks are found.
   */
  public static void validate(
      ASTCDCompilationUnit referenceCD,
      ASTCDCompilationUnit concreteCD,
      Set<String> mappings,
      Map<String, IncarnationContext> contexts,
      Set<CDConfParameter> confParams) {
    validate(referenceCD, concreteCD, mappings, contexts, confParams, false);
  }

  /**
   * Validates manual adaptation conflicts before Java output is cleaned or generated.
   *
   * <p>The detector covers type-kind mismatches, inheritance issues, duplicate or incompatible
   * fields and methods, enum-order conflicts, association-derived field conflicts, underspecified
   * {@code any} types, ambiguous stereotypes, and missing manual {@code forEach} targets. The
   * common-parent flag permits selected class/interface mismatches when the manual context can
   * safely route generation through an available parent/interface.
   */
  public static void validate(
      ASTCDCompilationUnit referenceCD,
      ASTCDCompilationUnit concreteCD,
      Set<String> mappings,
      Map<String, IncarnationContext> contexts,
      Set<CDConfParameter> confParams,
      boolean useCommonParentForMultipleIncarnations) {
    AdaptationConflictDetector detector =
        new AdaptationConflictDetector(
            referenceCD,
            concreteCD,
            mappings,
            contexts,
            confParams,
            useCommonParentForMultipleIncarnations);
    detector.detect();
  }

  private void detect() {
    validateManualMappings();
    validateConcreteTypeStructure();
    validateConcreteMembers();
    validateEnums();
    validateAssociations();
    validateUnderspecifiedTypes();
    validateAmbiguousStereotypes();
    validateForEachMappings();

    if (!conflicts.isEmpty()) {
      conflicts.sort(String::compareTo);
      throw new CodeAdaptationException(
          "Manual code adaptation detected conflicts before output generation:"
              + System.lineSeparator()
              + "- "
              + String.join(System.lineSeparator() + "- ", conflicts));
    }
  }

  /**
   * Validates conflicts that come directly from the manual incarnation context.
   *
   * <p>This covers explicit type, field, and method mappings before looking at derived structure.
   * A conflict here means the context itself maps one reference element to concrete elements that
   * cannot all stand in for it.
   */
  private void validateManualMappings() {
    for (IncarnationContext context : contexts.values()) {
      for (Map.Entry<StableElementKey, List<StableElementKey>> entry :
          context.getStableMappings().entrySet()) {
        StableElementKey reference = entry.getKey();
        for (StableElementKey concrete : entry.getValue()) {
          if (reference.getKind() == StableElementKey.Kind.TYPE) {
            validateTypeMapping(context, reference, concrete);
          }
        }
        if (reference.getKind() == StableElementKey.Kind.FIELD && entry.getValue().size() > 1) {
          validateFieldTargets(context.getMappingName(), reference, entry.getValue());
        }
        if (reference.getKind() == StableElementKey.Kind.METHOD && entry.getValue().size() > 1) {
          validateMethodTargets(context.getMappingName(), reference, entry.getValue());
        }
      }
    }
  }

  /**
   * Checks whether a mapped reference type and concrete type have compatible CD kinds.
   *
   * <p>Classes, interfaces, and enums are not generally interchangeable. The only tolerated
   * mismatch is the common-parent case used for multiple incarnations, where a concrete class and
   * one of its grouping interfaces can jointly realize the reference type.
   */
  private void validateTypeMapping(
      IncarnationContext context, StableElementKey referenceKey, StableElementKey concreteKey) {
    ASTCDType reference = referenceTypes.get(referenceKey.getName());
    ASTCDType concrete = concreteTypes.get(concreteKey.getName());
    if (reference == null || concrete == null) {
      return;
    }
    if (!typeKind(reference).equals(typeKind(concrete))) {
      if (canUseCommonParentForTypeMismatch(context, referenceKey, reference, concrete)) {
        return;
      }
      conflict(
          context.getMappingName(),
          "type-kind mismatch",
          reference.getName() + " is a " + typeKind(reference) + " but "
              + concrete.getName() + " is a " + typeKind(concrete));
    }
  }

  /**
   * Returns whether a class/interface mismatch is explained by a configured grouping parent.
   *
   * <p>When multiple concrete types incarnate the same reference type, a common interface can act
   * as the stable reference point for generated code. In that mode, the mismatch is not reported as
   * a type-kind conflict.
   */
  private boolean canUseCommonParentForTypeMismatch(
      IncarnationContext context,
      StableElementKey referenceKey,
      ASTCDType reference,
      ASTCDType concrete) {
    if (!useCommonParentForMultipleIncarnations) {
      return false;
    }
    boolean referenceClassConcreteInterface =
        reference instanceof ASTCDClass && concrete instanceof ASTCDInterface;
    boolean referenceInterfaceConcreteClass =
        reference instanceof ASTCDInterface && concrete instanceof ASTCDClass;
    if (referenceClassConcreteInterface) {
      return true;
    }
    if (!referenceInterfaceConcreteClass) {
      return false;
    }
    if (context.findGroupingTypeForImplementer(concrete.getName()).isPresent()) {
      return true;
    }
    Set<String> mappedConcreteTypes = new LinkedHashSet<>();
    for (var incarnation : context.getIncarnations(referenceKey)) {
      mappedConcreteTypes.add(incarnation.getKey().getName());
    }
    for (String parentName : concreteParentNames(concrete)) {
      if (mappedConcreteTypes.contains(parentName)) {
        return true;
      }
    }
    return !concreteParentNames(concrete).isEmpty();
  }

  /**
   * Detects field mappings that collapse incompatible concrete fields into one reference field.
   *
   * <p>The same concrete owner/name pair may appear more than once, but it must keep the same field
   * type. Different types would make the adapted reference field ambiguous.
   */
  private void validateFieldTargets(
      String mapping, StableElementKey reference, List<StableElementKey> targets) {
    Map<String, String> byOwnerAndName = new LinkedHashMap<>();
    for (StableElementKey target : targets) {
      String key = target.getOwnerType().orElse("") + "." + target.getName();
      String type = target.getFieldKind().orElse("Object");
      String previous = byOwnerAndName.putIfAbsent(key, type);
      if (previous != null && !previous.equals(type)) {
        conflict(
            mapping,
            "field target conflict",
            reference.signature() + " maps to incompatible concrete fields " + targets);
      }
    }
  }

  /**
   * Detects method mappings that collapse incompatible concrete methods into one reference method.
   *
   * <p>Methods with the same signature must agree on their return type. Otherwise generated calls
   * to the reference method would not have a single stable Java type.
   */
  private void validateMethodTargets(
      String mapping, StableElementKey reference, List<StableElementKey> targets) {
    Map<String, String> bySignature = new LinkedHashMap<>();
    for (StableElementKey target : targets) {
      String signature = target.signature();
      String returnType = target.getReturnType().orElse("void");
      String previous = bySignature.putIfAbsent(signature, returnType);
      if (previous != null && !previous.equals(returnType)) {
        conflict(
            mapping,
            "method target conflict",
            reference.signature() + " maps to incompatible concrete methods " + targets);
      }
    }
  }

  /**
   * Validates concrete type declarations that are illegal or unsafe before adaptation starts.
   *
   * <p>This catches structural problems such as multiple class super types, invalid extends or
   * implements targets, and inheritance cycles.
   */
  private void validateConcreteTypeStructure() {
    for (ASTCDType type : concreteTypes.values()) {
      if (type instanceof ASTCDClass cdClass && cdClass.getSuperclassList().size() > 1) {
        conflict(
            "structure",
            "illegal multiple class supertypes",
            type.getName() + " extends more than one class");
      }
      validateSuperTypeKinds(type);
    }
    validateInheritanceCycles();
  }

  /**
   * Checks whether each extends or implements edge points to a type of the required kind.
   *
   * <p>Classes may extend classes and implement interfaces. Interfaces may extend interfaces. Any
   * other combination is rejected because generated Java would be invalid.
   */
  private void validateSuperTypeKinds(ASTCDType type) {
    if (type instanceof ASTCDClass) {
      CDTypeRelations.firstSuperclassName(type)
          .map(this::simpleName)
          .map(concreteTypes::get)
          .filter(ASTCDInterface.class::isInstance)
          .ifPresent(
              superType ->
                  conflict(
                      "structure",
                      "class extends interface",
                      type.getName() + " extends interface " + superType.getName()));
      for (String interfaceName : CDTypeRelations.interfaceNames(type)) {
        ASTCDType implemented = concreteTypes.get(simpleName(interfaceName));
        if (implemented != null && !(implemented instanceof ASTCDInterface)) {
          conflict(
              "structure",
              "class implements non-interface",
              type.getName() + " implements " + implemented.getName());
        }
      }
    } else if (type instanceof ASTCDInterface) {
      for (String interfaceName : CDTypeRelations.interfaceNames(type)) {
        ASTCDType extended = concreteTypes.get(simpleName(interfaceName));
        if (extended != null && !(extended instanceof ASTCDInterface)) {
          conflict(
              "structure",
              "interface extends non-interface",
              type.getName() + " extends " + extended.getName());
        }
      }
    }
  }

  /**
   * Detects inheritance cycles in the concrete CD.
   *
   * <p>The check includes class inheritance and interface inheritance/implementation edges. A cycle
   * is reported before generation because Java cannot compile such a hierarchy.
   */
  private void validateInheritanceCycles() {
    Map<String, Set<String>> edges = new LinkedHashMap<>();
    for (ASTCDType type : concreteTypes.values()) {
      Set<String> parents = new LinkedHashSet<>();
      CDTypeRelations.firstSuperclassName(type).map(this::simpleName).ifPresent(parents::add);
      for (String interfaceName : CDTypeRelations.interfaceNames(type)) {
        parents.add(simpleName(interfaceName));
      }
      parents.removeIf(parent -> !concreteTypes.containsKey(parent));
      edges.put(type.getName(), parents);
    }
    for (String typeName : edges.keySet()) {
      if (hasCycle(typeName, edges, new LinkedHashSet<>(), new HashSet<>())) {
        conflict("structure", "inheritance cycle", "cycle reaches " + typeName);
      }
    }
  }

  private boolean hasCycle(
      String current, Map<String, Set<String>> edges, Set<String> active, Set<String> done) {
    if (active.contains(current)) {
      return true;
    }
    if (!done.add(current)) {
      return false;
    }
    active.add(current);
    for (String parent : edges.getOrDefault(current, Set.of())) {
      if (hasCycle(parent, edges, active, done)) {
        return true;
      }
    }
    active.remove(current);
    return false;
  }

  /**
   * Validates duplicate fields and methods declared in each concrete type.
   *
   * <p>Duplicate field names or method signatures are only problematic here when their Java types
   * disagree. Equal declarations are tolerated because they do not introduce an adaptation choice.
   */
  private void validateConcreteMembers() {
    for (ASTCDType type : concreteTypes.values()) {
      Map<String, String> fields = new LinkedHashMap<>();
      for (ASTCDAttribute field : type.getCDAttributeList()) {
        String previous = fields.putIfAbsent(field.getName(), printType(field));
        if (previous != null && !previous.equals(printType(field))) {
          conflict(
              "structure",
              "duplicate field",
              type.getName() + "." + field.getName() + " has incompatible types");
        }
      }
      validateInheritedFieldConflicts(type, fields);

      Map<String, String> methods = new LinkedHashMap<>();
      for (ASTCDMethod method : type.getCDMethodList()) {
        String previous = methods.putIfAbsent(methodSignature(method), returnType(method));
        if (previous != null && !previous.equals(returnType(method))) {
          conflict(
              "structure",
              "duplicate method",
              type.getName() + "." + methodSignature(method) + " has incompatible return types");
        }
      }
    }
  }

  /**
   * Detects fields that hide inherited fields with incompatible types.
   *
   * <p>Generated code can safely use an inherited field name only if the visible field has the same
   * type throughout the hierarchy. A different local type would make references through a parent or
   * child view disagree.
   */
  private void validateInheritedFieldConflicts(ASTCDType type, Map<String, String> ownFields) {
    ArrayDeque<String> queue = new ArrayDeque<>();
    CDTypeRelations.firstSuperclassName(type).map(this::simpleName).ifPresent(queue::add);
    for (String interfaceName : CDTypeRelations.interfaceNames(type)) {
      queue.add(simpleName(interfaceName));
    }
    Set<String> visited = new HashSet<>();
    while (!queue.isEmpty()) {
      ASTCDType parent = concreteTypes.get(queue.removeFirst());
      if (parent == null || !visited.add(parent.getName())) {
        continue;
      }
      for (ASTCDAttribute inherited : parent.getCDAttributeList()) {
        String ownType = ownFields.get(inherited.getName());
        if (ownType != null && !ownType.equals(printType(inherited))) {
          conflict(
              "structure",
              "inherited field conflict",
              type.getName() + "." + inherited.getName()
                  + " conflicts with inherited "
                  + parent.getName()
                  + "."
                  + inherited.getName());
        }
      }
      CDTypeRelations.firstSuperclassName(parent).map(this::simpleName).ifPresent(queue::add);
      for (String interfaceName : CDTypeRelations.interfaceNames(parent)) {
        queue.add(simpleName(interfaceName));
      }
    }
  }

  /**
   * Validates mapped enum types.
   *
   * <p>Only constants shared by the reference and concrete enum are compared. Missing or additional
   * constants are handled by the normal adaptation flow; this check only rejects order changes that
   * would alter the meaning of ordinal-sensitive generated code.
   */
  private void validateEnums() {
    for (IncarnationContext context : contexts.values()) {
      for (Map.Entry<StableElementKey, List<StableElementKey>> entry :
          context.getStableMappings().entrySet()) {
        if (entry.getKey().getKind() != StableElementKey.Kind.TYPE) {
          continue;
        }
        ASTCDType reference = referenceTypes.get(entry.getKey().getName());
        if (!(reference instanceof ASTCDEnum referenceEnum)) {
          continue;
        }
        for (StableElementKey target : entry.getValue()) {
          ASTCDType concrete = concreteTypes.get(target.getName());
          if (concrete instanceof ASTCDEnum concreteEnum) {
            validateEnumOrder(context.getMappingName(), referenceEnum, concreteEnum);
          }
        }
      }
    }
  }

  /** Reports an enum order conflict when common constants appear in a different relative order. */
  private void validateEnumOrder(String mapping, ASTCDEnum referenceEnum, ASTCDEnum concreteEnum) {
    Map<String, Integer> concretePositions = new HashMap<>();
    for (int i = 0; i < concreteEnum.getCDEnumConstantList().size(); i++) {
      concretePositions.put(concreteEnum.getCDEnumConstant(i).getName(), i);
    }
    int lastSeen = -1;
    for (var referenceConstant : referenceEnum.getCDEnumConstantList()) {
      Integer concretePosition = concretePositions.get(referenceConstant.getName());
      if (concretePosition == null) {
        continue;
      }
      if (concretePosition < lastSeen) {
        conflict(
            mapping,
            "enum order conflict",
            concreteEnum.getName() + " orders reference constants differently from "
                + referenceEnum.getName());
        return;
      }
      lastSeen = concretePosition;
    }
  }

  /**
   * Validates every reference association against the concrete associations reachable by a mapping.
   *
   * <p>The association checks cover direction ambiguity, cardinality compatibility, and generated
   * role-field conflicts.
   */
  private void validateAssociations() {
    for (String mapping : mappings) {
      IncarnationContext context = contexts.get(mapping);
      if (context == null) {
        continue;
      }
      for (ASTCDAssociation referenceAssociation : referenceIndex.associations()) {
        validateAssociation(mapping, context, referenceAssociation);
      }
    }
  }

  /**
   * Matches one reference association to compatible concrete associations for a mapping.
   *
   * <p>A concrete association may match in the same direction or the reverse direction, depending
   * on which concrete types incarnate the reference endpoints. If both directions match at once,
   * the association is ambiguous and cannot be adapted deterministically.
   */
  private void validateAssociation(
      String mapping, IncarnationContext context, ASTCDAssociation referenceAssociation) {
    String refLeft = simpleName(referenceAssociation.getLeftQualifiedName().getQName());
    String refRight = simpleName(referenceAssociation.getRightQualifiedName().getQName());
    Set<String> leftTargets = concreteTypeNamesFor(context, refLeft);
    Set<String> rightTargets = concreteTypeNamesFor(context, refRight);
    if (leftTargets.isEmpty() || rightTargets.isEmpty()) {
      return;
    }
    for (ASTCDAssociation concreteAssociation : concreteIndex.associations()) {
      String conLeft = simpleName(concreteAssociation.getLeftQualifiedName().getQName());
      String conRight = simpleName(concreteAssociation.getRightQualifiedName().getQName());
      boolean same = leftTargets.contains(conLeft) && rightTargets.contains(conRight);
      boolean reverse = leftTargets.contains(conRight) && rightTargets.contains(conLeft);
      if (same && reverse) {
        conflict(
            mapping,
            "ambiguous association direction",
            associationName(concreteAssociation) + " can match "
                + associationName(referenceAssociation)
                + " in both directions");
      } else if (same) {
        validateAssociationSides(
            mapping,
            referenceAssociation.getLeft(),
            concreteAssociation.getLeft(),
            referenceAssociation.getRight(),
            concreteAssociation.getRight(),
            referenceAssociation,
            concreteAssociation);
      } else if (reverse) {
        validateAssociationSides(
            mapping,
            referenceAssociation.getLeft(),
            concreteAssociation.getRight(),
            referenceAssociation.getRight(),
            concreteAssociation.getLeft(),
            referenceAssociation,
            concreteAssociation);
      }
    }
    validateAssociationRoleConflicts(mapping, referenceAssociation, leftTargets, rightTargets);
  }

  /** Compares the matched association sides after direction has been resolved. */
  private void validateAssociationSides(
      String mapping,
      ASTCDAssocSide referenceLeft,
      ASTCDAssocSide concreteLeft,
      ASTCDAssocSide referenceRight,
      ASTCDAssocSide concreteRight,
      ASTCDAssociation referenceAssociation,
      ASTCDAssociation concreteAssociation) {
    validateCardinality(mapping, referenceLeft, concreteLeft, referenceAssociation, concreteAssociation);
    validateCardinality(mapping, referenceRight, concreteRight, referenceAssociation, concreteAssociation);
  }

  /**
   * Reports a cardinality conflict when both sides declare cardinalities and they differ.
   *
   * <p>Missing cardinalities are treated as unspecified and are therefore not rejected here.
   */
  private void validateCardinality(
      String mapping,
      ASTCDAssocSide referenceSide,
      ASTCDAssocSide concreteSide,
      ASTCDAssociation referenceAssociation,
      ASTCDAssociation concreteAssociation) {
    if (referenceSide.isPresentCDCardinality()
        && concreteSide.isPresentCDCardinality()
        && !referenceSide.getCDCardinality().deepEquals(concreteSide.getCDCardinality())) {
      conflict(
          mapping,
          "association cardinality conflict",
          associationName(concreteAssociation) + " has a cardinality incompatible with "
              + associationName(referenceAssociation));
    }
  }

  /**
   * Detects when an association role would generate a duplicate Java field on the same concrete
   * owner type.
   *
   * <p>Association roles are interpreted from the perspective of generated Java code: a role on
   * one association side becomes a field on the opposite type. For example, {@code A -> (bs) B}
   * contributes a field named {@code bs} to {@code A}. Symmetrically, {@code (as) A <- B}
   * contributes {@code as} to {@code B}.
   *
   * <p>A conflict is reported only if the reference role, after applying the current incarnation
   * mapping, could land on a concrete owner type that already receives the same role-derived field
   * from an unrelated concrete association. It is not enough that the same concrete type appears in
   * another association, or even that the same role name exists elsewhere. The duplicate must have
   * the same generated field owner.
   */
  private void validateAssociationRoleConflicts(
      String mapping,
      ASTCDAssociation referenceAssociation,
      Set<String> leftTargets,
      Set<String> rightTargets) {
    for (RoleFieldCandidate referenceRole : roleFieldCandidates(referenceAssociation)) {
      Set<String> ownerTargets =
          referenceRole.owner().equals(simpleName(referenceAssociation.getLeftQualifiedName().getQName()))
              ? leftTargets
              : rightTargets;
      Set<String> targetTargets =
          referenceRole.owner().equals(simpleName(referenceAssociation.getLeftQualifiedName().getQName()))
              ? rightTargets
              : leftTargets;
      if (ownerTargets.isEmpty()) {
        continue;
      }
      for (ASTCDAssociation other : concreteIndex.associations()) {
        for (RoleFieldCandidate concreteRole : roleFieldCandidates(other)) {
          if (!referenceRole.role().equals(concreteRole.role())) {
            continue;
          }
          if (ownerTargets.contains(concreteRole.owner())
              && targetTargets.contains(concreteRole.target())) {
            continue;
          }
          if (ownerTargets.contains(concreteRole.owner())) {
            conflict(
                mapping,
                "association role field conflict",
                "role '" + referenceRole.role() + "' may create a duplicate Java field on "
                    + concreteRole.owner());
          }
        }
      }
    }
  }

  /**
   * Converts CD association roles to generated Java field candidates.
   *
   * <p>The {@code owner} is the type that would contain the generated field, {@code role} is the
   * field name, and {@code target} is the field type or collection element type. The owner is the
   * opposite association side, because a navigable role describes how one side references the
   * other.
   */
  private List<RoleFieldCandidate> roleFieldCandidates(ASTCDAssociation association) {
    List<RoleFieldCandidate> result = new ArrayList<>();
    String left = simpleName(association.getLeftQualifiedName().getQName());
    String right = simpleName(association.getRightQualifiedName().getQName());
    if (association.getLeft().isPresentCDRole()) {
      result.add(new RoleFieldCandidate(right, association.getLeft().getCDRole().getName(), left));
    }
    if (association.getRight().isPresentCDRole()) {
      result.add(new RoleFieldCandidate(left, association.getRight().getCDRole().getName(), right));
    }
    return result;
  }

  /**
   * Detects reference members whose type is {@code any} without a concrete incarnation.
   *
   * <p>The placeholder {@code any} is only safe when the mapping tells us which concrete field or
   * method should provide the actual Java type. Otherwise generated code would have to guess.
   */
  private void validateUnderspecifiedTypes() {
    for (String mapping : mappings) {
      IncarnationContext context = contexts.get(mapping);
      if (context == null) {
        continue;
      }
      for (ASTCDType referenceType : referenceTypes.values()) {
        boolean ownerMapped =
            !context.getIncarnations(StableElementKey.type(referenceType.getName())).isEmpty();
        if (!ownerMapped) {
          continue;
        }
        for (ASTCDAttribute attribute : referenceType.getCDAttributeList()) {
          if (isAny(JavaLoader.print(attribute.getMCType()))
              && context.getIncarnations(StableElementKey.field(referenceType, attribute)).isEmpty()) {
            conflict(
                mapping,
                "underspecified attribute type",
                referenceType.getName() + "." + attribute.getName()
                    + " uses any without a concrete incarnation");
          }
        }
        for (ASTCDMethod method : referenceType.getCDMethodList()) {
          boolean methodMapped =
              !context.getIncarnations(StableElementKey.method(referenceType, method)).isEmpty();
          if (isAny(JavaLoader.print(method.getMCReturnType())) && !methodMapped) {
            conflict(
                mapping,
                "underspecified method return type",
                referenceType.getName() + "." + method.getName()
                    + " returns any without a concrete incarnation");
          }
          for (ASTCDParameter parameter : method.getCDParameterList()) {
            if (isAny(JavaLoader.print(parameter.getMCType())) && !methodMapped) {
              conflict(
                  mapping,
                  "underspecified method parameter type",
                  referenceType.getName() + "." + method.getName()
                      + " has parameter "
                      + parameter.getName()
                      + " of type any");
            }
          }
        }
      }
    }
  }

  /**
   * Detects stereotypes that name an overloaded reference method without a signature.
   *
   * <p>A stereotype like {@code ref="doIt"} is ambiguous when the reference type contains multiple
   * {@code doIt} overloads. In that case the concrete method must point to a signature-bearing
   * reference key instead of only the method name.
   */
  private void validateAmbiguousStereotypes() {
    for (String mapping : mappings) {
      for (ASTCDType concreteType : concreteTypes.values()) {
        Optional<String> referenceTypeName = getStereotypeValue(concreteType, mapping);
        if (referenceTypeName.isEmpty() && confParams.contains(CDConfParameter.NAME_MAPPING)) {
          referenceTypeName = Optional.of(concreteType.getName());
        }
        ASTCDType referenceType = referenceTypes.get(referenceTypeName.map(this::simpleName).orElse(""));
        if (referenceType == null) {
          continue;
        }
        Map<String, Long> methodsByName = new HashMap<>();
        for (ASTCDMethod method : referenceType.getCDMethodList()) {
          methodsByName.merge(method.getName(), 1L, Long::sum);
        }
        for (ASTCDMethod concreteMethod : concreteType.getCDMethodList()) {
          Optional<String> stereotype = getStereotypeValue(concreteMethod, mapping);
          if (stereotype.isPresent()
              && !stereotype.get().contains("(")
              && methodsByName.getOrDefault(simpleName(stereotype.get()), 0L) > 1) {
            conflict(
                mapping,
                "ambiguous overloaded method stereotype",
                concreteType.getName() + "." + concreteMethod.getName()
                    + " maps to overloaded reference method '"
                    + stereotype.get()
                    + "'");
          }
        }
      }
    }
  }

  /**
   * Validates reference elements that declare a {@code forEach} stereotype.
   *
   * <p>A {@code forEach} declaration promises that the context builder can derive one or more
   * concrete incarnations. If none are present, the adaptation would silently drop generated output.
   */
  private void validateForEachMappings() {
    for (String mapping : mappings) {
      IncarnationContext context = contexts.get(mapping);
      if (context == null) {
        continue;
      }
      for (ASTCDType referenceType : referenceTypes.values()) {
        validateForEachMapping(mapping, context, StableElementKey.type(referenceType), referenceType);
        for (ASTCDAttribute attribute : referenceType.getCDAttributeList()) {
          validateForEachMapping(
              mapping, context, StableElementKey.field(referenceType, attribute), attribute);
        }
        for (ASTCDMethod method : referenceType.getCDMethodList()) {
          validateForEachMapping(mapping, context, StableElementKey.method(referenceType, method), method);
        }
      }
    }
  }

  /** Reports a missing {@code forEach} incarnation for one reference type, field, or method. */
  private void validateForEachMapping(
      String mapping, IncarnationContext context, StableElementKey key, Object element) {
    Optional<String> forEachValue = getForEachValue(element);
    if (forEachValue.isEmpty()) {
      return;
    }
    if (context.getIncarnations(key).isEmpty()) {
      conflict(
          mapping,
          "missing forEach incarnation",
          key.signature() + " declares forEach='" + forEachValue.get()
              + "' but no concrete incarnation could be derived");
    }
  }

  private Set<String> concreteTypeNamesFor(IncarnationContext context, String referenceTypeName) {
    Set<String> result = new LinkedHashSet<>();
    for (var incarnation : context.getIncarnations(StableElementKey.type(referenceTypeName))) {
      result.add(incarnation.getKey().getName());
    }
    if (result.isEmpty() && concreteTypes.containsKey(referenceTypeName)) {
      result.add(referenceTypeName);
    }
    return result;
  }

  private Set<String> concreteParentNames(ASTCDType concrete) {
    Set<String> parents = new LinkedHashSet<>();
    CDTypeRelations.firstSuperclassName(concrete).map(this::simpleName).ifPresent(parents::add);
    for (String interfaceName : CDTypeRelations.interfaceNames(concrete)) {
      parents.add(simpleName(interfaceName));
    }
    parents.removeIf(parent -> !concreteIndex.hasType(parent));
    return parents;
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

  private Optional<String> getForEachValue(Object element) {
    if (element instanceof ASTCDType type) {
      return getStereotypeValue(type, "forEach");
    }
    if (element instanceof ASTCDAttribute attribute) {
      return getStereotypeValue(attribute, "forEach");
    }
    if (element instanceof ASTCDMethod method) {
      return getStereotypeValue(method, "forEach");
    }
    return Optional.empty();
  }

  private void conflict(String mapping, String kind, String detail) {
    conflicts.add("[" + mapping + "] " + kind + ": " + detail + "." + " Run with useConcretization=true or make the manual mapping explicit.");
  }

  private String typeKind(ASTCDType type) {
    if (type instanceof ASTCDClass) {
      return "class";
    }
    if (type instanceof ASTCDInterface) {
      return "interface";
    }
    if (type instanceof ASTCDEnum) {
      return "enum";
    }
    return type.getClass().getSimpleName();
  }

  private String printType(ASTCDAttribute attribute) {
    return JavaSourceNames.printNormalizedFieldType(attribute);
  }

  private String methodSignature(ASTCDMethod method) {
    return JavaSourceNames.methodSignature(method);
  }

  private String returnType(ASTCDMethod method) {
    return JavaSourceNames.printNormalizedReturnType(method);
  }

  private boolean isAny(String type) {
    return "any".equals(type == null ? "" : type.trim());
  }

  private String associationName(ASTCDAssociation association) {
    if (association.isPresentName()) {
      return association.getName();
    }
    return association.getLeftQualifiedName().getQName()
        + "--"
        + association.getRightQualifiedName().getQName();
  }

  private String simpleName(String name) {
    return JavaSourceNames.simpleName(name);
  }

  /**
   * A Java field implied by an association role.
   *
   * @param owner the type that would receive the generated field
   * @param role the generated field name
   * @param target the referenced type behind the field
   */
  private record RoleFieldCandidate(String owner, String role, String target) {}
}
