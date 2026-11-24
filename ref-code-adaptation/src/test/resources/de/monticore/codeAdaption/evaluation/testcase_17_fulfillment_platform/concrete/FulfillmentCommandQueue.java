package de.monticore.codeAdaption.evaluation.testcase_17_fulfillment_platform.concrete;

/** The command list is supplied by adapted reference code. */
public class FulfillmentCommandQueue {
  public void schedule(FulfillmentCommand command) {
    commands.add(command);
  }

  public boolean drain() {
    boolean successful = true;
    for (FulfillmentCommand command : commands) {
      if (successful) {
        successful = command.run();
      }
    }
    commands.clear();
    return successful;
  }
}
