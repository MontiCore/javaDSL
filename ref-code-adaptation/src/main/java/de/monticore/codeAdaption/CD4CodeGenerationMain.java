package de.monticore.codeAdaption;

import de.monticore.CD4CodeTool;
import de.se_rwth.commons.logging.Finding;
import de.se_rwth.commons.logging.Log;
import java.util.List;

/** Isolated process entry point for concrete-code generation. */
public final class CD4CodeGenerationMain {

  private CD4CodeGenerationMain() {}

  public static void main(String[] arguments) {
    try {
      Log.enableFailQuick(false);
      new CD4CodeTool().run(arguments);
      List<Finding> errors = Log.getFindings().stream().filter(Finding::isError).toList();
      if (!errors.isEmpty()) {
        errors.stream().map(Finding::getMsg).distinct().forEach(System.err::println);
        System.exit(1);
      }
    } catch (Throwable throwable) {
      throwable.printStackTrace(System.err);
      System.exit(1);
    }
  }
}
