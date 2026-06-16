package de.monticore.codeAdaption.cdconcretization.attributes.underspecified;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"DataClass"}, template = "${}")
public class DataClass {
  @Adapt(ref = {"DataClass.attribute"}, template = "${}")
  private Object attribute;

}
