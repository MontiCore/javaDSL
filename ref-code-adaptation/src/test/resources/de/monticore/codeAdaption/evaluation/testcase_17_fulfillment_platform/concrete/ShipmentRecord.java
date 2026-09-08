package de.monticore.codeAdaption.evaluation.testcase_17_fulfillment_platform.concrete;

public class ShipmentRecord {
  private final String orderId;
  private final String route;
  private final String label;

  public ShipmentRecord(String orderId, String route, String label) {
    this.orderId = orderId;
    this.route = route;
    this.label = label;
  }

  public String getOrderId() {
    return orderId;
  }

  public String getRoute() {
    return route;
  }

  public String getLabel() {
    return label;
  }
}
