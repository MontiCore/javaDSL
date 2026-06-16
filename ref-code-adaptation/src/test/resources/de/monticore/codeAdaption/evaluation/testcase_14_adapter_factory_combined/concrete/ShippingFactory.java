package de.monticore.codeAdaption.evaluation.testcase_14_adapter_factory_combined.concrete;

public class ShippingFactory {
  public ShippingPort createCarrier(LegacyCarrier carrier) {
    return new CarrierAdapter(carrier);
  }
}
