package DesignPatterns;

import de.monticore.codeAdaption.utils.Adapt;
import java.util.ArrayList;
import java.util.List;

public class Composite implements Component {
  private final List<Component> componentList = new ArrayList<>();

  @Adapt(ref = "Component.execute()", template = "${}")
  public void execute() {
    for (Component component : componentList) {
      component.execute();
    }
  }

  @Adapt(ref = "Component", template = "add${}")
  public void addComponent(Component component) {
    componentList.add(component);
  }

  @Adapt(ref = "Component", template = "remove${}")
  public void removeComponent(Component component) {
    componentList.remove(component);
  }

  @Adapt(ref = "Component", template = "get${}")
  public List<Component> getComponent() {
    return componentList;
  }
}
