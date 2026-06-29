package Concrete;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Project"}, template = "${}")
public class Project extends ProjectTOP {

  @Adapt(ref = {"Task"}, template = "replace${}")
  public void replaceTask(Task replacement) {
    this.tasks = replacement;
  }
}
