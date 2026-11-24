package de.monticore.codeAdaption.evaluation.testcase_17_fulfillment_platform.concrete;

public class LegacyCarrierApi {
  private final String accountCode;
  private int sequence;

  public LegacyCarrierApi(String accountCode) {
    this.accountCode = accountCode;
  }

  public String createLegacyConsignment(DeliveryOrder order, String route) {
    sequence++;
    return accountCode + "-" + route + "-" + order.getOrderId() + "-" + sequence;
  }
}
