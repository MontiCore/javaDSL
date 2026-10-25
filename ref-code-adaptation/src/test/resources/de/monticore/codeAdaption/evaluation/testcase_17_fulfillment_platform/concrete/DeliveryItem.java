package de.monticore.codeAdaption.evaluation.testcase_17_fulfillment_platform.concrete;

public class DeliveryItem {
  private final String sku;
  private final int quantity;

  public DeliveryItem(String sku, int quantity) {
    this.sku = sku;
    this.quantity = quantity;
  }

  public String getSku() {
    return sku;
  }

  public int getQuantity() {
    return quantity;
  }
}
