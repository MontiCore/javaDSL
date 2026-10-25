package de.monticore.codeAdaption.evaluation.testcase_14_adapter_factory_combined.concrete;

public class LegacyCarrier {
  String endpoint;

  public boolean dispatch(String label, boolean insured) {
    return label != null && !label.isBlank();
  }
}
