package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"StrafsteuerSperrDatum"}, template = "${}")
public class StrafsteuerSperrDatum {
  @Adapt(ref = {"StrafsteuerSperrDatum.sperrDatum"}, template = "${}")
  private ZonedDateTime sperrDatum;

}
