package de.monticore.codeAdaption.evaluation.testcase_17_fulfillment_platform.concrete;

import java.util.ArrayList;
import java.util.List;

public class AuditLog {
  private final List<String> entries = new ArrayList<>();

  public void append(String entry) {
    entries.add(entry);
  }

  public boolean containsOrder(String orderId) {
    for (String entry : entries) {
      if (entry.contains(orderId)) {
        return true;
      }
    }
    return false;
  }
}
