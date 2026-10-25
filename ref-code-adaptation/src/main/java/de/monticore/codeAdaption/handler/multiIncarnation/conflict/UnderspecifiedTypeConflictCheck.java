package de.monticore.codeAdaption.handler.multiIncarnation.conflict;

import de.monticore.cd4codebasis._ast.ASTCDMethod;
import de.monticore.cd4codebasis._ast.ASTCDParameter;
import de.monticore.cdbasis._ast.ASTCDAttribute;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.codeAdaption.handler.multiIncarnation.IncarnationContext;
import de.monticore.codeAdaption.handler.multiIncarnation.StableElementKey;
import de.monticore.codeAdaption.utils.JavaLoader;

final class UnderspecifiedTypeConflictCheck implements AdaptationConflictCheck {

  @Override
  public void check(ConflictDetectionContext context, ConflictCollector conflicts) {
    for (String mapping : context.mappings()) {
      IncarnationContext incarnationContext = context.contexts().get(mapping);
      if (incarnationContext == null) {
        continue;
      }
      for (ASTCDType referenceType : context.referenceTypes().values()) {
        boolean ownerMapped =
            !incarnationContext.getIncarnations(StableElementKey.type(referenceType.getName())).isEmpty();
        if (!ownerMapped) {
          continue;
        }
        for (ASTCDAttribute attribute : referenceType.getCDAttributeList()) {
          if (context.isAny(JavaLoader.print(attribute.getMCType()))
              && incarnationContext
                  .getIncarnations(StableElementKey.field(referenceType, attribute))
                  .isEmpty()
              && !hasMappedForEachDerivative(
                  context, incarnationContext, referenceType, attribute)) {
            conflicts.conflict(
                mapping,
                "underspecified attribute type",
                referenceType.getName() + "." + attribute.getName()
                    + " uses any without a concrete incarnation");
          }
        }
        for (ASTCDMethod method : referenceType.getCDMethodList()) {
          boolean methodMapped =
              !incarnationContext.getIncarnations(StableElementKey.method(referenceType, method)).isEmpty();
          if (context.isAny(JavaLoader.print(method.getMCReturnType()))
              && !methodMapped
              && !hasMappedForEachDerivative(context, incarnationContext, referenceType, method)) {
            conflicts.conflict(
                mapping,
                "underspecified method return type",
                referenceType.getName() + "." + method.getName()
                    + " returns any without a concrete incarnation");
          }
          for (ASTCDParameter parameter : method.getCDParameterList()) {
            if (context.isAny(JavaLoader.print(parameter.getMCType())) && !methodMapped) {
              conflicts.conflict(
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

  private boolean hasMappedForEachDerivative(
      ConflictDetectionContext context,
      IncarnationContext incarnationContext,
      ASTCDType owner,
      ASTCDMethod target) {
    String qualifiedTarget = owner.getName() + "." + target.getName();
    for (ASTCDAttribute attribute : owner.getCDAttributeList()) {
      if (context.forEachValue(attribute)
              .filter(value -> value.equals(target.getName()) || value.endsWith(qualifiedTarget))
              .isPresent()
          && !incarnationContext
              .getIncarnations(StableElementKey.field(owner, attribute))
              .isEmpty()) {
        return true;
      }
    }
    return false;
  }

  private boolean hasMappedForEachDerivative(
      ConflictDetectionContext context,
      IncarnationContext incarnationContext,
      ASTCDType owner,
      ASTCDAttribute target) {
    String qualifiedTarget = owner.getName() + "." + target.getName();
    for (ASTCDAttribute attribute : owner.getCDAttributeList()) {
      if (context.forEachValue(attribute)
              .filter(value -> value.equals(target.getName()) || value.equals(qualifiedTarget))
              .isPresent()
          && !incarnationContext
              .getIncarnations(StableElementKey.field(owner, attribute))
              .isEmpty()) {
        return true;
      }
    }
    return false;
  }
}
