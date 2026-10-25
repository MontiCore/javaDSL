package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Arbeitstage"}, template = "${}")
public class Arbeitstage {
  @Adapt(ref = {"Arbeitstage.guiltigAb"}, template = "${}")
  private ZonedDateTime guiltigAb;

  @Adapt(ref = {"Arbeitstage.guiltigBis"}, template = "${}")
  private ZonedDateTime guiltigBis;

  @Adapt(ref = {"Arbeitstage.wochenTage"}, template = "${}")
  private List<Integer> wochenTage;

}
