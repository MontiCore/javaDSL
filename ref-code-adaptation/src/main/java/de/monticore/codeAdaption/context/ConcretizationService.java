package de.monticore.codeAdaption.context;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdconcretization.ConcretizationCompleter;
import de.monticore.cdconformance.CDConfParameter;
import de.se_rwth.commons.logging.Log;
import java.util.ArrayList;
import java.util.Set;

/** Wraps cdconcretization setup and fail-quick handling for adaptation runs. */
public final class ConcretizationService {

  private final Set<CDConfParameter> confParams;

  public ConcretizationService(Set<CDConfParameter> confParams) {
    this.confParams = confParams;
  }

  public void completeConcreteCD(
      ASTCDCompilationUnit conCD, ASTCDCompilationUnit refCD, Set<String> mappings) {
    ConcretizationCompleter completer = new ConcretizationCompleter(confParams);
    boolean failQuickEnabled = Log.isFailQuickEnabled();
    boolean completed = false;
    try {
      Log.enableFailQuick(false);
      completer.completeCD(conCD, refCD, new ArrayList<>(mappings));
      completed = true;
      Log.info("Concretized concrete CD before code adaptation", "CodeAdapter");
    } catch (Throwable throwable) {
      Log.warn(
          "CD concretization failed before code adaptation: "
              + throwable.getMessage()
              + " - continuing with available conformance mappings");
    } finally {
      if (completed) {
        Log.enableFailQuick(failQuickEnabled);
      }
    }
  }
}
