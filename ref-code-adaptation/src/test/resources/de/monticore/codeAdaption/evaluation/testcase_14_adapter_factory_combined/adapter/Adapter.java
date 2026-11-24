package de.monticore.codeAdaption.evaluation.testcase_14_adapter_factory_combined.adapter;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = "Adapter", template = "${}")
public class Adapter implements Target {
  @Adapt(ref = "Adapter.adaptee", template = "${}")
  private Adaptee adaptee;

  public Adapter(Adaptee adaptee) {
    this.adaptee = adaptee;
  }

  @Adapt(ref = "Target.request", template = "${}")
  public boolean request(String payload) {
    return adaptee.specificRequest(payload);
  }
}
