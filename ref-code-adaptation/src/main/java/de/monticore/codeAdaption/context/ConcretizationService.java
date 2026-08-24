package de.monticore.codeAdaption.context;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdconcretization.ConcretizationCompleter;
import de.monticore.cdconformance.CDConfParameter;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.se_rwth.commons.logging.Log;
import java.util.ArrayList;
import java.util.Set;

/** Wraps cdconcretization setup and fail-quick handling for adaptation runs. */
public final class ConcretizationService {

  private final Set<CDConfParameter> confParams;

  public ConcretizationService(Set<CDConfParameter> confParams) {
    this.confParams = Set.copyOf(confParams);
  }

  public ASTCDCompilationUnit completeConcreteCD(
      ASTCDCompilationUnit conCD, ASTCDCompilationUnit refCD, Set<String> mappings) {
    return completeConcreteCDInPlace(conCD.deepClone(), refCD, mappings);
  }

  /**
   * Completes a caller-owned working copy in place so its partial state remains available when
   * cdconcretization reports an error.
   */
  public ASTCDCompilationUnit completeConcreteCDInPlace(
      ASTCDCompilationUnit completedCD, ASTCDCompilationUnit refCD, Set<String> mappings) {
    ConcretizationCompleter completer = new ConcretizationCompleter(confParams);
    JavaLoader.initializeCDSymbolTable(completedCD);
    synchronized (Log.class) {
      boolean failQuickEnabled = Log.isFailQuickEnabled();
      int findingsBefore = Log.getFindings().size();
      long errorsBefore = Log.getErrorCount();
      try {
        Log.enableFailQuick(false);
        ArrayList<String> orderedMappings = new ArrayList<>(mappings);
        orderedMappings.sort(String::compareTo);
        completer.completeCD(completedCD, refCD, orderedMappings);
        // Completion mutates the cloned AST by adding and repairing model elements. Rebuild its
        // symbols before conformance/context construction so added fields, methods, types and
        // inheritance relationships are visible through the same completed snapshot.
        JavaLoader.initializeCDSymbolTable(completedCD);
        if (Log.getErrorCount() > errorsBefore) {
          String diagnostics =
              Log.getFindings().subList(findingsBefore, Log.getFindings().size()).stream()
                  .map(finding -> finding.getMsg())
                  .distinct()
                  .reduce((left, right) -> left + "; " + right)
                  .orElse("unknown concretization error");
          throw new IllegalStateException(
              "CD concretization reported errors; adaptation cannot continue safely: "
                  + diagnostics);
        }
        Log.info("Concretized concrete CD before code adaptation", "CodeAdapter");
        return completedCD;
      } catch (Exception | AssertionError throwable) {
        throw new IllegalStateException(
            "CD concretization failed before code adaptation; the input concrete CD was not modified",
            throwable);
      } finally {
        // Log.enableFailQuick(true) terminates the process when error findings exist. The errors
        // produced by this guarded dependency call are represented by the exception above, so
        // remove only those new findings before restoring the caller's global logging policy.
        if (Log.getErrorCount() > errorsBefore && Log.getFindings().size() > findingsBefore) {
          Log.getFindings().subList(findingsBefore, Log.getFindings().size()).clear();
        }
        Log.enableFailQuick(failQuickEnabled);
      }
    }
  }
}
