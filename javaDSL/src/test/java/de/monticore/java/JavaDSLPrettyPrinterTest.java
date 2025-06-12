/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._ast.ASTJavaDSLNode;
import de.monticore.java.javadsl._parser.JavaDSLParser;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public final class JavaDSLPrettyPrinterTest extends AbstractTest {
  
  @ParameterizedTest
  @ValueSource(strings = {
      "src/test/resources/de/monticore/java/parser/ASTClassDeclaration.java",
      "src/test/resources/de/monticore/java/parser/ParseException.java",
      "src/test/resources/de/monticore/java/parser/TokenMgrError.java",
      "src/test/resources/parsableAndCompilableModels/simpleTestClasses/HelloWorld.java"
  })
  public void testPrettyPrinter(String path) throws IOException {
    // Parse input
    ASTJavaDSLNode ast = parse(path);
    
    // Prettyprinting input
    String output = JavaDSLMill.prettyPrint(ast, false);
    
    // Parsing printed input
    ASTJavaDSLNode printedAST = parse(new StringReader(output));
    assertTrue(ast.deepEquals(printedAST));
  }
  
  private ASTJavaDSLNode parse(String modelName) throws IOException {
    Path model = Paths.get(modelName);
    JavaDSLParser parser = JavaDSLMill.parser();
    Optional<ASTCompilationUnit> ast = parser.parse(model.toString());
    assertFalse(parser.hasErrors());
    assertTrue(ast.isPresent());
    return ast.get();
  }
  
  private ASTJavaDSLNode parse(StringReader reader) throws IOException {
    JavaDSLParser parser = JavaDSLMill.parser();
    Optional<ASTCompilationUnit> ast = parser.parse(reader);
    assertFalse(parser.hasErrors());
    assertTrue(ast.isPresent());
    return ast.get();
  }
}
