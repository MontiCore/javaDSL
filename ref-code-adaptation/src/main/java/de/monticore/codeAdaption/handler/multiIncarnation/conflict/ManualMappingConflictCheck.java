package de.monticore.codeAdaption.handler.multiIncarnation.conflict;

import de.monticore.cdbasis._ast.ASTCDClass;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.cdinterfaceandenum._ast.ASTCDInterface;
import de.monticore.codeAdaption.handler.multiIncarnation.IncarnationContext;
import de.monticore.codeAdaption.handler.multiIncarnation.StableElementKey;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class ManualMappingConflictCheck implements AdaptationConflictCheck {

  @Override
  public void check(ConflictDetectionContext context, ConflictCollector conflicts) {
    for (IncarnationContext incarnationContext : context.contexts().values()) {
      validateExplicitStereotypes(context, conflicts, incarnationContext);
      for (Map.Entry<StableElementKey, List<IncarnationContext.MappedElement>> entry :
          incarnationContext.getMappings().entrySet()) {
        StableElementKey reference = entry.getKey();
        for (IncarnationContext.MappedElement mapped : entry.getValue()) {
          StableElementKey concrete = mapped.key();
          if (reference.getKind() == StableElementKey.Kind.TYPE) {
            validateTypeMapping(context, conflicts, incarnationContext, reference, concrete);
          }
        }
        if (reference.getKind() == StableElementKey.Kind.FIELD && entry.getValue().size() > 1) {
          validateFieldTargets(
              conflicts,
              incarnationContext.getMappingName(),
              reference,
              entry.getValue().stream().map(IncarnationContext.MappedElement::key).toList());
        }
        if (reference.getKind() == StableElementKey.Kind.METHOD && entry.getValue().size() > 1) {
          validateMethodTargets(
              conflicts,
              incarnationContext.getMappingName(),
              reference,
              entry.getValue().stream().map(IncarnationContext.MappedElement::key).toList());
        }
      }
    }
  }

  /**
   * Rejects active explicit stereotypes that the manual builder could not resolve.
   *
   * <p>The builder deliberately returns only valid stable-key relationships. Without this inverse
   * check, a misspelled reference target would silently disappear from the context and a
   * no-concrete-code run could still publish generated model shells without the intended adapted
   * implementation.
   */
  private void validateExplicitStereotypes(
      ConflictDetectionContext context,
      ConflictCollector conflicts,
      IncarnationContext incarnationContext) {
    String mapping = incarnationContext.getMappingName();
    for (ASTCDType concreteType : context.concreteIndex().types()) {
      context
          .stereotypeValue(concreteType, mapping)
          .filter(
              ignored ->
                  !hasConcreteTarget(
                      incarnationContext, StableElementKey.type(concreteType.getName())))
          .ifPresent(
              target ->
                  conflicts.conflict(
                      mapping,
                      "unresolved explicit type stereotype",
                      concreteType.getName() + " names unknown reference target '" + target + "'"));

      for (var concreteField : concreteType.getCDAttributeList()) {
        context
            .stereotypeValue(concreteField, mapping)
            .filter(
                ignored ->
                    !hasConcreteTarget(
                        incarnationContext,
                        StableElementKey.field(concreteType, concreteField)))
            .ifPresent(
                target ->
                    conflicts.conflict(
                        mapping,
                        "unresolved explicit field stereotype",
                        concreteType.getName()
                            + "."
                            + concreteField.getName()
                            + " names unknown or owner-incompatible reference target '"
                            + target
                            + "'"));
      }

      for (var concreteMethod : concreteType.getCDMethodList()) {
        context
            .stereotypeValue(concreteMethod, mapping)
            .filter(
                ignored ->
                    !hasConcreteTarget(
                        incarnationContext,
                        StableElementKey.method(concreteType, concreteMethod)))
            .ifPresent(
                target ->
                    conflicts.conflict(
                        mapping,
                        "unresolved explicit method stereotype",
                        concreteType.getName()
                            + "."
                            + concreteMethod.getName()
                            + " names unknown or owner-incompatible reference target '"
                            + target
                            + "'"));
      }
    }
  }

  private boolean hasConcreteTarget(
      IncarnationContext incarnationContext, StableElementKey concreteKey) {
    return incarnationContext.getMappings().values().stream()
        .flatMap(List::stream)
        .map(IncarnationContext.MappedElement::key)
        .anyMatch(concreteKey::equals);
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
    if (reference instanceof ASTCDClass && concrete instanceof ASTCDInterface) {
      long implementers =
          context.concreteIndex().classes().stream()
              .filter(type -> context.concreteIndex().isSubtypeOf(type.getName(), concrete.getName()))
              .count();
      return implementers > 1;
    }
    if (!(reference instanceof ASTCDInterface) || !(concrete instanceof ASTCDClass)) {
      return false;
    }
    return incarnationContext.getIncarnations(referenceKey).size() > 1
        && incarnationContext
            .getGroupingFor(StableElementKey.type(concrete.getName()))
            .isPresent();
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
