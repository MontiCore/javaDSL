package de.monticore.codeAdaption.evaluation.testcase_17_fulfillment_platform.concrete;

public class FulfillmentCoordinator {
  private final RoutePlanner routePlanner;
  private final CarrierGateway carrierGateway;
  private final FulfillmentCommandQueue commandQueue;
  private final ShipmentEventPublisher eventPublisher;
  private final InventoryLedger inventory;
  private final ShipmentRepository shipments;

  public FulfillmentCoordinator(
      RoutePlanner routePlanner,
      CarrierGateway carrierGateway,
      FulfillmentCommandQueue commandQueue,
      ShipmentEventPublisher eventPublisher,
      InventoryLedger inventory,
      ShipmentRepository shipments) {
    this.routePlanner = routePlanner;
    this.carrierGateway = carrierGateway;
    this.commandQueue = commandQueue;
    this.eventPublisher = eventPublisher;
    this.inventory = inventory;
    this.shipments = shipments;
  }

  public boolean fulfill(DeliveryOrder order) {
    RoutePolicy selectedPolicy =
        order.isSustainable() ? new SustainableRoutePolicy() : new FastestRoutePolicy();
    routePlanner.usePolicy(selectedPolicy);
    String route = routePlanner.planRoute(order);

    BookCarrierCommand booking = new BookCarrierCommand(carrierGateway, order, route);
    commandQueue.schedule(new ReserveInventoryCommand(inventory, order));
    commandQueue.schedule(booking);
    commandQueue.schedule(
        new RecordShipmentCommand(order, route, booking, shipments, eventPublisher));
    return commandQueue.drain();
  }
}
