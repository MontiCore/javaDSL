package de.monticore.codeAdaption.handler.multiIncarnation.conflict;

import de.monticore.cd4codebasis._ast.ASTCDMethod;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.cdconformance.CDConfParameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

final class AmbiguousStereotypeConflictCheck implements AdaptationConflictCheck {

  @Override
  public void check(ConflictDetectionContext context, ConflictCollector conflicts) {
    for (String mapping : context.mappings()) {
      for (ASTCDType concreteType : context.concreteTypes().values()) {
        Optional<String> referenceTypeName = context.stereotypeValue(concreteType, mapping);
        if (referenceTypeName.isEmpty() && context.confParams().contains(CDConfParameter.NAME_MAPPING)) {
          referenceTypeName = Optional.of(concreteType.getName());
        }
        ASTCDType referenceType =
            context.referenceTypes().get(referenceTypeName.map(context::simpleName).orElse(""));
        if (referenceType == null) {
          continue;
        }
        Map<String, Long> methodsByName = new HashMap<>();
        for (ASTCDMethod method : referenceType.getCDMethodList()) {
          methodsByName.merge(method.getName(), 1L, Long::sum);
        }
        for (ASTCDMethod concreteMethod : concreteType.getCDMethodList()) {
          Optional<String> stereotype = context.stereotypeValue(concreteMethod, mapping);
          if (stereotype.isPresent()
              && !stereotype.get().contains("(")
              && methodsByName.getOrDefault(context.simpleName(stereotype.get()), 0L) > 1) {
            conflicts.conflict(
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
}
