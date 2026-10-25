package de.monticore.codeAdaption.handler.multiIncarnation.conflict;

import de.monticore.cd4codebasis._ast.ASTCDMethod;
import de.monticore.cdbasis._ast.ASTCDAttribute;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.codeAdaption.utils.CDTypeRelations;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

final class ConcreteMemberConflictCheck implements AdaptationConflictCheck {

  @Override
  public void check(ConflictDetectionContext context, ConflictCollector conflicts) {
    for (ASTCDType type : context.concreteTypes().values()) {
      Map<String, String> fields = new LinkedHashMap<>();
      for (ASTCDAttribute field : type.getCDAttributeList()) {
        String previous = fields.putIfAbsent(field.getName(), context.printType(field));
        if (previous != null && !previous.equals(context.printType(field))) {
          conflicts.conflict(
              "structure",
              "duplicate field",
              type.getName() + "." + field.getName() + " has incompatible types");
        }
      }
      validateInheritedFieldConflicts(context, conflicts, type, fields);

      Map<String, String> methods = new LinkedHashMap<>();
      for (ASTCDMethod method : type.getCDMethodList()) {
        String previous = methods.putIfAbsent(context.methodSignature(method), context.returnType(method));
        if (previous != null && !previous.equals(context.returnType(method))) {
          conflicts.conflict(
              "structure",
              "duplicate method",
              type.getName() + "." + context.methodSignature(method) + " has incompatible return types");
        }
      }
    }
  }

  private void validateInheritedFieldConflicts(
      ConflictDetectionContext context,
      ConflictCollector conflicts,
      ASTCDType type,
      Map<String, String> ownFields) {
    ArrayDeque<String> queue = new ArrayDeque<>();
    CDTypeRelations.firstSuperclassName(type).map(context::simpleName).ifPresent(queue::add);
    for (String interfaceName : CDTypeRelations.interfaceNames(type)) {
      queue.add(context.simpleName(interfaceName));
    }
    Set<String> visited = new HashSet<>();
    while (!queue.isEmpty()) {
      ASTCDType parent = context.concreteTypes().get(queue.removeFirst());
      if (parent == null || !visited.add(parent.getName())) {
        continue;
      }
      for (ASTCDAttribute inherited : parent.getCDAttributeList()) {
        String ownType = ownFields.get(inherited.getName());
        if (ownType != null && !ownType.equals(context.printType(inherited))) {
          conflicts.conflict(
              "structure",
              "inherited field conflict",
              type.getName() + "." + inherited.getName()
                  + " conflicts with inherited "
                  + parent.getName()
                  + "."
                  + inherited.getName());
        }
      }
      CDTypeRelations.firstSuperclassName(parent).map(context::simpleName).ifPresent(queue::add);
      for (String interfaceName : CDTypeRelations.interfaceNames(parent)) {
        queue.add(context.simpleName(interfaceName));
      }
    }
  }
}
