package de.monticore.codeAdaption.evaluation.testcase_17_fulfillment_platform.concrete;

public class AuditTrailListener implements ShipmentEventListener {
  private final AuditLog auditLog;

  public AuditTrailListener(AuditLog auditLog) {
    this.auditLog = auditLog;
  }

  @Override
  public void onShipmentEvent(ShipmentRecord shipment) {
    auditLog.append("shipment:" + shipment.getOrderId() + ":" + shipment.getLabel());
  }
}
