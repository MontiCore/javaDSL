/* (c) https://github.com/MontiCore/monticore */
package inventory;

public enum Category {

  FOOD(1_000) {
    @Override
    public boolean isPerishable() {
      return true;
    }
  },
  TOOLS(10_000),
  BOOKS(3_000);

  private final int expensiveThreshold;

  Category(int expensiveThreshold) {
    this.expensiveThreshold = expensiveThreshold;
  }

  public int getExpensiveThreshold() {
    return expensiveThreshold;
  }

  public boolean isPerishable() {
    return false;
  }
}
