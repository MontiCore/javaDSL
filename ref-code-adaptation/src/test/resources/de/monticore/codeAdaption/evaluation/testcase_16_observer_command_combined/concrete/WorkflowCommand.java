package de.monticore.codeAdaption.evaluation.testcase_16_observer_command_combined.concrete;

public interface WorkflowCommand {
  void run(String event, boolean async);
}
