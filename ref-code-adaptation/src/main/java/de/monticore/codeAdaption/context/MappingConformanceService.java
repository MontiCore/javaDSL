package de.monticore.codeAdaption.context;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdconformance.CDConfParameter;
import de.monticore.cdconformance.CDConformanceChecker;
import de.se_rwth.commons.logging.Log;
import java.util.List;
import java.util.Set;

/** Creates and runs conformance checkers with the adapter's fail-quick policy. */
public final class MappingConformanceService {

  private final Set<CDConfParameter> confParams;

  public MappingConformanceService(Set<CDConfParameter> confParams) {
    this.confParams = Set.copyOf(confParams);
  }

  public CDConformanceChecker newChecker() {
    return new CDConformanceChecker(confParams);
  }

  public boolean checkOrFalse(
      CDConformanceChecker checker,
      ASTCDCompilationUnit conCD,
      ASTCDCompilationUnit refCD,
      String mapping) {
    boolean valid = false;
    Throwable failure = null;
    List<String> diagnostics;
    synchronized (Log.class) {
      boolean failQuickEnabled = Log.isFailQuickEnabled();
      int findingsBefore = Log.getFindings().size();
      try {
        Log.enableFailQuick(false);
        valid = checker.checkConformance(conCD, refCD, mapping);
      } catch (RuntimeException | AssertionError throwable) {
        failure = throwable;
      } finally {
        diagnostics =
            Log.getFindings().subList(findingsBefore, Log.getFindings().size()).stream()
                .map(finding -> finding.getMsg())
                .distinct()
                .toList();
        // The guarded dependency call is represented by the boolean result and diagnostics below.
        // Do not leak its findings into the caller's process-global logging state.
        Log.getFindings().subList(findingsBefore, Log.getFindings().size()).clear();
        Log.enableFailQuick(failQuickEnabled);
      }
    }
    if (failure != null) {
      Log.warn(
          "Conformance checker threw during check for mapping '"
              + mapping
              + "': "
              + failure.getMessage()
              + " - will use validated stereotype-based fallback");
    }
    diagnostics.forEach(
        diagnostic ->
            Log.warn(
                "Conformance check for mapping '" + mapping + "': " + diagnostic));
    return valid;
  }
}
