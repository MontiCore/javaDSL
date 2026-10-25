package DesignPatterns;

import de.monticore.codeAdaption.utils.Adapt;

public class Leaf implements Component {

  @Adapt(ref = "Component.execute()", template = "${}")
  public void execute() {
    // do nothing
  }
}
