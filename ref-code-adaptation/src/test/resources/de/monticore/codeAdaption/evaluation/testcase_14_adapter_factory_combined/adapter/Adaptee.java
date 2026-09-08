package de.monticore.codeAdaption.evaluation.testcase_14_adapter_factory_combined.adapter;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = "Adaptee", template = "${}")
public class Adaptee {
  @Adapt(ref = "Adaptee.specificRequest", template = "${}")
  public boolean specificRequest(String payload) {
    return false;
  }
}
