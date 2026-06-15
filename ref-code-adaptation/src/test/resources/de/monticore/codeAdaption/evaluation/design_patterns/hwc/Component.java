package de.monticore.codeAdaption.evaluation.design_patterns.Composition;

import de.monticore.codeAdaption.utils.Adapt;

public interface Component {

  @Adapt(ref="execute",template="${}")
  public void execute();

}