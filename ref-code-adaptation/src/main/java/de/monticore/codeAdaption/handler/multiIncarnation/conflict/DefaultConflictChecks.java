package de.monticore.codeAdaption.handler.multiIncarnation.conflict;

import java.util.List;

/** Registry for the conflict checks. */
public final class DefaultConflictChecks {

  private DefaultConflictChecks() {}

  public static List<AdaptationConflictCheck> all() {
    return List.of(
        new ManualMappingConflictCheck(),
        new ConcreteTypeStructureConflictCheck(),
        new ConcreteMemberConflictCheck(),
        new EnumConflictCheck(),
        new AssociationConflictCheck(),
        new UnderspecifiedTypeConflictCheck(),
        new AmbiguousStereotypeConflictCheck(),
        new ForEachConflictCheck());
  }
}
