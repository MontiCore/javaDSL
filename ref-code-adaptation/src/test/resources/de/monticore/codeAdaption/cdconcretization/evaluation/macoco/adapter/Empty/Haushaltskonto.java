package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Haushaltskonto"}, template = "${}")
public class Haushaltskonto extends Konto {
  @Adapt(ref = {"Haushaltskonto.startDatum"}, template = "${}")
  private Optional<ZonedDateTime> startDatum;

  @Adapt(ref = {"Haushaltskonto.endDatum"}, template = "${}")
  private Optional<ZonedDateTime> endDatum;

}
