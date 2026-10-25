package de.monticore.codeAdaption.evaluation.testcase_16_observer_command_combined.concrete;

public class EmailCommand implements WorkflowCommand {
  String address;

  @Override
  public void run(String event, boolean async) {
  }
}
