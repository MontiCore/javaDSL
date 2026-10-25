package de.monticore.codeAdaption.evaluation.testcase_14_adapter_factory_combined.concrete;

public class CarrierAdapter implements ShippingPort {
  private final LegacyCarrier carrier;

  public CarrierAdapter(LegacyCarrier carrier) {
    this.carrier = carrier;
  }

  @Override
  public boolean ship(String label, boolean insured) {
    return carrier.dispatch(label, insured);
  }
}
