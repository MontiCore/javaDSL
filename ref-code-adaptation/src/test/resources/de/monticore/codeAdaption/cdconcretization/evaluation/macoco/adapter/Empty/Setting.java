package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Setting"}, template = "${}")
public class Setting {
  @Adapt(ref = {"Setting.identifier"}, template = "${}")
  private String identifier;

}
