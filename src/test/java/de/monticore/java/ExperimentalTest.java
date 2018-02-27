/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import java.io.IOException;

import org.antlr.v4.runtime.RecognitionException;
import org.junit.Test;

/**
 * A simple test for MyDSL tool.
 *
 * @author (last commit) $Author$
 */
public class ExperimentalTest extends AbstractTestClass {
  @Test
  public void testMyModel() throws RecognitionException, IOException {
    JavaDSLTool.main(new String[] { "src/test/resources/experimentalTestModels/MyModel.java" });
  }
}
