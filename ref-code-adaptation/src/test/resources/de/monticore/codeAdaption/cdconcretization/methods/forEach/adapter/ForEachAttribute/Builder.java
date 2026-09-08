package de.monticore.codeAdaption.cdconcretization.methods.foreach.adapter.foreachattribute;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Builder"}, template = "${}")
public class Builder {
  @Adapt(ref = {"Builder.attribute"}, template = "${}")
  public Builder attribute(Object attribute) {
    return null;
  }

}
