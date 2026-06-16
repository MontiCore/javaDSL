package de.monticore.codeAdaption.evaluation.testcase_16_observer_command_combined.adapter;

import de.monticore.codeAdaption.utils.Adapt;
import java.util.ArrayList;
import java.util.List;

@Adapt(ref = "Invoker", template = "${}")
public class Invoker {
  @Adapt(ref = "Invoker.commands", template = "${}")
  private List<Command> commands = new ArrayList<>();

  @Adapt(ref = "Invoker.enqueue", template = "${}")
  public void enqueue(Command command) {
    commands.add(command);
  }

  @Adapt(ref = "Invoker.runAll", template = "${}")
  public void runAll() {
    for (Command command : commands) {
      command.execute();
    }
  }
}
