package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Sonderurlaub"}, template = "${}")
public class Sonderurlaub {
  @Adapt(ref = {"Sonderurlaub.datumVon"}, template = "${}")
  private ZonedDateTime datumVon;

  @Adapt(ref = {"Sonderurlaub.datumBis"}, template = "${}")
  private ZonedDateTime datumBis;

  @Adapt(ref = {"Sonderurlaub.anzahl"}, template = "${}")
  private long anzahl;

}
