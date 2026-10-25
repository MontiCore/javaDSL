package de.monticore.codeAdaption.evaluation.testcase_15_composite_decorator_combined.concrete;

import java.util.ArrayList;
import java.util.List;

public class GroupNode implements Renderable {
  private final List<Renderable> nodes = new ArrayList<>();

  public void attach(Renderable node) {
    nodes.add(node);
  }

  public void detach(Renderable node) {
    nodes.remove(node);
  }

  @Override
  public String render(int depth) {
    StringBuilder result = new StringBuilder();
    for (Renderable node : nodes) {
      result.append(node.render(depth + 1));
    }
    return result.toString();
  }
}
