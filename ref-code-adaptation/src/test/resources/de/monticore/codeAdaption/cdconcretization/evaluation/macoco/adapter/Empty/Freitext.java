package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Freitext"}, template = "${}")
public class Freitext {
  @Adapt(ref = {"Freitext.erstellDatum"}, template = "${}")
  private Optional<ZonedDateTime> erstellDatum;

  @Adapt(ref = {"Freitext.bearbeitetDatum"}, template = "${}")
  private Optional<ZonedDateTime> bearbeitetDatum;

  @Adapt(ref = {"Freitext.text"}, template = "${}")
  private Optional<String> text;

}
