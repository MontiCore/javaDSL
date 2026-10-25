package de.monticore.codeAdaption.evaluation.testcase_17_fulfillment_platform.concrete;

import java.util.ArrayList;
import java.util.List;

public class ShipmentRepository {
  private final List<ShipmentRecord> shipments = new ArrayList<>();

  public void save(ShipmentRecord shipment) {
    shipments.add(shipment);
  }

  public boolean containsOrder(String orderId) {
    for (ShipmentRecord shipment : shipments) {
      if (shipment.getOrderId().equals(orderId)) {
        return true;
      }
    }
    return false;
  }

  public int size() {
    return shipments.size();
  }
}
