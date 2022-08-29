/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ExperimentalTest extends AbstractTest {

  @Disabled // TODO This fails because of a core grammar issue. Need to decide on how to handle this
  @Test
  public void testMyModel() {
    JavaDSLTool.main(new String[] { "src/test/resources/experimentalTestModels/MyModel.java" });
  }

}
