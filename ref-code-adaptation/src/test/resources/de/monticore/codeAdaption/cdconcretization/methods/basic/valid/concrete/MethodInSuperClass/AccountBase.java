package de.monticore.codeAdaption.cdconcretization.methods.basic.valid.concrete.methodinsuperclass;

public class AccountBase {
  public void withdraw(int amount) {
  }

  public void deposit(int amount) {
  }

  public void transfer(int amount, AccountBase account) {
  }

  public int getBalance() {
    return 0;
  }

}
