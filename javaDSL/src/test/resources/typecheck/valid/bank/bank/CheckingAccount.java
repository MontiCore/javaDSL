/* (c) https://github.com/MontiCore/monticore */
package bank;

public class CheckingAccount extends Account {

  private final long overdraftLimit;

  public CheckingAccount(String id, long initialBalance, long overdraftLimit) {
    super(id, initialBalance);
    this.overdraftLimit = overdraftLimit;
  }

  @Override
  protected long availableAmount() {
    return balance + overdraftLimit;
  }

  @Override
  public double monthlyInterestRate() {
    return balance < 0 ? 0.01 : 0.0;
  }
}
