/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import de.monticore.generating.templateengine.reporting.commons.ReportingRepository;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._symboltable.IJavaDSLArtifactScope;
import de.monticore.java.javadsl._symboltable.IJavaDSLGlobalScope;
import de.monticore.java.reporting.JavaDSL2ODReporter;
import de.monticore.java.reporting.JavaDSLNodeIdentHelper;
import de.monticore.java.util.JavaSourceTest;
import de.monticore.java.util.TestModels;
import de.monticore.runtime.junit.TestWithMCLanguage;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static de.monticore.java.JavaDSLAssertions.assertParsingSuccess;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestWithMCLanguage(JavaDSLMill.class)
public class ODReportingTest {

  @TempDir
  private Path outputDir;

  @JavaSourceTest(basePath = TestModels.PARSABLE_AND_COMPILABLE, files = {
      "simpleTestClasses/HelloWorld.java", "simpleTestClasses/GenericClass.java",
      "stressfulPackage/StressfulSyntax.java" })
  public void testReporting(Path model) throws IOException {
    String modelName = model.getFileName().toString().replaceFirst("\\.java$", "");

    report(model, modelName);

    Path expectedOutputDir = outputDir.resolve("reports").resolve(modelName);
    Path expectedOutputFile = expectedOutputDir.resolve(modelName + "_AST.od");
    assertTrue(Files.isDirectory(expectedOutputDir),
        "could not find generated directory: " + expectedOutputDir);
    assertTrue(Files.isRegularFile(expectedOutputFile),
        "could not find generated object diagram: " + expectedOutputFile);
    assertTrue(Files.size(expectedOutputFile) > 0,
        "generated object diagram is empty: " + expectedOutputFile);
  }

  private void report(Path model, String modelName) {
    ASTCompilationUnit compilationUnit = assertParsingSuccess(model);

    IJavaDSLGlobalScope globalScope = JavaDSLMill.globalScope();
    globalScope.init();
    IJavaDSLArtifactScope artifactScope =
        JavaDSLMill.scopesGenitorDelegator().createFromAST(compilationUnit);
    globalScope.addSubScope(artifactScope);

    ReportingRepository reporting = new ReportingRepository(new JavaDSLNodeIdentHelper());
    new JavaDSL2ODReporter(outputDir.toString(), modelName, reporting).flush(compilationUnit);
  }
}
