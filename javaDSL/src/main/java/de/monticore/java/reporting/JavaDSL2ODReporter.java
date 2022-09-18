package de.monticore.java.reporting;

import de.monticore.ast.ASTNode;
import de.monticore.generating.templateengine.reporting.commons.AReporter;
import de.monticore.generating.templateengine.reporting.commons.ReportingConstants;
import de.monticore.generating.templateengine.reporting.commons.ReportingRepository;
import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.prettyprint.IndentPrinter;
import de.se_rwth.commons.Names;

import java.io.File;

public final class JavaDSL2ODReporter extends AReporter {

  private final ReportingRepository reporting;

  public JavaDSL2ODReporter(String outputDir, String modelName, ReportingRepository reporting) {
    super(
        outputDir + File.separator + ReportingConstants.REPORTING_DIR + File.separator + modelName,
        Names.getSimpleName(modelName) + "_AST",
        ReportingConstants.OD_FILE_EXTENSION
    );

    this.reporting = reporting;
  }

  @Override
  public void flush(ASTNode ast) {
    writeContent(ast);
    writeFooter();
    super.flush(ast);
  }

  private void writeContent(ASTNode node) {
    if (node instanceof ASTCompilationUnit) {
      ASTCompilationUnit compilationUnit = (ASTCompilationUnit) node;

      IndentPrinter printer = new IndentPrinter();
      JavaDSL2ODDelegator transformer = new JavaDSL2ODDelegator(printer, this.reporting);
      writeLine(transformer.print(compilationUnit));

      writeLine(printer.getContent());
    }
  }

  private void writeFooter() {
    writeLine("/*");
    writeLine(" * ========================================================== Explanation");
    writeLine(" * Shows the AST with all attributes as object diagram");
    writeLine(" */");
  }

  @Override
  protected void writeHeader() {
    writeLine("/*");
    writeLine(" * ========================================================== AST for JavaDSL");
    writeLine(" */");
  }

}
