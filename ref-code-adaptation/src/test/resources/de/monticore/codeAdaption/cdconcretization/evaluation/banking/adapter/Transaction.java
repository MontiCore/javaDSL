package de.monticore.codeAdaption.cdconcretization.evaluation.banking;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Transaction"}, template = "${}")
public class Transaction {
  @Adapt(ref = {"Transaction.sourceAccount"}, template = "${}")
  private Account sourceAccount;

  @Adapt(ref = {"Transaction.targetAccount"}, template = "${}")
  private Account targetAccount;

  @Adapt(ref = {"Transaction.amount"}, template = "${}")
  private double amount;

}
