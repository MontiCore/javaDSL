/* (c) https://github.com/MontiCore/monticore */
package bank;

public interface AccountListener {

  void transferred(Account from, Account to, long amount);

  default void failed(InsufficientFundsException exception) {
    System.err.println(exception.getMessage());
  }
}
