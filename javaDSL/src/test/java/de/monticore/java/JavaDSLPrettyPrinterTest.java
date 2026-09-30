/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._ast.ASTJavaDSLNode;
import de.monticore.java.javadsl._parser.JavaDSLParser;
import de.monticore.java.util.JavaSourceTest;
import de.monticore.runtime.junit.TestWithMCLanguage;
import de.se_rwth.commons.logging.Log;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestWithMCLanguage(JavaDSLMill.class)
public final class JavaDSLPrettyPrinterTest {

  @JavaSourceTest(basePath = "src/test/resources/de/monticore/java/parser", files = {
      "ASTClassDeclaration.java", "ParseException.java", "TokenMgrError.java" })
  @JavaSourceTest(basePath = "src/test/resources/parsableAndCompilableModels/simpleTestClasses",
      files = {"HelloWorld.java"})
  public void testPrettyPrinter(String path) throws IOException {
    // Parse input
    ASTJavaDSLNode ast = parse(path);
    
    // Prettyprinting input
    String output = JavaDSLMill.prettyPrint(ast, false);
    
    // Parsing printed input
    ASTJavaDSLNode printedAST = parse(new StringReader(output));
    assertTrue(ast.deepEquals(printedAST));
    assertTrue(Log.getFindings().isEmpty());
  }
  
  private ASTJavaDSLNode parse(String modelName) throws IOException {
    Path model = Paths.get(modelName);
    JavaDSLParser parser = JavaDSLMill.parser();
    Optional<ASTCompilationUnit> ast = parser.parse(model.toString());
    assertFalse(parser.hasErrors());
    assertTrue(ast.isPresent());
    assertTrue(Log.getFindings().isEmpty());
    return ast.get();
  }
  
  private ASTJavaDSLNode parse(StringReader reader) throws IOException {
    JavaDSLParser parser = JavaDSLMill.parser();
    Optional<ASTCompilationUnit> ast = parser.parse(reader);
    assertFalse(parser.hasErrors());
    assertTrue(ast.isPresent());
    assertTrue(Log.getFindings().isEmpty());
    return ast.get();
  }
}
