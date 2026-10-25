package de.monticore.codeAdaption.handler.multiIncarnation.conflict;

import de.monticore.cdassociation._ast.ASTCDAssocSide;
import de.monticore.cdassociation._ast.ASTCDAssociation;
import de.monticore.codeAdaption.handler.multiIncarnation.IncarnationContext;
import java.util.Set;

final class AssociationConflictCheck implements AdaptationConflictCheck {

  @Override
  public void check(ConflictDetectionContext context, ConflictCollector conflicts) {
    for (String mapping : context.mappings()) {
      IncarnationContext incarnationContext = context.contexts().get(mapping);
      if (incarnationContext == null) {
        continue;
      }
      for (ASTCDAssociation referenceAssociation : context.referenceIndex().associations()) {
        validateAssociation(context, conflicts, mapping, incarnationContext, referenceAssociation);
      }
    }
  }

  private void validateAssociation(
      ConflictDetectionContext context,
      ConflictCollector conflicts,
      String mapping,
      IncarnationContext incarnationContext,
      ASTCDAssociation referenceAssociation) {
    String refLeft = context.simpleName(referenceAssociation.getLeftQualifiedName().getQName());
    String refRight = context.simpleName(referenceAssociation.getRightQualifiedName().getQName());
    Set<String> leftTargets = context.concreteTypeNamesFor(incarnationContext, refLeft);
    Set<String> rightTargets = context.concreteTypeNamesFor(incarnationContext, refRight);
    if (leftTargets.isEmpty() || rightTargets.isEmpty()) {
      return;
    }
    for (ASTCDAssociation concreteAssociation : context.concreteIndex().associations()) {
      String conLeft = context.simpleName(concreteAssociation.getLeftQualifiedName().getQName());
      String conRight = context.simpleName(concreteAssociation.getRightQualifiedName().getQName());
      boolean same = leftTargets.contains(conLeft) && rightTargets.contains(conRight);
      boolean reverse = leftTargets.contains(conRight) && rightTargets.contains(conLeft);
      if (same && reverse) {
        conflicts.conflict(
            mapping,
            "ambiguous association direction",
            context.associationName(concreteAssociation) + " can match "
                + context.associationName(referenceAssociation)
                + " in both directions");
      } else if (same) {
        validateAssociationSides(
            context,
            conflicts,
            mapping,
            referenceAssociation.getLeft(),
            concreteAssociation.getLeft(),
            referenceAssociation.getRight(),
            concreteAssociation.getRight(),
            referenceAssociation,
            concreteAssociation);
      } else if (reverse) {
        validateAssociationSides(
            context,
            conflicts,
            mapping,
            referenceAssociation.getLeft(),
            concreteAssociation.getRight(),
            referenceAssociation.getRight(),
            concreteAssociation.getLeft(),
            referenceAssociation,
            concreteAssociation);
      }
    }
    validateAssociationRoleConflicts(
        context, conflicts, mapping, referenceAssociation, leftTargets, rightTargets);
  }

  private void validateAssociationSides(
      ConflictDetectionContext context,
      ConflictCollector conflicts,
      String mapping,
      ASTCDAssocSide referenceLeft,
      ASTCDAssocSide concreteLeft,
      ASTCDAssocSide referenceRight,
      ASTCDAssocSide concreteRight,
      ASTCDAssociation referenceAssociation,
      ASTCDAssociation concreteAssociation) {
    validateCardinality(
        context, conflicts, mapping, referenceLeft, concreteLeft, referenceAssociation, concreteAssociation);
    validateCardinality(
        context, conflicts, mapping, referenceRight, concreteRight, referenceAssociation, concreteAssociation);
  }

  private void validateCardinality(
      ConflictDetectionContext context,
      ConflictCollector conflicts,
      String mapping,
      ASTCDAssocSide referenceSide,
      ASTCDAssocSide concreteSide,
      ASTCDAssociation referenceAssociation,
      ASTCDAssociation concreteAssociation) {
    if (referenceSide.isPresentCDCardinality()
        && concreteSide.isPresentCDCardinality()
        && !referenceSide.getCDCardinality().deepEquals(concreteSide.getCDCardinality())) {
      conflicts.conflict(
          mapping,
          "association cardinality conflict",
          context.associationName(concreteAssociation) + " has a cardinality incompatible with "
              + context.associationName(referenceAssociation));
    }
  }

  private void validateAssociationRoleConflicts(
      ConflictDetectionContext context,
      ConflictCollector conflicts,
      String mapping,
      ASTCDAssociation referenceAssociation,
      Set<String> leftTargets,
      Set<String> rightTargets) {
    for (ConflictDetectionContext.RoleFieldCandidate referenceRole :
        context.roleFieldCandidates(referenceAssociation)) {
      Set<String> ownerTargets =
          referenceRole.owner().equals(context.simpleName(referenceAssociation.getLeftQualifiedName().getQName()))
              ? leftTargets
              : rightTargets;
      Set<String> targetTargets =
          referenceRole.owner().equals(context.simpleName(referenceAssociation.getLeftQualifiedName().getQName()))
              ? rightTargets
              : leftTargets;
      if (ownerTargets.isEmpty()) {
        continue;
      }
      for (ASTCDAssociation other : context.concreteIndex().associations()) {
        for (ConflictDetectionContext.RoleFieldCandidate concreteRole :
            context.roleFieldCandidates(other)) {
          if (!referenceRole.role().equals(concreteRole.role())) {
            continue;
          }
          if (ownerTargets.contains(concreteRole.owner())
              && targetTargets.contains(concreteRole.target())) {
            continue;
          }
          if (ownerTargets.contains(concreteRole.owner())) {
            conflicts.conflict(
                mapping,
                "association role field conflict",
                "role '" + referenceRole.role() + "' may create a duplicate Java field on "
                    + concreteRole.owner());
          }
        }
      }
    }
  }
}
