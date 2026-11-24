package de.monticore.codeAdaption.evaluation.testcase_16_observer_command_combined.concrete;

import java.util.ArrayList;
import java.util.List;

public class CommandQueue {
  private final List<WorkflowCommand> backlog = new ArrayList<>();

  public void schedule(WorkflowCommand command) {
    backlog.add(command);
  }

  public void drain(String event) {
    for (WorkflowCommand command : backlog) {
      command.run(event, false);
    }
  }
}
