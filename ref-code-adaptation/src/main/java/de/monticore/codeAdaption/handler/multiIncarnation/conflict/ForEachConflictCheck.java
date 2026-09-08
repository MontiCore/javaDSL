package de.monticore.codeAdaption.handler.multiIncarnation.conflict;

import de.monticore.cd4codebasis._ast.ASTCDMethod;
import de.monticore.cdbasis._ast.ASTCDAttribute;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.codeAdaption.handler.multiIncarnation.IncarnationContext;
import de.monticore.codeAdaption.handler.multiIncarnation.StableElementKey;
import java.util.Optional;

final class ForEachConflictCheck implements AdaptationConflictCheck {

  @Override
  public void check(ConflictDetectionContext context, ConflictCollector conflicts) {
    for (String mapping : context.mappings()) {
      IncarnationContext incarnationContext = context.contexts().get(mapping);
      if (incarnationContext == null) {
        continue;
      }
      for (ASTCDType referenceType : context.referenceTypes().values()) {
        validateForEachMapping(
            context, conflicts, mapping, incarnationContext, StableElementKey.type(referenceType), referenceType);
        for (ASTCDAttribute attribute : referenceType.getCDAttributeList()) {
          validateForEachMapping(
              context,
              conflicts,
              mapping,
              incarnationContext,
              StableElementKey.field(referenceType, attribute),
              attribute);
        }
        for (ASTCDMethod method : referenceType.getCDMethodList()) {
          validateForEachMapping(
              context,
              conflicts,
              mapping,
              incarnationContext,
              StableElementKey.method(referenceType, method),
              method);
        }
      }
    }
  }

  private void validateForEachMapping(
      ConflictDetectionContext context,
      ConflictCollector conflicts,
      String mapping,
      IncarnationContext incarnationContext,
      StableElementKey key,
      Object element) {
    Optional<String> forEachValue = context.forEachValue(element);
    if (forEachValue.isEmpty()) {
      return;
    }
    if (incarnationContext.getIncarnations(key).isEmpty()) {
      conflicts.conflict(
          mapping,
          "missing forEach incarnation",
          key.signature() + " declares forEach='" + forEachValue.get()
              + "' but no concrete incarnation could be derived");
    }
  }
}
