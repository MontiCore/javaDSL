import de.monticore.codeAdaption.utils.Adapt;

@Adapt(template = "Account", value = {"Account"})
class Account {
  @Adapt(template = "balance", value = {"balance"})
  private int balance;

  @Adapt(template = "getBalance", value = {"getBalance"})
  int getBalance() {
    return balance;
  }
}
