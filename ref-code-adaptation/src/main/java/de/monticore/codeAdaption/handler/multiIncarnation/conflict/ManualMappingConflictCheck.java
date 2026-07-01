package de.monticore.codeAdaption.handler.multiIncarnation.conflict;

import de.monticore.cdbasis._ast.ASTCDClass;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.cdinterfaceandenum._ast.ASTCDInterface;
import de.monticore.codeAdaption.handler.multiIncarnation.IncarnationContext;
import de.monticore.codeAdaption.handler.multiIncarnation.StableElementKey;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class ManualMappingConflictCheck implements AdaptationConflictCheck {

  @Override
  public void check(ConflictDetectionContext context, ConflictCollector conflicts) {
    for (IncarnationContext incarnationContext : context.contexts().values()) {
      for (Map.Entry<StableElementKey, List<StableElementKey>> entry :
          incarnationContext.getStableMappings().entrySet()) {
        StableElementKey reference = entry.getKey();
        for (StableElementKey concrete : entry.getValue()) {
          if (reference.getKind() == StableElementKey.Kind.TYPE) {
            validateTypeMapping(context, conflicts, incarnationContext, reference, concrete);
          }
        }
        if (reference.getKind() == StableElementKey.Kind.FIELD && entry.getValue().size() > 1) {
          validateFieldTargets(conflicts, incarnationContext.getMappingName(), reference, entry.getValue());
        }
        if (reference.getKind() == StableElementKey.Kind.METHOD && entry.getValue().size() > 1) {
          validateMethodTargets(conflicts, incarnationContext.getMappingName(), reference, entry.getValue());
        }
      }
    }
  }

  private void validateTypeMapping(
      ConflictDetectionContext context,
      ConflictCollector conflicts,
      IncarnationContext incarnationContext,
      StableElementKey referenceKey,
      StableElementKey concreteKey) {
    ASTCDType reference = context.referenceTypes().get(referenceKey.getName());
    ASTCDType concrete = context.concreteTypes().get(concreteKey.getName());
    if (reference == null || concrete == null) {
      return;
    }
    if (!context.typeKind(reference).equals(context.typeKind(concrete))) {
      if (canUseCommonParentForTypeMismatch(context, incarnationContext, referenceKey, reference, concrete)) {
        return;
      }
      conflicts.conflict(
          incarnationContext.getMappingName(),
          "type-kind mismatch",
          reference.getName() + " is a " + context.typeKind(reference) + " but "
              + concrete.getName() + " is a " + context.typeKind(concrete));
    }
  }

  private boolean canUseCommonParentForTypeMismatch(
      ConflictDetectionContext context,
      IncarnationContext incarnationContext,
      StableElementKey referenceKey,
      ASTCDType reference,
      ASTCDType concrete) {
    if (!context.useCommonParentForMultipleIncarnations()) {
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
    if (incarnationContext.findGroupingTypeForImplementer(concrete.getName()).isPresent()) {
      return true;
    }
    Set<String> mappedConcreteTypes = new LinkedHashSet<>();
    for (var incarnation : incarnationContext.getIncarnations(referenceKey)) {
      mappedConcreteTypes.add(incarnation.getKey().getName());
    }
    for (String parentName : context.concreteParentNames(concrete)) {
      if (mappedConcreteTypes.contains(parentName)) {
        return true;
      }
    }
    return !context.concreteParentNames(concrete).isEmpty();
  }

  private void validateFieldTargets(
      ConflictCollector conflicts,
      String mapping,
      StableElementKey reference,
      List<StableElementKey> targets) {
    Map<String, String> byOwnerAndName = new LinkedHashMap<>();
    for (StableElementKey target : targets) {
      String key = target.getOwnerType().orElse("") + "." + target.getName();
      String type = target.getFieldKind().orElse("Object");
      String previous = byOwnerAndName.putIfAbsent(key, type);
      if (previous != null && !previous.equals(type)) {
        conflicts.conflict(
            mapping,
            "field target conflict",
            reference.signature() + " maps to incompatible concrete fields " + targets);
      }
    }
  }

  private void validateMethodTargets(
      ConflictCollector conflicts,
      String mapping,
      StableElementKey reference,
      List<StableElementKey> targets) {
    Map<String, String> bySignature = new LinkedHashMap<>();
    for (StableElementKey target : targets) {
      String signature = target.signature();
      String returnType = target.getReturnType().orElse("void");
      String previous = bySignature.putIfAbsent(signature, returnType);
      if (previous != null && !previous.equals(returnType)) {
        conflicts.conflict(
            mapping,
            "method target conflict",
            reference.signature() + " maps to incompatible concrete methods " + targets);
      }
    }
  }
}
