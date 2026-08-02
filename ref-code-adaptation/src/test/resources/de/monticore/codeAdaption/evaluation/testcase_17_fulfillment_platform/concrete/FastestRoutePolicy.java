package de.monticore.codeAdaption.evaluation.testcase_17_fulfillment_platform.concrete;

public class FastestRoutePolicy implements RoutePolicy {
  @Override
  public String selectRoute(DeliveryOrder order) {
    return order.isPriority() ? "AIR_EXPRESS" : "ROAD_EXPRESS";
  }
}
