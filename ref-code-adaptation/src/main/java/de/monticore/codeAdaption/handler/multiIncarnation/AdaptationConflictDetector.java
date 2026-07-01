package de.monticore.codeAdaption.handler.multiIncarnation;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdconformance.CDConfParameter;
import de.monticore.codeAdaption.CodeAdaptationException;
import de.monticore.codeAdaption.handler.multiIncarnation.conflict.AdaptationConflictCheck;
import de.monticore.codeAdaption.handler.multiIncarnation.conflict.ConflictCollector;
import de.monticore.codeAdaption.handler.multiIncarnation.conflict.ConflictDetectionContext;
import de.monticore.codeAdaption.handler.multiIncarnation.conflict.DefaultConflictChecks;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Runs manual adaptation conflict checks before Java output is cleaned or generated.
 *
 * <p>The detector is an orchestration facade. Built-in checks are behind
 * {@link AdaptationConflictCheck} strategies, and callers can provide additional strategies .
 */
public class AdaptationConflictDetector {

  private final ConflictDetectionContext context;
  private final Collection<? extends AdaptationConflictCheck> additionalChecks;

  public AdaptationConflictDetector(
      ASTCDCompilationUnit referenceCD,
      ASTCDCompilationUnit concreteCD,
      Set<String> mappings,
      Map<String, IncarnationContext> contexts,
      Set<CDConfParameter> confParams) {
    this(referenceCD, concreteCD, mappings, contexts, confParams, false);
  }

  public AdaptationConflictDetector(
      ASTCDCompilationUnit referenceCD,
      ASTCDCompilationUnit concreteCD,
      Set<String> mappings,
      Map<String, IncarnationContext> contexts,
      Set<CDConfParameter> confParams,
      boolean useCommonParentForMultipleIncarnations) {
    this(
        referenceCD,
        concreteCD,
        mappings,
        contexts,
        confParams,
        useCommonParentForMultipleIncarnations,
        List.of());
  }

  public AdaptationConflictDetector(
      ASTCDCompilationUnit referenceCD,
      ASTCDCompilationUnit concreteCD,
      Set<String> mappings,
      Map<String, IncarnationContext> contexts,
      Set<CDConfParameter> confParams,
      boolean useCommonParentForMultipleIncarnations,
      Collection<? extends AdaptationConflictCheck> additionalChecks) {
    this.context =
        new ConflictDetectionContext(
            referenceCD,
            concreteCD,
            mappings,
            contexts,
            confParams,
            useCommonParentForMultipleIncarnations);
    this.additionalChecks = additionalChecks == null ? List.of() : List.copyOf(additionalChecks);
  }

  /**
   * Validates manual, non-concretizing adaptation contexts and throws one combined
   * {@link CodeAdaptationException} when unresolved risks are found.
   */
  public static void validate(
      ASTCDCompilationUnit referenceCD,
      ASTCDCompilationUnit concreteCD,
      Set<String> mappings,
      Map<String, IncarnationContext> contexts,
      Set<CDConfParameter> confParams) {
    validate(referenceCD, concreteCD, mappings, contexts, confParams, false);
  }

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
    validate(
        referenceCD,
        concreteCD,
        mappings,
        contexts,
        confParams,
        useCommonParentForMultipleIncarnations,
        List.of());
  }

  /**
   * Validates manual adaptation conflicts with caller-provided checks appended after the built-in
   * checks.
   */
  public static void validate(
      ASTCDCompilationUnit referenceCD,
      ASTCDCompilationUnit concreteCD,
      Set<String> mappings,
      Map<String, IncarnationContext> contexts,
      Set<CDConfParameter> confParams,
      boolean useCommonParentForMultipleIncarnations,
      Collection<? extends AdaptationConflictCheck> additionalChecks) {
    AdaptationConflictDetector detector =
        new AdaptationConflictDetector(
            referenceCD,
            concreteCD,
            mappings,
            contexts,
            confParams,
            useCommonParentForMultipleIncarnations,
            additionalChecks);
    detector.detect();
  }

  private void detect() {
    ConflictCollector conflicts = new ConflictCollector();
    for (AdaptationConflictCheck check : DefaultConflictChecks.all()) {
      check.check(context, conflicts);
    }
    for (AdaptationConflictCheck check : additionalChecks) {
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
