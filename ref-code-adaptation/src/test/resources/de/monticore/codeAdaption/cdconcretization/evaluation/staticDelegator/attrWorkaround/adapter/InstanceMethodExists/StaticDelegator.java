package de.monticore.codeAdaption.cdconcretization.evaluation.staticdelegator.attrworkaround.adapter.instancemethodexists;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"StaticDelegator"}, template = "${}")
public class StaticDelegator {
  @Adapt(ref = {"StaticDelegator._method"}, template = "${}")
  private Object _method;

  @Adapt(ref = {"StaticDelegator.method"}, template = "${}")
  private Object method;

}
