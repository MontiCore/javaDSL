package de.monticore.codeAdaption.evaluation.testcase_16_observer_command_combined.adapter;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = "Observer", template = "${}")
public interface Observer {
  @Adapt(ref = "Observer.update", template = "${}")
  void update();
}
