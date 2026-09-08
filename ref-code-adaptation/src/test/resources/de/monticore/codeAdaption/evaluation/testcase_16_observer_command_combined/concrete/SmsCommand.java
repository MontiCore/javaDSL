package de.monticore.codeAdaption.evaluation.testcase_16_observer_command_combined.concrete;

public class SmsCommand implements WorkflowCommand {
  String phoneNumber;

  @Override
  public void run(String event, boolean async) {
  }
}
