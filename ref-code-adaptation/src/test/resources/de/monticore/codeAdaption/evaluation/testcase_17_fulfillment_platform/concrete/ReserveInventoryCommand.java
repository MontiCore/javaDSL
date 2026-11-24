package de.monticore.codeAdaption.evaluation.testcase_17_fulfillment_platform.concrete;

public class ReserveInventoryCommand implements FulfillmentCommand {
  private final InventoryLedger inventory;
  private final DeliveryOrder order;

  public ReserveInventoryCommand(InventoryLedger inventory, DeliveryOrder order) {
    this.inventory = inventory;
    this.order = order;
  }

  @Override
  public boolean run() {
    return inventory.reserve(order);
  }
}
