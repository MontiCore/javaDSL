package de.monticore.codeAdaption.cdconcretization.methods.underspecified.adapter.parametertypeunderspecifiedincarnated;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Operator"}, template = "${}")
public class Operator {
  @Adapt(ref = {"Operator.operation"}, template = "${}")
  public void operation(Object param) {
  }

}
