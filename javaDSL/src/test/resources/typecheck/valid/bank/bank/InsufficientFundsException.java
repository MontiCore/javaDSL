/* (c) https://github.com/MontiCore/monticore */
package bank;

public class InsufficientFundsException extends Exception {

  private final transient Account account;

  private final long requested;

  public InsufficientFundsException(Account account, long requested) {
    super("Insufficient funds on " + account.getId() + ": requested " + requested);
    this.account = account;
    this.requested = requested;
  }

  public Account getAccount() {
    return account;
  }

  public long getRequested() {
    return requested;
  }
}
