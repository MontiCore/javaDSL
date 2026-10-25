package de.monticore.codeAdaption.evaluation.testcase_14_adapter_factory_combined.adapter;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = "Target", template = "${}")
public interface Target {
  @Adapt(ref = "Target.request", template = "${}")
  boolean request(String payload);
}
