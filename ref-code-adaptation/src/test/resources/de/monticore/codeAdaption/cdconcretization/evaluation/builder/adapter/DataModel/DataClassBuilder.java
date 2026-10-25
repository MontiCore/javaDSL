package de.monticore.codeAdaption.cdconcretization.evaluation.builder.adapter.datamodel;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"DataClassBuilder"}, template = "${}")
public class DataClassBuilder {
  @Adapt(ref = {"DataClassBuilder.attribute"}, template = "${}")
  private Object attribute;

  @Adapt(ref = {"DataClassBuilder.attribute"}, template = "${}")
  public DataClassBuilder attribute(Object attribute) {
    return null;
  }

  @Adapt(ref = {"DataClassBuilder.build"}, template = "${}")
  public DataClass build() {
    return null;
  }

}
