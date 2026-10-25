package de.monticore.codeAdaption.cdconcretization.evaluation.getter-setter;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"DataClass"}, template = "${}")
public class DataClass {
  @Adapt(ref = {"DataClass.attribute"}, template = "${}")
  private Object attribute;

  @Adapt(ref = {"DataClass.getAttribute"}, template = "${}")
  public Object getAttribute() {
    return null;
  }

}
