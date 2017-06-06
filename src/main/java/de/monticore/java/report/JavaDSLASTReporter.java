/*
 * ******************************************************************************
 * MontiCore Language Workbench
 * Copyright (c) 2015, MontiCore, All rights reserved.
 *
 * This project is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this project. If not, see <http://www.gnu.org/licenses/>.
 * ******************************************************************************
 */

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