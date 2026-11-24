package de.monticore.codeAdaption.evaluation.testcase_17_fulfillment_platform.adapter;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = "Target", template = "${}")
public interface Target {
  @Adapt(ref = "Target.request", template = "${}")
  String request(Order order, String route);
}
