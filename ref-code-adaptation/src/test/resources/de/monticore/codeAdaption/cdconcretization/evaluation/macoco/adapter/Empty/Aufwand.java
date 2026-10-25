package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Aufwand"}, template = "${}")
public class Aufwand {
  @Adapt(ref = {"Aufwand.pM"}, template = "${}")
  private long pM;

  @Adapt(ref = {"Aufwand.umfang"}, template = "${}")
  private ZahlenWert umfang;

  @Adapt(ref = {"Aufwand.laufzeitVon"}, template = "${}")
  private ZonedDateTime laufzeitVon;

  @Adapt(ref = {"Aufwand.laufzeitBis"}, template = "${}")
  private ZonedDateTime laufzeitBis;

  @Adapt(ref = {"Aufwand.aenderungsdatum"}, template = "${}")
  private Optional<ZonedDateTime> aenderungsdatum;

}
