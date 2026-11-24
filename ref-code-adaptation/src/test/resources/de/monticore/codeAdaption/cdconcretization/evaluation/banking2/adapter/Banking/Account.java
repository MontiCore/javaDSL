package de.monticore.codeAdaption.cdconcretization.evaluation.banking2.adapter.banking;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Account"}, template = "${}")
public class Account {
  @Adapt(ref = {"Account.id"}, template = "${}")
  private String id;

  @Adapt(ref = {"Account.balance"}, template = "${}")
  private double balance;

  @Adapt(ref = {"Account.status"}, template = "${}")
  private AccountStatus status;

  @Adapt(ref = {"Account.withdraw"}, template = "${}")
  public void withdraw(double amount) {
  }

  @Adapt(ref = {"Account.deposit"}, template = "${}")
  public void deposit(double amount) {
  }

  @Adapt(ref = {"Account.transfer"}, template = "${}")
  public void transfer(Account targetAccount, double amount) {
  }

}
