package de.monticore.codeAdaption.handler;

import de.monticore.cdassociation._ast.ASTCDAssociation;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.cdbasis._symboltable.CDTypeSymbol;
import de.monticore.codeAdaption.matcher.CodeMatching;
import de.monticore.codeAdaption.utils.JavaSourceNames;
import de.monticore.codeAdaption.utils.visitors.JavaAstElemCollector;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.symboltable.ISymbol;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Updates Java accesses to fields generated from navigable CD association roles. */
final class AssociationRoleUpdateService {

  private final BasicUpdateHandler handler;

  AssociationRoleUpdateService(BasicUpdateHandler handler) {
    this.handler = handler;
  }

  void handleAssociationRoleUpdate(JavaAstElemCollector collector) {
    for (ASTTypeDeclaration javaType : collector.getAllTypeDeclarations()) {
      Map<String, String> roleRewrites = new LinkedHashMap<>();
      for (ASTCDAssociation referenceAssociation : handler.refIndex.associations()) {
        for (AssociationRole referenceRole : associationRoles(referenceAssociation)) {
          if (!adaptsReferenceType(javaType, referenceRole.ownerType())) {
            continue;
          }

          String concreteOwner = resolveConcreteAssociationType(referenceRole.ownerType());
          String concreteTarget = resolveConcreteAssociationType(referenceRole.targetType());
          Set<String> concreteRoles = new LinkedHashSet<>();
          for (ASTCDAssociation concreteAssociation : handler.conIndex.associations()) {
            for (AssociationRole concreteRole : associationRoles(concreteAssociation)) {
              if (concreteOwner.equals(concreteRole.ownerType())
                  && concreteTarget.equals(concreteRole.targetType())) {
                concreteRoles.add(concreteRole.roleName());
              }
            }
          }

          if (concreteRoles.size() > 1) {
            if (concreteRoles.contains(referenceRole.roleName())) {
              roleRewrites.putIfAbsent(referenceRole.roleName(), referenceRole.roleName());
              continue;
            }
            throw new IllegalStateException(
                "Ambiguous concrete association roles for "
                    + referenceRole.ownerType()
                    + "."
                    + referenceRole.roleName()
                    + ": "
                    + concreteRoles);
          }
          if (concreteRoles.size() == 1) {
            String concreteRole = concreteRoles.iterator().next();
            String previous = roleRewrites.putIfAbsent(referenceRole.roleName(), concreteRole);
            if (previous != null && !previous.equals(concreteRole)) {
              throw new IllegalStateException(
                  "Conflicting concrete association roles for "
                      + referenceRole.ownerType()
                      + "."
                      + referenceRole.roleName());
            }
          }
        }
      }
      roleRewrites.forEach(
          (sourceRole, concreteRole) ->
              handler.updater.updateAssociationRole(javaType, sourceRole, concreteRole));
    }
  }

  private boolean adaptsReferenceType(ASTTypeDeclaration javaType, String referenceType) {
    Optional<CodeMatching> matching = handler.validator.getMatchedType(javaType);
    if (matching.isPresent()
        && matching.get().getReferences().stream()
            .filter(CDTypeSymbol.class::isInstance)
            .map(ISymbol::getName)
            .anyMatch(referenceType::equals)) {
      return true;
    }
    return referenceType.equals(javaType.getName());
  }

  private String resolveConcreteAssociationType(String referenceType) {
    Optional<ASTCDType> type = handler.refIndex.type(referenceType);
    if (type.isEmpty()) {
      return referenceType;
    }
    Optional<ISymbol> fromContext = handler.getSymbolFromContext(type.get().getSymbol());
    ISymbol concrete = fromContext.orElseGet(() -> handler.getConTypeSymbol(type.get().getSymbol()));
    return JavaSourceNames.simpleName(concrete.getName());
  }

  private List<AssociationRole> associationRoles(ASTCDAssociation association) {
    String left = JavaSourceNames.simpleName(association.getLeftQualifiedName().getQName());
    String right = JavaSourceNames.simpleName(association.getRightQualifiedName().getQName());
    List<AssociationRole> roles = new ArrayList<>();
    if (association.getLeft().isPresentCDRole()) {
      roles.add(new AssociationRole(right, left, association.getLeft().getCDRole().getName()));
    }
    if (association.getRight().isPresentCDRole()) {
      roles.add(new AssociationRole(left, right, association.getRight().getCDRole().getName()));
    }
    return roles;
  }

  private record AssociationRole(String ownerType, String targetType, String roleName) {}
}
