/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.report;

import java.io.File;

import de.monticore.ast.ASTNode;
import de.monticore.generating.templateengine.reporting.commons.AReporter;
import de.monticore.generating.templateengine.reporting.commons.ReportingConstants;
import de.monticore.generating.templateengine.reporting.commons.ReportingRepository;
import de.monticore.java.javadsl._ast.ASTJavaDSLNode;
import de.monticore.prettyprint.IndentPrinter;
import de.se_rwth.commons.Names;

public class JavaDSLASTReporter extends AReporter {
    
  private String modelName;
  
  private ReportingRepository reporting;
  
  public JavaDSLASTReporter(String outputDir, String modelName, ReportingRepository reporting) {
    super(outputDir + File.separator + ReportingConstants.REPORTING_DIR + File.separator
        + modelName,
        Names.getSimpleName(modelName) + "_AST", ReportingConstants.OD_FILE_EXTENSION);
    this.modelName = modelName;
    this.reporting = reporting;
  }
  
  /**
   * @see de.monticore.generating.templateengine.reporting.commons.AReporter#writeHeader()
   */
  @Override
  protected void writeHeader() {
    writeLine("/*");
    writeLine(" * ========================================================== AST for JavaDSL");
    writeLine(" */");
  }
  
  private void writeFooter() {
    writeLine("/*");
    writeLine(" * ========================================================== Explanation");
    writeLine(" * Shows the AST with all attributes as object diagram");
    writeLine(" */");
  }
  
  /**
   * @see de.monticore.generating.templateengine.reporting.commons.AReporter#flush(de.monticore.ast.ASTNode)
   */
  @Override
  public void flush(ASTNode ast) {
    writeContent(ast);
    writeFooter();
    super.flush(ast);
  }

  /**
   * @param ast
   */
  private void writeContent(ASTNode ast) {
    if (ast instanceof ASTJavaDSLNode) {
      ASTJavaDSLNode cd4aNode = (ASTJavaDSLNode) ast;
      IndentPrinter pp = new IndentPrinter();
      JavaDSLAST2OD odPrinter = new JavaDSLAST2OD(pp, reporting);
      odPrinter.printObjectDiagram(Names.getSimpleName(modelName)+"_AST", cd4aNode);
      writeLine(pp.getContent());
    }
  }
  
}
