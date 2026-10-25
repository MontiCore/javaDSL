package de.monticore.codeAdaption.cdconcretization.attributes.foreach.adapter.foreachattributedifferentnameclassmi;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Builder"}, template = "${}")
public class Builder {
  @Adapt(ref = {"Builder.attrCopy"}, template = "${}")
  private Object attrCopy;

}
