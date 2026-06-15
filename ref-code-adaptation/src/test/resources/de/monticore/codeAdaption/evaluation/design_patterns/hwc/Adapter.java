package de.monticore.codeAdaption.evaluation.design_patterns.Adapter;

import de.monticore.codeAdaption.utils.Adapt;

public class Adapter implements Target {

  @Adapt(ref="operation",template="${}")
  public void operation() {
    // do nothing
  }
}