/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._parser.JavaDSLParser;
import de.monticore.java.util.JavaSourceTest;
import de.monticore.java.util.TestModels;
import de.monticore.runtime.junit.PrettyPrinterTester;
import de.monticore.runtime.junit.TestWithMCLanguage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Parses a model, pretty prints it, and checks that the re-parsed AST equals the original. */
@TestWithMCLanguage(JavaDSLMill.class)
public final class JavaDSLPrettyPrinterTest {

  @JavaSourceTest(basePath = TestModels.PARSER, files = {
      "ASTClassDeclaration.java", "ParseException.java", "TokenMgrError.java" })
  @JavaSourceTest(basePath = TestModels.SIMPLE_TEST_CLASSES, files = {"HelloWorld.java"})
  public void testPrettyPrinter(Path path) throws IOException {
    JavaDSLParser parser = JavaDSLMill.parser();
    PrettyPrinterTester.testPrettyPrinter(
        Files.readString(path),
        parser,
        parser::parse_String,
        ast -> JavaDSLMill.prettyPrint(ast, false));
  }
}
