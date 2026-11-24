package de.monticore.codeAdaption.evaluation.testcase_17_fulfillment_platform.concrete;

import java.util.List;

public final class FulfillmentScenario {
  private FulfillmentScenario() {
  }

  public static boolean run() {
    DeliveryOrder order =
        new DeliveryOrder(
            "ORDER-2026-017",
            "customer@example.test",
            List.of(new DeliveryItem("ROBOT-ARM", 2), new DeliveryItem("SENSOR-KIT", 3)),
            false,
            true);

    InventoryLedger inventory = new InventoryLedger();
    inventory.stock("ROBOT-ARM", 5);
    inventory.stock("SENSOR-KIT", 10);
    ShipmentRepository shipments = new ShipmentRepository();
    AuditLog auditLog = new AuditLog();
    CustomerNotifier notifier = new CustomerNotifier();

    ShipmentEventPublisher publisher = new ShipmentEventPublisher();
    OperationsDashboardListener dashboard = new OperationsDashboardListener();
    publisher.subscribe(new AuditTrailListener(auditLog));
    publisher.subscribe(new CustomerNotificationListener(notifier, order.getCustomerEmail()));
    publisher.subscribe(dashboard);

    RoutePlanner planner = new RoutePlanner(new FastestRoutePolicy());
    CarrierGateway gateway =
        new CarrierGatewayFactory().createGateway(new LegacyCarrierApi("FULFILLMENT-EU"));
    FulfillmentCoordinator coordinator =
        new FulfillmentCoordinator(
            planner,
            gateway,
            new FulfillmentCommandQueue(),
            publisher,
            inventory,
            shipments);

    boolean completed = coordinator.fulfill(order);
    return completed
        && inventory.reservedUnits("ROBOT-ARM") == 2
        && inventory.reservedUnits("SENSOR-KIT") == 3
        && shipments.containsOrder(order.getOrderId())
        && auditLog.containsOrder(order.getOrderId())
        && notifier.notified(order.getCustomerEmail())
        && dashboard.getProcessedShipments() == 1
        && dashboard.getLatestOrderId().equals(order.getOrderId());
  }

  /**
   * Proves that an adapted command queue stops after failure, clears the failed batch, and can be
   * reused without replaying old commands.
   */
  public static boolean runQueueFailureAndReuse() {
    int[] executions = {0};
    FulfillmentCommandQueue queue = new FulfillmentCommandQueue();
    queue.schedule(new ProbeCommand(executions, 1, false));
    queue.schedule(new ProbeCommand(executions, 100, true));
    boolean firstResult = queue.drain();

    queue.schedule(new ProbeCommand(executions, 10, true));
    boolean secondResult = queue.drain();
    return !firstResult && secondResult && executions[0] == 11;
  }

  private static final class ProbeCommand implements FulfillmentCommand {
    private final int[] executions;
    private final int weight;
    private final boolean result;

    private ProbeCommand(int[] executions, int weight, boolean result) {
      this.executions = executions;
      this.weight = weight;
      this.result = result;
    }

    @Override
    public boolean run() {
      executions[0] += weight;
      return result;
    }
  }
}
