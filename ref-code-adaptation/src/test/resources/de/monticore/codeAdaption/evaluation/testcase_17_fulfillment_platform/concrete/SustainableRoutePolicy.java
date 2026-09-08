package de.monticore.codeAdaption.evaluation.testcase_17_fulfillment_platform.concrete;

public class SustainableRoutePolicy implements RoutePolicy {
  @Override
  public String selectRoute(DeliveryOrder order) {
    return order.totalUnits() > 8 ? "RAIL_CONSOLIDATED" : "ELECTRIC_VAN";
  }
}
