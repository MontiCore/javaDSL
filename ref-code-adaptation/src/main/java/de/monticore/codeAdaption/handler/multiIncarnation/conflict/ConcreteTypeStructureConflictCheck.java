package de.monticore.codeAdaption.handler.multiIncarnation.conflict;

import de.monticore.cdbasis._ast.ASTCDClass;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.cdinterfaceandenum._ast.ASTCDInterface;
import de.monticore.codeAdaption.utils.CDTypeRelations;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

final class ConcreteTypeStructureConflictCheck implements AdaptationConflictCheck {

  @Override
  public void check(ConflictDetectionContext context, ConflictCollector conflicts) {
    for (ASTCDType type : context.concreteTypes().values()) {
      if (type instanceof ASTCDClass cdClass && cdClass.getSuperclassList().size() > 1) {
        conflicts.conflict(
            "structure",
            "illegal multiple class supertypes",
            type.getName() + " extends more than one class");
      }
      validateSuperTypeKinds(context, conflicts, type);
    }
    validateInheritanceCycles(context, conflicts);
  }

  private void validateSuperTypeKinds(
      ConflictDetectionContext context, ConflictCollector conflicts, ASTCDType type) {
    if (type instanceof ASTCDClass) {
      CDTypeRelations.firstSuperclassName(type)
          .map(context::simpleName)
          .map(context.concreteTypes()::get)
          .filter(ASTCDInterface.class::isInstance)
          .ifPresent(
              superType ->
                  conflicts.conflict(
                      "structure",
                      "class extends interface",
                      type.getName() + " extends interface " + superType.getName()));
      for (String interfaceName : CDTypeRelations.interfaceNames(type)) {
        ASTCDType implemented = context.concreteTypes().get(context.simpleName(interfaceName));
        if (implemented != null && !(implemented instanceof ASTCDInterface)) {
          conflicts.conflict(
              "structure",
              "class implements non-interface",
              type.getName() + " implements " + implemented.getName());
        }
      }
    } else if (type instanceof ASTCDInterface) {
      for (String interfaceName : CDTypeRelations.interfaceNames(type)) {
        ASTCDType extended = context.concreteTypes().get(context.simpleName(interfaceName));
        if (extended != null && !(extended instanceof ASTCDInterface)) {
          conflicts.conflict(
              "structure",
              "interface extends non-interface",
              type.getName() + " extends " + extended.getName());
        }
      }
    }
  }

  private void validateInheritanceCycles(ConflictDetectionContext context, ConflictCollector conflicts) {
    Map<String, Set<String>> edges = new LinkedHashMap<>();
    for (ASTCDType type : context.concreteTypes().values()) {
      Set<String> parents = new LinkedHashSet<>();
      CDTypeRelations.firstSuperclassName(type).map(context::simpleName).ifPresent(parents::add);
      for (String interfaceName : CDTypeRelations.interfaceNames(type)) {
        parents.add(context.simpleName(interfaceName));
      }
      parents.removeIf(parent -> !context.concreteTypes().containsKey(parent));
      edges.put(type.getName(), parents);
    }
    for (String typeName : edges.keySet()) {
      if (hasCycle(typeName, edges, new LinkedHashSet<>(), new HashSet<>())) {
        conflicts.conflict("structure", "inheritance cycle", "cycle reaches " + typeName);
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
}
