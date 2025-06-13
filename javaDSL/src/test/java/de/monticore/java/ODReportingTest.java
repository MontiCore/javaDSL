/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import de.monticore.generating.templateengine.reporting.commons.ReportingRepository;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._symboltable.IJavaDSLArtifactScope;
import de.monticore.java.javadsl._symboltable.IJavaDSLGlobalScope;
import de.monticore.java.javadsl._symboltable.JavaDSLScopesGenitorDelegator;
import de.monticore.java.reporting.JavaDSL2ODReporter;
import de.monticore.java.reporting.JavaDSLNodeIdentHelper;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static de.monticore.java.JavaDSLAssertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class ODReportingTest extends AbstractTest {
  
  @TempDir
  private Path outputDir;
  
  private static void createAstAndST(Path pathName, String modelName, Path outputDir) {
    ASTCompilationUnit compilationUnit =
        assertParsingSuccess(pathName + File.separator + modelName + ".java");
    
    ReportingRepository reporting = new ReportingRepository(new JavaDSLNodeIdentHelper());
    JavaDSL2ODReporter reporter =
        new JavaDSL2ODReporter(outputDir.toString(), modelName, reporting);
    
    IJavaDSLGlobalScope globalScope = JavaDSLMill.globalScope();
    globalScope.init();
    
    JavaDSLScopesGenitorDelegator genitor = JavaDSLMill.scopesGenitorDelegator();
    IJavaDSLArtifactScope artifactScope = genitor.createFromAST(compilationUnit);
    globalScope.addSubScope(artifactScope);
    
    reporter.flush(compilationUnit);
  }
  
  public static Stream<Arguments> testReporting() {
    Path resourcePath = Paths.get("src", "test", "resources", "parsableAndCompilableModels");
    return Stream.of(arguments(resourcePath.resolve("simpleTestClasses"), "HelloWorld"),
        arguments(resourcePath.resolve("simpleTestClasses"), "GenericClass"),
        arguments(resourcePath.resolve("stressfulPackage"), "StressfulSyntax"));
  }
  
  @ParameterizedTest
  @MethodSource
  public void testReporting(Path basePath, String modelName) {
    createAstAndST(basePath, modelName, outputDir);
    
    Path expectedOutputDir = outputDir.resolve("reports").resolve(modelName);
    Path expectedOutputFile = expectedOutputDir.resolve(modelName + "_AST.od");
    assertTrue(expectedOutputDir.toFile().exists(),
        "could not find generated directory: " + expectedOutputDir);
    assertTrue(expectedOutputDir.toFile().isDirectory(),
        "output directory is a file: " + expectedOutputDir);
    assertTrue(expectedOutputFile.toFile().exists(),
        "could not find generated object diagram: " + expectedOutputFile);
    assertTrue(expectedOutputFile.toFile().length() > 0, "generated object diagram is empty");
  }
}
