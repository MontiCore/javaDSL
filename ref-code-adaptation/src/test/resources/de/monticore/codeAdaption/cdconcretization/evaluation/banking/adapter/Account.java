package de.monticore.codeAdaption.cdconcretization.evaluation.banking;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Account"}, template = "${}")
public class Account {
  @Adapt(ref = {"Account.id"}, template = "${}")
  private String id;

  @Adapt(ref = {"Account.balance"}, template = "${}")
  private double balance;

}
