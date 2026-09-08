package de.monticore.codeAdaption.evaluation.testcase_17_fulfillment_platform.adapter;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = "Adapter", template = "${}")
public class Adapter implements Target {
  @Adapt(ref = "Adapter.adaptee", template = "${}")
  private final Adaptee adaptee;

  public Adapter(Adaptee adaptee) {
    this.adaptee = adaptee;
  }

  @Override
  @Adapt(ref = "Target.request", template = "${}")
  public String request(Order order, String route) {
    return adaptee.specificRequest(order, route);
  }
}
