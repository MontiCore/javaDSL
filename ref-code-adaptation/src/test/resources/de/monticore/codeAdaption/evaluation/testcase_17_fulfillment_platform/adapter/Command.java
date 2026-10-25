package de.monticore.codeAdaption.evaluation.testcase_17_fulfillment_platform.adapter;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = "Command", template = "${}")
public interface Command {
  @Adapt(ref = "Command.execute", template = "${}")
  boolean execute();
}
