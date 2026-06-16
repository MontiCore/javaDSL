package de.monticore.codeAdaption.evaluation.testcase_16_observer_command_combined.adapter;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = "Command", template = "${}")
public interface Command {
  @Adapt(ref = "Command.execute", template = "${}")
  void execute();
}
