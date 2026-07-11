package de.monticore.codeAdaption.handler.multiIncarnation.conflict;

import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.cdinterfaceandenum._ast.ASTCDEnum;
import de.monticore.codeAdaption.handler.multiIncarnation.IncarnationContext;
import de.monticore.codeAdaption.handler.multiIncarnation.StableElementKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class EnumConflictCheck implements AdaptationConflictCheck {

  @Override
  public void check(ConflictDetectionContext context, ConflictCollector conflicts) {
    for (IncarnationContext incarnationContext : context.contexts().values()) {
      for (Map.Entry<StableElementKey, List<IncarnationContext.MappedElement>> entry :
          incarnationContext.getMappings().entrySet()) {
        if (entry.getKey().getKind() != StableElementKey.Kind.TYPE) {
          continue;
        }
        ASTCDType reference = context.referenceTypes().get(entry.getKey().getName());
        if (!(reference instanceof ASTCDEnum referenceEnum)) {
          continue;
        }
        for (IncarnationContext.MappedElement target : entry.getValue()) {
          ASTCDType concrete = context.concreteTypes().get(target.key().getName());
          if (concrete instanceof ASTCDEnum concreteEnum) {
            validateEnumOrder(context, conflicts, incarnationContext.getMappingName(), referenceEnum, concreteEnum);
          }
        }
      }
    }
  }

  private void validateEnumOrder(
      ConflictDetectionContext context,
      ConflictCollector conflicts,
      String mapping,
      ASTCDEnum referenceEnum,
      ASTCDEnum concreteEnum) {
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
        conflicts.conflict(
            mapping,
            "enum order conflict",
            concreteEnum.getName() + " orders reference constants differently from "
                + referenceEnum.getName());
        return;
      }
      lastSeen = concretePosition;
    }
  }
}
