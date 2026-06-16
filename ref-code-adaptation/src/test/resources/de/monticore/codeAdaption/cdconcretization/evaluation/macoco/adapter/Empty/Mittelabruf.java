package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Mittelabruf"}, template = "${}")
public class Mittelabruf extends Buchungseintrag {
  @Adapt(ref = {"Mittelabruf.abrufdatum"}, template = "${}")
  private ZonedDateTime abrufdatum;

  @Adapt(ref = {"Mittelabruf.status"}, template = "${}")
  private MittelabrufStatus status;

  @Adapt(ref = {"Mittelabruf.zeitraum"}, template = "${}")
  private String zeitraum;

  @Adapt(ref = {"Mittelabruf.pauschaleManuell"}, template = "${}")
  private Optional<Long> pauschaleManuell;

}
