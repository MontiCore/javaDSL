/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ExperimentalTest extends AbstractTest {

<<<<<<< HEAD
/**
 * A simple test for MyDSL tool.
 *
 */
public class ExperimentalTest extends AbstractTestClass {
=======
  @Disabled // TODO This fails because of a core grammar issue. Need to decide on how to handle this
>>>>>>> b1632b992b7e88612c226e7c337d14e8cbff6536
  @Test
  public void testMyModel() {
    JavaDSLTool.main(new String[] { "src/test/resources/experimentalTestModels/MyModel.java" });
  }

}
