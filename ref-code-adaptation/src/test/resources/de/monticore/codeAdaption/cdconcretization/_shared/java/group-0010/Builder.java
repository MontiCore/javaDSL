package de.monticore.codeAdaption.cdconcretization.attributes.foreach.adapter.foreachattribute;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Builder"}, template = "${}")
public class Builder {
  @Adapt(ref = {"Builder.attribute"}, template = "${}")
  private Object attribute;

}
