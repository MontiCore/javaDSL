package de.monticore.codeAdaption.cdconcretization.evaluation.banking2;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Bank"}, template = "${}")
public class Bank {
  @Adapt(ref = {"Bank.id"}, template = "${}")
  private String id;

  @Adapt(ref = {"Bank.overallBalance"}, template = "${}")
  private double overallBalance;

}
