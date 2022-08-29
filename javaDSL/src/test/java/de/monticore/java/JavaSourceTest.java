/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import de.se_rwth.commons.logging.Log;
import de.se_rwth.commons.logging.LogStub;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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

  @BeforeEach
  public void setup() {
    Log.init();
    Log.enableFailQuick(false);
  }

  @BeforeEach
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
    FileSystem fs = FileSystems.getDefault();
    for (String[] fail : initialParsingFails) {
      visitorFilesFailed.visitFile(fs.getPath("", fail), null);
    }
    assertEquals(visitorFilesFailed.getNumberOfTests(), visitorFilesFailed.getFailCount());
  }

  @AfterEach
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
