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
package de.monticore.java;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Optional;

import org.junit.Test;

import de.monticore.generating.templateengine.reporting.commons.ReportingRepository;
import de.monticore.generating.templateengine.reporting.reporter.SymbolTableReporter;
import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.report.JavaDSLASTReporter;
import de.monticore.java.report.JavaDSLNodeIdentHelper;
import de.monticore.java.report.JavaDSLSymbolTableReporter;
//import de.monticore.java.javadsl._od.JavaDSL2OD;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.symboltable.Scope;
import de.monticore.symboltable.Symbol;

/**
 * Test for {@link ODPrinter}.
 *
 * @author Marita Breuer
 */
public class ODReportingTest extends AbstractTestClass {
  
  protected void createAstAndST(String pathName, String modelName) throws IOException {
    ASTCompilationUnit astCompilationUnit = parse(pathName, modelName);
    Scope artifactScope = astCompilationUnit.getEnclosingScope().get();
    
    Optional<Symbol> sym = artifactScope.resolveLocally(modelName, JavaTypeSymbol.KIND);
    assertTrue(sym.isPresent());
    assertTrue(sym.get() instanceof JavaTypeSymbol);
    JavaTypeSymbol symbol = (JavaTypeSymbol) sym.get();
    
    ReportingRepository reporting = new ReportingRepository(new JavaDSLNodeIdentHelper());
    JavaDSLASTReporter astReporter = new JavaDSLASTReporter("target", modelName, reporting);
    astReporter.flush(astCompilationUnit);
    
    SymbolTableReporter stReporter = new JavaDSLSymbolTableReporter("target", modelName,
        reporting);
    stReporter.flush(symbol.getAstNode().get());
  }
  
  @Test
  public void checkHelloWorld() throws IOException {
    createAstAndST("src/test/resources/parsableAndCompilableModels/simpleTestClasses",
        "HelloWorld");
  }
  
}
