package de.monticore.codeAdaption.evaluation.testcase_17_fulfillment_platform.concrete;

public class RoutePlanner {
  public RoutePlanner(RoutePolicy policy) {
    this.policy = policy;
  }

  public void usePolicy(RoutePolicy policy) {
    this.policy = policy;
  }

  public String planRoute(DeliveryOrder order) {
    return policy.selectRoute(order);
  }
}
