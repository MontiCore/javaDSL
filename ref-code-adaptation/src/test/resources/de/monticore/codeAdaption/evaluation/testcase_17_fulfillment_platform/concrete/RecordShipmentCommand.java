package de.monticore.codeAdaption.evaluation.testcase_17_fulfillment_platform.concrete;

public class RecordShipmentCommand implements FulfillmentCommand {
  private final DeliveryOrder order;
  private final String route;
  private final BookCarrierCommand booking;
  private final ShipmentRepository repository;
  private final ShipmentEventPublisher publisher;

  public RecordShipmentCommand(
      DeliveryOrder order,
      String route,
      BookCarrierCommand booking,
      ShipmentRepository repository,
      ShipmentEventPublisher publisher) {
    this.order = order;
    this.route = route;
    this.booking = booking;
    this.repository = repository;
    this.publisher = publisher;
  }

  @Override
  public boolean run() {
    if (booking.getLabel().isBlank()) {
      return false;
    }
    ShipmentRecord shipment = new ShipmentRecord(order.getOrderId(), route, booking.getLabel());
    repository.save(shipment);
    publisher.publishShipment(shipment);
    return true;
  }
}
