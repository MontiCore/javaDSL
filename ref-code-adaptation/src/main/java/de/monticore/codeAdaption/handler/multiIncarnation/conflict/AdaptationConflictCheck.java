package de.monticore.codeAdaption.handler.multiIncarnation.conflict;

/** Interface for manual adaptation conflict checks. */
public interface AdaptationConflictCheck {

  void check(ConflictDetectionContext context, ConflictCollector conflicts);
}
