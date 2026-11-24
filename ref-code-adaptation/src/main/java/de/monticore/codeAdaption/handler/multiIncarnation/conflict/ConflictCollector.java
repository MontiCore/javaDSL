package de.monticore.codeAdaption.handler.multiIncarnation.conflict;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Collect conflict messages. */
public final class ConflictCollector {

  private final List<String> conflicts = new ArrayList<>();

  public void conflict(String mapping, String kind, String detail) {
    conflicts.add(
        "["
            + mapping
            + "] "
            + kind
            + ": "
            + detail
            + ". Run with useConcretization=true or make the manual mapping explicit.");
  }

  public boolean hasConflicts() {
    return !conflicts.isEmpty();
  }

  public List<String> formattedConflicts() {
    List<String> formatted = new ArrayList<>(conflicts);
    Collections.sort(formatted);
    return formatted;
  }
}
