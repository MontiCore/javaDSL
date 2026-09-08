package Concrete;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Task"}, template = "${}")
public class Task extends TaskTOP {

  @Adapt(ref = {"Project"}, template = "moveTo${}")
  public void moveToProject(Project replacement) {
    this.project = replacement;
  }
}
