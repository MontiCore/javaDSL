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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

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
    Slf4jLog.init();
    Log.enableFailQuick(false);
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
}
