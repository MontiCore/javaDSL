package de.monticore.codeAdaption.evaluation.testcase_17_fulfillment_platform.concrete;

import java.util.ArrayList;
import java.util.List;

public class DeliveryOrder {
  private final String orderId;
  private final String customerEmail;
  private final List<DeliveryItem> items;
  private final boolean priority;
  private final boolean sustainable;

  public DeliveryOrder(
      String orderId,
      String customerEmail,
      List<DeliveryItem> items,
      boolean priority,
      boolean sustainable) {
    this.orderId = orderId;
    this.customerEmail = customerEmail;
    this.items = new ArrayList<>(items);
    this.priority = priority;
    this.sustainable = sustainable;
  }

  public String getOrderId() {
    return orderId;
  }

  public String getCustomerEmail() {
    return customerEmail;
  }

  public List<DeliveryItem> getItems() {
    return List.copyOf(items);
  }

  public boolean isPriority() {
    return priority;
  }

  public boolean isSustainable() {
    return sustainable;
  }

  public int totalUnits() {
    int units = 0;
    for (DeliveryItem item : items) {
      units += item.getQuantity();
    }
    return units;
  }
}
