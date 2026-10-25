package de.monticore.codeAdaption.evaluation.testcase_17_fulfillment_platform.adapter;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = "Observer", template = "${}")
public interface Observer {
  @Adapt(ref = "Observer.update", template = "${}")
  void update(String event);
}
