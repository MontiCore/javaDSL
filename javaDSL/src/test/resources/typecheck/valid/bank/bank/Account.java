/* (c) https://github.com/MontiCore/monticore */
package bank;

import java.util.ArrayList;
import java.util.List;

/** An account with a balance in cents and a history of transactions. */
public abstract class Account {

  private final String id;

  protected long balance;

  private final List<Transaction> history = new ArrayList<>();

  protected Account(String id, long initialBalance) {
    this.id = id;
    this.balance = initialBalance;
  }

  public String getId() {
    return id;
  }

  public long getBalance() {
    return balance;
  }

  public List<Transaction> getHistory() {
    return history;
  }

  public void deposit(long amount) {
    if (amount <= 0) {
      throw new IllegalArgumentException("amount must be positive: " + amount);
    }
    balance += amount;
    history.add(new Transaction(TransactionKind.DEPOSIT, amount));
  }

  public void withdraw(long amount) throws InsufficientFundsException {
    if (amount > availableAmount()) {
      throw new InsufficientFundsException(this, amount);
    }
    balance -= amount;
    history.add(new Transaction(TransactionKind.WITHDRAWAL, amount));
  }

  /** The amount that can be withdrawn. */
  protected abstract long availableAmount();

  public abstract double monthlyInterestRate();

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + id + ": " + balance + "]";
  }
}
