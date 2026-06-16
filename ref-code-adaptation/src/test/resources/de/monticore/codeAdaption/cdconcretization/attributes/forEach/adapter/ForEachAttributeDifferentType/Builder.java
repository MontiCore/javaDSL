package de.monticore.codeAdaption.cdconcretization.attributes.foreach.adapter.foreachattributedifferenttype;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Builder"}, template = "${}")
public class Builder {
  @Adapt(ref = {"Builder.hasModifiedAttribute"}, template = "${}")
  private boolean hasModifiedAttribute;

}
