package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Legende"}, template = "${}")
public class Legende {
  @Adapt(ref = {"Legende.type"}, template = "${}")
  private LegendeTyp type;

  @Adapt(ref = {"Legende.expression"}, template = "${}")
  private String expression;

  @Adapt(ref = {"Legende.description"}, template = "${}")
  private String description;

}
