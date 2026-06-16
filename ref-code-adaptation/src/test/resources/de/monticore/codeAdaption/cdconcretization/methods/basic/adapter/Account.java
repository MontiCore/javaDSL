package de.monticore.codeAdaption.cdconcretization.methods.basic;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Account"}, template = "${}")
public class Account {
  @Adapt(ref = {"Account.withdraw"}, template = "${}")
  public void withdraw(int amount) {
  }

  @Adapt(ref = {"Account.deposit"}, template = "${}")
  public void deposit(int amount) {
  }

  @Adapt(ref = {"Account.transfer"}, template = "${}")
  public void transfer(int amount, Account account) {
  }

  @Adapt(ref = {"Account.getBalance"}, template = "${}")
  public int getBalance() {
    return 0;
  }

}
