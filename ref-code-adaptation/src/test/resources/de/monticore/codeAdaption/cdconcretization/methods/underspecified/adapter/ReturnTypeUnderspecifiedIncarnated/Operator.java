package de.monticore.codeAdaption.cdconcretization.methods.underspecified.adapter.returntypeunderspecifiedincarnated;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Operator"}, template = "${}")
public class Operator {
  @Adapt(ref = {"Operator.operation"}, template = "${}")
  public Object operation() {
    return null;
  }

}
