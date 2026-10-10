/* (c) https://github.com/MontiCore/monticore */
package bank;

public class Transaction {

  private final TransactionKind kind;

  private final long amount;

  public Transaction(TransactionKind kind, long amount) {
    this.kind = kind;
    this.amount = amount;
  }

  public TransactionKind getKind() {
    return kind;
  }

  public long getAmount() {
    return amount;
  }

  /** The change of the balance caused by this transaction. */
  public long signedAmount() {
    return kind.getSign() * amount;
  }
}
