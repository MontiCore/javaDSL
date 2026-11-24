package de.monticore.codeAdaption.cdconcretization.evaluation.staticdelegator.adapter.staticdelegator;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"StaticDelegator"}, template = "${}")
public class StaticDelegator {
  @Adapt(ref = {"StaticDelegator._method"}, template = "${}")
  public Object _method() {
    return null;
  }

  @Adapt(ref = {"StaticDelegator.method"}, template = "${}")
  public Object method() {
    return null;
  }

}
