package de.monticore.codeAdaption.cdconcretization.attributes.foreach;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Builder"}, template = "${}")
public class Builder {
  @Adapt(ref = {"Builder.attribute"}, template = "${}")
  private Object attribute;

}
