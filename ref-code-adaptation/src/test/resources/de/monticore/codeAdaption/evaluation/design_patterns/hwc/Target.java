package de.monticore.codeAdaption.evaluation.design_patterns.Composition;

import de.monticore.codeAdaption.utils.Adapt;

public interface Target {

  @Adapt(ref="Target.operation",template="${}")
  public void operation();

}