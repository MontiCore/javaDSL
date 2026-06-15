package de.monticore.codeAdaption.evaluation.design_patterns.Composition;

import de.monticore.codeAdaption.utils.Adapt;

public class Composite implements Component {
  private final List<Component> componentList ;

  @Adapt(ref="execute",template="${}")
  public void execute() {
    for (Component component : componentList) {
      component.execute();
    }
  }

  @Adapt(ref="Component",template="add${}")
  public void addComponent(Component comp){
    componentList.add(comp);
  }

  @Adapt(ref="Component",template="remove${}")
  public void removeComponent(Component comp){
    componentList.remove(comp);
  }

  @Adapt(ref="Component",template="get${}")
  public List<Component> getComponent() {
    return componentList;
  }
}