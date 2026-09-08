package de.monticore.codeAdaption.evaluation.testcase_17_fulfillment_platform.concrete;

public class BookCarrierCommand implements FulfillmentCommand {
  private final CarrierGateway gateway;
  private final DeliveryOrder order;
  private final String route;
  private String label = "";

  public BookCarrierCommand(CarrierGateway gateway, DeliveryOrder order, String route) {
    this.gateway = gateway;
    this.order = order;
    this.route = route;
  }

  @Override
  public boolean run() {
    label = gateway.createConsignment(order, route);
    return label != null && !label.isBlank();
  }

  public String getLabel() {
    return label;
  }
}
