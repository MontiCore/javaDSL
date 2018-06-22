/* (c) https://github.com/MontiCore/monticore */
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
    Scope artifactScope = astCompilationUnit.getEnclosingScope();
    
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
  
  @Test
  public void checkGenericClass() throws IOException {
    createAstAndST("src/test/resources/parsableAndCompilableModels/simpleTestClasses",
        "GenericClass");
  }
  
  @Test
  public void checkByte() throws IOException {
    createAstAndST("src/test/resources/",
        "java.lang.Byte");
  }}
