package de.monticore.codeAdaption.cdconcretization.methods.multiincarnation;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Bank"}, template = "${}")
public class Bank {
  @Adapt(ref = {"Bank.createAccount"}, template = "${}")
  public void createAccount(Account account) {
  }

  @Adapt(ref = {"Bank.transfer"}, template = "${}")
  public Transaction transfer(Account from, Account to, int amount) {
    return null;
  }

}
