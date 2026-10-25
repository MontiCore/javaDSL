package DesignPatterns;

import de.monticore.codeAdaption.utils.Adapt;

public class Adapter implements Target {

  @Adapt(ref = "Target.operation()", template = "${}")
  public void operation() {
    // do nothing
  }
}
