package de.monticore.codeAdaption.cdconcretization.evaluation.banking;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Customer"}, template = "${}")
public class Customer {
  @Adapt(ref = {"Customer.name"}, template = "${}")
  private String name;

  @Adapt(ref = {"Customer.address"}, template = "${}")
  private String address;

  @Adapt(ref = {"Customer.phoneNumber"}, template = "${}")
  private String phoneNumber;

}
