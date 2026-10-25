package de.monticore.codeAdaption.evaluation.testcase_15_composite_decorator_combined.adapter;

import de.monticore.codeAdaption.utils.Adapt;
import java.util.ArrayList;
import java.util.List;

@Adapt(ref = "Composite", template = "${}")
public class Composite implements Component {
  @Adapt(ref = "Composite.children", template = "${}")
  private List<Component> children = new ArrayList<>();

  @Adapt(ref = "Composite.add", template = "${}")
  public void add(Component component) {
    children.add(component);
  }

  @Adapt(ref = "Composite.remove", template = "${}")
  public void remove(Component component) {
    children.remove(component);
  }

  @Override
  @Adapt(ref = "Component.operation", template = "${}")
  public String operation() {
    StringBuilder builder = new StringBuilder();
    for (Component child : children) {
      builder.append(child.operation());
    }
    return builder.toString();
  }
}
