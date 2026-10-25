package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"GroupedAccessPolicy"}, template = "${}")
public class GroupedAccessPolicy {
  @Adapt(ref = {"GroupedAccessPolicy.name"}, template = "${}")
  private String name;

}
