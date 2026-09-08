package de.monticore.codeAdaption.evaluation.testcase_16_observer_command_combined.concrete;

import java.util.ArrayList;
import java.util.List;

public class EventBus {
  private final List<WorkflowCommand> listeners = new ArrayList<>();
  String lastEvent;

  public void subscribe(WorkflowCommand command) {
    listeners.add(command);
  }

  public void unsubscribe(WorkflowCommand command) {
    listeners.remove(command);
  }

  public void publish(String event) {
    lastEvent = event;
    for (WorkflowCommand listener : listeners) {
      listener.run(event, false);
    }
  }
}
