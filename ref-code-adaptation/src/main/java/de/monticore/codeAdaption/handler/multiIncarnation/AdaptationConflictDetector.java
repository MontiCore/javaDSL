package de.monticore.codeAdaption.handler.multiIncarnation;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdconformance.CDConfParameter;
import de.monticore.codeAdaption.CodeAdaptationException;
import de.monticore.codeAdaption.handler.multiIncarnation.conflict.AdaptationConflictCheck;
import de.monticore.codeAdaption.handler.multiIncarnation.conflict.ConflictCollector;
import de.monticore.codeAdaption.handler.multiIncarnation.conflict.ConflictDetectionContext;
import de.monticore.codeAdaption.handler.multiIncarnation.conflict.DefaultConflictChecks;
import java.util.Map;
import java.util.Set;

/**
 * Runs manual adaptation conflict checks before Java output is cleaned or generated.
 *
 * <p>The detector is an orchestration facade over the built-in conflict-check strategies.
 */
public final class AdaptationConflictDetector {

  private AdaptationConflictDetector() {}

  /**
   * Validates manual adaptation conflicts before Java output is cleaned or generated.
   *
   * <p>The detector covers type-kind mismatches, inheritance issues, duplicate or incompatible
   * members, enum order mismatches, association ambiguities, underspecified {@code any} types, and
   * unsupported forEach mappings.
   */
  public static void validate(
      ASTCDCompilationUnit referenceCD,
      ASTCDCompilationUnit concreteCD,
      Set<String> mappings,
      Map<String, IncarnationContext> contexts,
      Set<CDConfParameter> confParams,
      boolean useCommonParentForMultipleIncarnations) {
    ConflictDetectionContext context =
        new ConflictDetectionContext(
            referenceCD,
            concreteCD,
            mappings,
            contexts,
            confParams,
            useCommonParentForMultipleIncarnations);
    ConflictCollector conflicts = new ConflictCollector();
    for (AdaptationConflictCheck check : DefaultConflictChecks.all()) {
      check.check(context, conflicts);
    }

    if (conflicts.hasConflicts()) {
      throw new CodeAdaptationException(
          "Manual code adaptation detected conflicts before output generation:"
              + System.lineSeparator()
              + "- "
              + String.join(System.lineSeparator() + "- ", conflicts.formattedConflicts()));
    }
  }
}
