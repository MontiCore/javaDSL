package de.monticore.codeAdaption.cdconcretization.attributes.foreach.adapter.foreachattribute;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"DataClass"}, template = "${}")
public class DataClass {
  @Adapt(ref = {"DataClass.attribute"}, template = "${}")
  private Object attribute;

}
