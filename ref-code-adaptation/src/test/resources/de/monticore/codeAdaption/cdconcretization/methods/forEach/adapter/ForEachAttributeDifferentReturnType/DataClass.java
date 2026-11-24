package de.monticore.codeAdaption.cdconcretization.methods.foreach.adapter.foreachattributedifferentreturntype;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"DataClass"}, template = "${}")
public class DataClass {
  @Adapt(ref = {"DataClass.attribute"}, template = "${}")
  private Object attribute;

  @Adapt(ref = {"DataClass.isAttributeModified"}, template = "${}")
  public boolean isAttributeModified() {
    return false;
  }

}
