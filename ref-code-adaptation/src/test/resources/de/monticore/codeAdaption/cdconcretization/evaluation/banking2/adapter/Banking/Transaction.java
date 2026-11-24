package de.monticore.codeAdaption.cdconcretization.evaluation.banking2.adapter.banking;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Transaction"}, template = "${}")
public class Transaction {
  @Adapt(ref = {"Transaction.amount"}, template = "${}")
  private double amount;

}
