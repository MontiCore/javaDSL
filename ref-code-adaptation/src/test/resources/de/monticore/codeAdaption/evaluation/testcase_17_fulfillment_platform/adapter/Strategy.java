package de.monticore.codeAdaption.evaluation.testcase_17_fulfillment_platform.adapter;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = "Strategy", template = "${}")
public interface Strategy {
  @Adapt(ref = "Strategy.execute", template = "${}")
  String execute(Order order);
}
