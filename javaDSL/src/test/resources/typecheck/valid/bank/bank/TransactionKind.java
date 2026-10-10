/* (c) https://github.com/MontiCore/monticore */
package bank;

public enum TransactionKind {

  DEPOSIT(1, "D"),
  WITHDRAWAL(-1, "W");

  private final int sign;

  private final String shortName;

  TransactionKind(int sign, String shortName) {
    this.sign = sign;
    this.shortName = shortName;
  }

  public int getSign() {
    return sign;
  }

  public String getShortName() {
    return shortName;
  }

  public static TransactionKind fromShortName(String shortName) {
    for (TransactionKind kind : values()) {
      if (kind.shortName.equals(shortName)) {
        return kind;
      }
    }
    throw new IllegalArgumentException(shortName);
  }
}
