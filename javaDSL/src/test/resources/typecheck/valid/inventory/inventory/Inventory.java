/* (c) https://github.com/MontiCore/monticore */
package inventory;

import static inventory.Flags.ON_SALE;
import static java.lang.Math.max;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Inventory {

  private final Map<String, Item> items = new LinkedHashMap<>();

  private final Map<String, Integer> quantities = new LinkedHashMap<>();

  private final Map<String, Integer> flags = new LinkedHashMap<>();

  public void add(Item item, int quantity) {
    items.put(item.sku(), item);
    quantities.merge(item.sku(), quantity, Integer::sum);
    flags.putIfAbsent(item.sku(), 0);
  }

  public int remove(String sku, int quantity) {
    int available = quantities.getOrDefault(sku, 0);
    int removed = Math.min(available, quantity);
    quantities.put(sku, max(0, available - removed));
    return removed;
  }

  public void markOnSale(String sku) {
    flags.computeIfPresent(sku, (key, value) -> Flags.set(value, ON_SALE));
  }

  public long totalValue() {
    long total = 0;
    for (var entry : quantities.entrySet()) {
      var item = items.get(entry.getKey());
      total += (long) item.priceInCents() * entry.getValue();
    }
    return total;
  }

  public List<Item> sortedByPrice() {
    var result = new ArrayList<>(items.values());
    result.sort(Comparator.comparingInt(Item::priceInCents).reversed());
    return result;
  }

  public String shelf(Item item) {
    String shelf;
    switch (item.category()) {
      case FOOD:
        shelf = "cold storage";
        break;
      case TOOLS:
        shelf = "hall " + (item.sku().charAt(0) - 'A' + 1);
        break;
      default:
        shelf = "main";
    }
    return shelf;
  }

  /** Finds the first pair of items whose prices sum up to the budget. */
  public String findPair(int budget) {
    List<Item> all = new ArrayList<>(items.values());
    String result = null;
    search:
    for (int i = 0; i < all.size(); i++) {
      for (int j = i + 1; j < all.size(); j++) {
        if (all.get(i).priceInCents() + all.get(j).priceInCents() == budget) {
          result = all.get(i).name() + " & " + all.get(j).name();
          break search;
        }
      }
    }
    return result;
  }

  public String report() {
    class Line {
      final String text;

      Line(Item item, int quantity) {
        this.text = String.format("%-10s %5d", item.name(), quantity);
      }
    }
    var lines = new ArrayList<Line>();
    items.forEach((sku, item) -> lines.add(new Line(item, quantities.get(sku))));
    String header = """
        Name       Count
        ----------------
        """;
    StringBuilder report = new StringBuilder(header);
    for (Line line : lines) {
      report.append(line.text).append('\n');
    }
    return report.toString();
  }

  public static void main(String[] args) {
    Inventory inventory = new Inventory();
    inventory.add(new Item("A-1", "Hammer", Category.TOOLS, 1_299), 5);
    inventory.add(new Item("F-7", "Apples", Category.FOOD, 199), 40);
    inventory.markOnSale("F-7");
    System.out.println(inventory.report());
    System.out.println(inventory.totalValue() + " " + inventory.findPair(1_498));
    for (Item item : inventory.sortedByPrice()) {
      System.out.println(item.name() + ": " + inventory.shelf(item) + " "
          + (item.isExpensive() ? "expensive" : "cheap") + " "
          + (item.category().isPerishable() ? "perishable" : ""));
    }
  }
}
