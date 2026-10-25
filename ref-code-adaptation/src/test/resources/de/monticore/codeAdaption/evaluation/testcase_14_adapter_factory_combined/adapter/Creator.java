package de.monticore.codeAdaption.evaluation.testcase_14_adapter_factory_combined.adapter;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = "Creator", template = "${}")
public abstract class Creator {
  @Adapt(ref = "Creator.create", template = "${}")
  public Target create(Adaptee adaptee) {
    return new Adapter(adaptee);
  }
}
