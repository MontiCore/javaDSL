/* (c) https://github.com/MontiCore/monticore */
package bank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Bank {

  private final Map<String, Account> accounts = new HashMap<>();

  private final List<AccountListener> listeners = new ArrayList<>();

  public void open(Account account) {
    if (accounts.containsKey(account.getId())) {
      throw new IllegalStateException("duplicate account " + account.getId());
    }
    accounts.put(account.getId(), account);
  }

  public Optional<Account> find(String id) {
    return Optional.ofNullable(accounts.get(id));
  }

  public void addListener(AccountListener listener) {
    listeners.add(listener);
  }

  public boolean transfer(String fromId, String toId, long amount) {
    Account from = accounts.get(fromId);
    Account to = accounts.get(toId);
    if (from == null || to == null) {
      return false;
    }
    try {
      from.withdraw(amount);
      to.deposit(amount);
      for (AccountListener listener : listeners) {
        listener.transferred(from, to, amount);
      }
      return true;
    }
    catch (InsufficientFundsException e) {
      for (AccountListener listener : listeners) {
        listener.failed(e);
      }
      return false;
    }
  }

  public long totalBalance() {
    long total = 0;
    for (Account account : accounts.values()) {
      total += account.getBalance();
    }
    return total;
  }

  public int countTransactions(TransactionKind kind) {
    int count = 0;
    for (Account account : accounts.values()) {
      for (Transaction transaction : account.getHistory()) {
        if (transaction.getKind() == kind) {
          count++;
        }
      }
    }
    return count;
  }

  public String describe(Account account) {
    String type;
    if (account instanceof SavingsAccount) {
      type = "savings";
    }
    else if (account instanceof CheckingAccount) {
      type = "checking";
    }
    else {
      type = "unknown";
    }
    return type + " account " + account.getId();
  }

  public static String format(long cents) {
    long euros = cents / 100;
    int rest = (int) Math.abs(cents % 100);
    return euros + "," + (rest < 10 ? "0" + rest : String.valueOf(rest)) + " EUR";
  }

  public static void main(String[] args) {
    Bank bank = new Bank();
    bank.open(new CheckingAccount("C-1", 10_000, 5_000));
    SavingsAccount savings = new SavingsAccount("S-1");
    savings.setRate(0.03);
    bank.open(savings);
    bank.addListener((from, to, amount) ->
        System.out.println(from.getId() + " -> " + to.getId() + ": " + format(amount)));
    bank.addListener(new AccountListener() {
      private int transfers;

      @Override
      public void transferred(Account from, Account to, long amount) {
        transfers++;
      }
    });
    bank.transfer("C-1", "S-1", 12_000);
    switch (bank.countTransactions(TransactionKind.DEPOSIT)) {
      case 0:
        System.out.println("no deposits");
        break;
      default:
        System.out.println(format(bank.totalBalance()));
    }
    bank.find("S-1").ifPresent(account -> System.out.println(bank.describe(account)));
  }
}
