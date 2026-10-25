package de.monticore.codeAdaption.evaluation.testcase_17_fulfillment_platform.adapter;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = "Adaptee", template = "${}")
public class Adaptee {
  @Adapt(ref = "Adaptee.specificRequest", template = "${}")
  public String specificRequest(Order order, String route) {
    return "";
  }
}
