package de.monticore.codeAdaption.cdconcretization.methods.foreach.adapter.foreachtypesamereturntype;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Mill"}, template = "${}")
public class Mill {
  @Adapt(ref = {"Mill.builder"}, template = "${}")
  public Builder builder() {
    return null;
  }

}
