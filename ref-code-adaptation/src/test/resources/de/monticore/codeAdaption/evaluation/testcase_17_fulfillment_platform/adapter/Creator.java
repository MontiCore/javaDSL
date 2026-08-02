package de.monticore.codeAdaption.evaluation.testcase_17_fulfillment_platform.adapter;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = "Creator", template = "${}")
public class Creator {
  @Adapt(ref = "Creator.create", template = "${}")
  public Target create(Adaptee adaptee) {
    return new Adapter(adaptee);
  }
}
