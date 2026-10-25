package de.monticore.codeAdaption.evaluation.testcase_17_fulfillment_platform.adapter;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = "Context", template = "${}")
public class Context {
  @Adapt(ref = "Context.strategy", template = "${}")
  private Strategy strategy;

  @Adapt(ref = "Context.setStrategy", template = "${}")
  public void setStrategy(Strategy strategy) {
    this.strategy = strategy;
  }

  @Adapt(ref = "Context.execute", template = "${}")
  public String execute(Order order) {
    return strategy.execute(order);
  }
}
