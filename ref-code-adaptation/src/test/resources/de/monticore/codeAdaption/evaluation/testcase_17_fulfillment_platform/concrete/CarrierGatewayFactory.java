package de.monticore.codeAdaption.evaluation.testcase_17_fulfillment_platform.concrete;

public class CarrierGatewayFactory {
  public CarrierGateway createGateway(LegacyCarrierApi legacyCarrier) {
    return new LegacyCarrierGatewayAdapter(legacyCarrier);
  }
}
