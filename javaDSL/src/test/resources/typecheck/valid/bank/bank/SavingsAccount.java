/* (c) https://github.com/MontiCore/monticore */
package bank;

public class SavingsAccount extends Account {

  public static final double DEFAULT_RATE = 0.002;

  private double rate = DEFAULT_RATE;

  public SavingsAccount(String id) {
    super(id, 0);
  }

  public void setRate(double rate) {
    this.rate = rate;
  }

  @Override
  protected long availableAmount() {
    return balance;
  }

  @Override
  public double monthlyInterestRate() {
    return rate / 12;
  }

  public long interestForMonths(int months) {
    double result = balance;
    for (int month = 1; month <= months; month++) {
      result = result * (1 + monthlyInterestRate());
    }
    return Math.round(result) - balance;
  }
}
