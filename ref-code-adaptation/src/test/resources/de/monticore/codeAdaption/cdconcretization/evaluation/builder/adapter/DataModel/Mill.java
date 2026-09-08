package de.monticore.codeAdaption.cdconcretization.evaluation.builder.adapter.datamodel;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Mill"}, template = "${}")
public class Mill {
  @Adapt(ref = {"Mill.dataClassBuilder"}, template = "${}")
  public DataClassBuilder dataClassBuilder() {
    return null;
  }

}
