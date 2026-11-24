package de.monticore.codeAdaption.cdconcretization.methods.foreach.adapter.foreachattributemultipleparameters;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"DataClass"}, template = "${}")
public class DataClass {
  @Adapt(ref = {"DataClass.attribute"}, template = "${}")
  private Object attribute;

  @Adapt(ref = {"DataClass.updateAttribute"}, template = "${}")
  public void updateAttribute(Object attribute, String changedBy) {
  }

}
