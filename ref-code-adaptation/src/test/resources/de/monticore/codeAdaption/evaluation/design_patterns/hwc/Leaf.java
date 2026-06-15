package de.monticore.codeAdaption.evaluation.design_patterns.Composition;

import de.monticore.codeAdaption.utils.Adapt;

public class Leaf implements Component {

  @Adapt(ref="execute",template="${}")
  public void execute() {
    // do nothing
  }
}