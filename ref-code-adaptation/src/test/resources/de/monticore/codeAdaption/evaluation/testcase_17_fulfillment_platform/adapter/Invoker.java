package de.monticore.codeAdaption.evaluation.testcase_17_fulfillment_platform.adapter;

import de.monticore.codeAdaption.utils.Adapt;
import java.util.ArrayList;
import java.util.List;

@Adapt(ref = "Invoker", template = "${}")
public class Invoker {
  @Adapt(ref = "Invoker.commands", template = "${}")
  private final List<Command> commands = new ArrayList<>();

  @Adapt(ref = "Invoker.enqueue", template = "${}")
  public void enqueue(Command command) {
    commands.add(command);
  }

  @Adapt(ref = "Invoker.runAll", template = "${}")
  public boolean runAll() {
    boolean successful = true;
    for (Command command : commands) {
      if (successful) {
        successful = command.execute();
      }
    }
    commands.clear();
    return successful;
  }
}
