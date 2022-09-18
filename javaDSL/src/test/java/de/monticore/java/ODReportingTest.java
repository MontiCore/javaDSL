/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import java.io.File;

import de.monticore.generating.templateengine.reporting.commons.ReportingRepository;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._symboltable.IJavaDSLArtifactScope;
import de.monticore.java.javadsl._symboltable.IJavaDSLGlobalScope;
import de.monticore.java.javadsl._symboltable.JavaDSLScopesGenitor;
import de.monticore.java.javadsl._visitor.JavaDSLTraverser;
import de.monticore.java.reporting.JavaDSL2ODReporter;
import de.monticore.java.reporting.JavaDSLNodeIdentHelper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static de.monticore.java.JavaDSLAssertions.*;

@Disabled // TODO These tests are working yet. There are some issues with the JavaDSL2OD transformation
public class ODReportingTest extends AbstractTest {

  private static void createAstAndST(String pathName, String modelName) {
    ASTCompilationUnit compilationUnit = assertParsingSuccess(pathName + File.separator + modelName + ".java");
    ReportingRepository reporting = new ReportingRepository(new JavaDSLNodeIdentHelper());
    JavaDSL2ODReporter reporter = new JavaDSL2ODReporter("target", modelName, reporting);

    IJavaDSLGlobalScope globalScope = JavaDSLMill.globalScope();
    globalScope.init();

    JavaDSLScopesGenitor genitor = JavaDSLMill.scopesGenitor();
    JavaDSLTraverser traverser = JavaDSLMill.traverser();

    traverser.setJavaDSLHandler(genitor);
    traverser.add4JavaDSL(genitor);
    genitor.putOnStack(globalScope);

    IJavaDSLArtifactScope artifactScope = genitor.createFromAST(compilationUnit);
    globalScope.addSubScope(artifactScope);

    reporter.flush(compilationUnit);
  }

  @Test
  public void checkHelloWorld() {
    createAstAndST("src/test/resources/parsableAndCompilableModels/simpleTestClasses", "HelloWorld");
  }

  @Test
  public void checkGenericClass() {
    createAstAndST("src/test/resources/parsableAndCompilableModels/simpleTestClasses", "GenericClass");
  }

  @Test
  public void checkByte() {
    createAstAndST("src/test/resources", "java.lang.Byte");
  }

}
