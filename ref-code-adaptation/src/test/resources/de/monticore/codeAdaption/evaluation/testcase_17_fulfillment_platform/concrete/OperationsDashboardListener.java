package de.monticore.codeAdaption.evaluation.testcase_17_fulfillment_platform.concrete;

public class OperationsDashboardListener implements ShipmentEventListener {
  private int processedShipments;
  private String latestOrderId = "";

  @Override
  public void onShipmentEvent(ShipmentRecord shipment) {
    processedShipments++;
    latestOrderId = shipment.getOrderId();
  }

  public int getProcessedShipments() {
    return processedShipments;
  }

  public String getLatestOrderId() {
    return latestOrderId;
  }
}
