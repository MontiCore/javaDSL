/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import de.se_rwth.commons.logging.LogStub;
import org.antlr.v4.runtime.RecognitionException;
import org.junit.BeforeClass;
import org.junit.Test;

import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._ast.ASTJavaDSLNode;
import de.monticore.java.javadsl._parser.JavaDSLParser;
import de.monticore.java.prettyprint.JavaDSLPrettyPrinter;
import de.monticore.prettyprint.IndentPrinter;
import de.se_rwth.commons.logging.Log;
import de.se_rwth.commons.logging.Slf4jLog;


/**
 * TODO: Write me!
 *
 * @author  (last commit) $Author$
 * @since   TODO: add version number
 *
 */
public class JavaDSLPrettyPrinterTest {
  
  @BeforeClass
  public static void setup() {
    LogStub.init();
    LogStub.enableFailQuick(false);
  }
  
  protected ASTJavaDSLNode parse(String modelName) throws RecognitionException, IOException{
    Path model = Paths.get(modelName);
    JavaDSLParser parser = new JavaDSLParser();
    Optional<ASTCompilationUnit> ast = parser.parse(model.toString());
    assertFalse(parser.hasErrors());
    assertTrue(ast.isPresent());
    return ast.get();
  }
  
  protected ASTJavaDSLNode parse(StringReader reader) throws RecognitionException, IOException{
    JavaDSLParser parser = new JavaDSLParser();
    Optional<ASTCompilationUnit> ast = parser.parse(reader);
    assertFalse(parser.hasErrors());
    assertTrue(ast.isPresent());
    return ast.get();
  }


  @Test
  public void test1() throws RecognitionException, IOException {
    // Parse input
    ASTJavaDSLNode ast = parse("src/test/resources/de/monticore/java/parser/ASTClassDeclaration.java");
        
    // Prettyprinting input
    JavaDSLPrettyPrinter prettyPrinter = new JavaDSLPrettyPrinter(new IndentPrinter());
    String output = prettyPrinter.prettyprint(ast);
    
    // Parsing printed input
    ASTJavaDSLNode printedAST = parse(new StringReader(output));
    assertTrue(ast.deepEquals(printedAST));
  }

  @Test
  public void test2() throws RecognitionException, IOException {
    // Parse input
    ASTJavaDSLNode ast = parse("src/test/resources/de/monticore/java/parser/ParseException.java");
        
    // Prettyprinting input
    JavaDSLPrettyPrinter prettyPrinter = new JavaDSLPrettyPrinter(new IndentPrinter());
    String output = prettyPrinter.prettyprint(ast);
    
    // Parsing printed input
    ASTJavaDSLNode printedAST = parse(new StringReader(output));
    assertTrue(ast.deepEquals(printedAST));
  }

  @Test
  public void test3() throws RecognitionException, IOException {
    // Parse input
    ASTJavaDSLNode ast = parse("src/test/resources/de/monticore/java/parser/TokenMgrError.java");
        
    // Prettyprinting input
    JavaDSLPrettyPrinter prettyPrinter = new JavaDSLPrettyPrinter(new IndentPrinter());
    String output = prettyPrinter.prettyprint(ast);
    
    // Parsing printed input
    ASTJavaDSLNode printedAST = parse(new StringReader(output));
    assertTrue(ast.deepEquals(printedAST));
  }
  
  @Test
  public void test4() throws RecognitionException, IOException {
    // Parse input
    ASTJavaDSLNode ast = parse("src/test/resources/parsableAndCompilableModels/simpleTestClasses/HelloWorld.java");
        
    // Prettyprinting input
    JavaDSLPrettyPrinter prettyPrinter = new JavaDSLPrettyPrinter(new IndentPrinter());
    String output = prettyPrinter.prettyprint(ast);
    System.out.println(output);
    
    // Parsing printed input
    ASTJavaDSLNode printedAST = parse(new StringReader(output));
    assertTrue(ast.deepEquals(printedAST));
  }


}
