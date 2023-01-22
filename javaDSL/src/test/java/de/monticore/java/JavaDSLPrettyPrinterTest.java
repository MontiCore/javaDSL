/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._ast.ASTJavaDSLNode;
import de.monticore.java.javadsl._parser.JavaDSLParser;
import de.monticore.java.prettyprint.JavaDSLFullPrettyPrinter;
import de.monticore.prettyprint.IndentPrinter;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.jupiter.api.Test;

<<<<<<< HEAD
import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._ast.ASTJavaDSLNode;
import de.monticore.java.javadsl._parser.JavaDSLParser;
import de.monticore.java.prettyprint.JavaDSLPrettyPrinterDelegator;
import de.monticore.prettyprint.IndentPrinter;
import de.se_rwth.commons.logging.LogStub;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * TODO: Write me!
 *
 */
public class JavaDSLPrettyPrinterTest {
  
  @BeforeClass
  public static void setup() {
    Log.init();
    Log.enableFailQuick(false);
  }
  
  protected ASTJavaDSLNode parse(String modelName) throws RecognitionException, IOException {
=======
import static org.junit.jupiter.api.Assertions.*;

public final class JavaDSLPrettyPrinterTest extends AbstractTest {

  private ASTJavaDSLNode parse(String modelName) throws IOException {
>>>>>>> b1632b992b7e88612c226e7c337d14e8cbff6536
    Path model = Paths.get(modelName);
    JavaDSLParser parser = new JavaDSLParser();
    Optional<ASTCompilationUnit> ast = parser.parse(model.toString());
    assertFalse(parser.hasErrors());
    assertTrue(ast.isPresent());
    return ast.get();
  }

  private ASTJavaDSLNode parse(StringReader reader) throws IOException {
    JavaDSLParser parser = new JavaDSLParser();
    Optional<ASTCompilationUnit> ast = parser.parse(reader);
    assertFalse(parser.hasErrors());
    assertTrue(ast.isPresent());
    return ast.get();
  }

  @Test
  public void test1() throws IOException {
    // Parse input
    ASTJavaDSLNode ast = parse("src/test/resources/de/monticore/java/parser/ASTClassDeclaration.java");

    // Prettyprinting input
    JavaDSLFullPrettyPrinter prettyPrinter = new JavaDSLFullPrettyPrinter(new IndentPrinter());
    String output = prettyPrinter.prettyprint(ast);

    // Parsing printed input
    ASTJavaDSLNode printedAST = parse(new StringReader(output));
    assertTrue(ast.deepEquals(printedAST));
  }

  @Test
  public void test2() throws IOException {
    // Parse input
    ASTJavaDSLNode ast = parse("src/test/resources/de/monticore/java/parser/ParseException.java");

    // Prettyprinting input
    JavaDSLFullPrettyPrinter prettyPrinter = new JavaDSLFullPrettyPrinter(new IndentPrinter());
    String output = prettyPrinter.prettyprint(ast);

    // Parsing printed input
    ASTJavaDSLNode printedAST = parse(new StringReader(output));
    assertTrue(ast.deepEquals(printedAST));
  }

  @Test
  public void test3() throws IOException {
    // Parse input
    ASTJavaDSLNode ast = parse("src/test/resources/de/monticore/java/parser/TokenMgrError.java");

    // Prettyprinting input
    JavaDSLFullPrettyPrinter prettyPrinter = new JavaDSLFullPrettyPrinter(new IndentPrinter());
    String output = prettyPrinter.prettyprint(ast);

    // Parsing printed input
    ASTJavaDSLNode printedAST = parse(new StringReader(output));
    assertTrue(ast.deepEquals(printedAST));
  }

  @Test
  public void test4() throws IOException {
    // Parse input
    ASTJavaDSLNode ast = parse("src/test/resources/parsableAndCompilableModels/simpleTestClasses/HelloWorld.java");

    // Prettyprinting input
    JavaDSLFullPrettyPrinter prettyPrinter = new JavaDSLFullPrettyPrinter(new IndentPrinter());
    String output = prettyPrinter.prettyprint(ast);

    // Parsing printed input
    ASTJavaDSLNode printedAST = parse(new StringReader(output));
    assertTrue(ast.deepEquals(printedAST));
  }

}
