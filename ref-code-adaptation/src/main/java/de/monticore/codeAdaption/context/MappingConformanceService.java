package de.monticore.codeAdaption.context;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdconformance.CDConfParameter;
import de.monticore.cdconformance.CDConformanceChecker;
import de.se_rwth.commons.logging.Log;
import java.util.Set;

/** Creates and runs conformance checkers with the adapter's fail-quick policy. */
public final class MappingConformanceService {

  private final Set<CDConfParameter> confParams;

  public MappingConformanceService(Set<CDConfParameter> confParams) {
    this.confParams = confParams;
  }

  public CDConformanceChecker newChecker() {
    return new CDConformanceChecker(confParams);
  }

  public boolean checkOrFalse(
      CDConformanceChecker checker,
      ASTCDCompilationUnit conCD,
      ASTCDCompilationUnit refCD,
      String mapping) {
    boolean failQuickEnabled = Log.isFailQuickEnabled();
    boolean mappingValid = false;
    try {
      Log.enableFailQuick(false);
      mappingValid = checker.checkConformance(conCD, refCD, mapping);
      return mappingValid;
    } catch (Throwable throwable) {
      Log.warn(
          "Conformance checker threw during check for mapping '"
              + mapping
              + "': "
              + throwable.getMessage()
              + " - will use stereotype-based fallback");
      return false;
    } finally {
      if (mappingValid) {
        Log.enableFailQuick(failQuickEnabled);
      }
    }
  }
}
