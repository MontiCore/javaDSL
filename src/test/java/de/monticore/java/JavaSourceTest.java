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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import de.se_rwth.commons.logging.Log;
import de.se_rwth.commons.logging.LogStub;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * A simple test for MyDSL Tool.
 *
 */
public class JavaSourceTest {
  
  /* All files that initially failed to parse in the java source code JDK 7
   * using original grammar. */
  // @formatter:off
  static List<String[]> initialParsingFails = Arrays.asList(
      // Example:
      // new String[]{"target","javaLib","java","lang","invoke","LambdaForm.java"},
      );
  // @formatter:on

  ParseJavaFileVisitor visitor = new ParseJavaFileVisitor(initialParsingFails);
  ParseJavaFileVisitor visitorFilesFailed = new ParseJavaFileVisitor();
  
  private static final int NUMBER_TESTS = 59;
  
  private static final int NUMBER_COCOS = 532;

  @Before
  public void setup() {
    Log.init();
    Log.enableFailQuick(false);
  }
  
  @Before
  public void setUp() {
    Log.getFindings().clear();
  }


  @Test
  public void testAllJavaSourceFiles() {
    Path path = FileSystems.getDefault().getPath("target", "generated-test-resources", "JDK", "java", "lang");
    
    try {
      Files.walkFileTree(path, visitor);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    assertEquals(visitor.getNumberOfTests(), visitor.getSuccessCount());
  }
 
  @Test
  public void testJavaSourceFilesThatInitiallyFailed() {
    
    try {
      FileSystem fs = FileSystems.getDefault();
      for (String[] fail : initialParsingFails) {
        visitorFilesFailed.visitFile(fs.getPath("", fail), null);
      }
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    assertEquals(visitorFilesFailed.getNumberOfTests(), visitorFilesFailed.getFailCount());
  }
  
  @After
  public void printResult() {
    LogStub.info("Success rate: " + visitor.getSuccessRate(), JavaSourceTest.class.getName());
    LogStub.info("Fail rate: " + visitorFilesFailed.getFailRate(), JavaSourceTest.class.getName());
    LogStub.info("Number of Tests: " + visitor.getNumberOfTests(), JavaSourceTest.class.getName());
    LogStub.info("Number of Tests with failing cocos: " + visitor.getFailClassesCount(), JavaSourceTest.class.getName() + " (expecting: " + NUMBER_TESTS + ")");
    LogStub.info("Number of failing cocos: " + visitor.getFailCocosCount(), JavaSourceTest.class.getName() + " (expecting: " + NUMBER_COCOS + ")");
    for (Path fileParsedWithErrors : visitorFilesFailed.fails) {
      LogStub.info(fileParsedWithErrors.toString(), JavaSourceTest.class.getName());
    }
  }
  
}