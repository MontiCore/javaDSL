package de.monticore.codeAdaption.evaluation.testcase_17_fulfillment_platform.concrete;

import java.util.HashMap;
import java.util.Map;

public class InventoryLedger {
  private final Map<String, Integer> available = new HashMap<>();
  private final Map<String, Integer> reserved = new HashMap<>();

  public void stock(String sku, int quantity) {
    available.put(sku, available.getOrDefault(sku, 0) + quantity);
  }

  public boolean reserve(DeliveryOrder order) {
    for (DeliveryItem item : order.getItems()) {
      if (available.getOrDefault(item.getSku(), 0) < item.getQuantity()) {
        return false;
      }
    }
    for (DeliveryItem item : order.getItems()) {
      available.put(
          item.getSku(), available.getOrDefault(item.getSku(), 0) - item.getQuantity());
      reserved.put(
          item.getSku(), reserved.getOrDefault(item.getSku(), 0) + item.getQuantity());
    }
    return true;
  }

  public int reservedUnits(String sku) {
    return reserved.getOrDefault(sku, 0);
  }
}
