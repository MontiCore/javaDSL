/* (c) https://github.com/MontiCore/monticore */
package inventory;

import java.util.Objects;

/** An item of the inventory, quantities are tracked per item. */
public record Item(String sku, String name, Category category, int priceInCents) {

  public Item {
    Objects.requireNonNull(sku);
    if (priceInCents < 0) {
      throw new IllegalArgumentException("negative price");
    }
  }

  public Item withPrice(int newPrice) {
    return new Item(sku, name, category, newPrice);
  }

  public boolean isExpensive() {
    return priceInCents > category.getExpensiveThreshold();
  }
}
