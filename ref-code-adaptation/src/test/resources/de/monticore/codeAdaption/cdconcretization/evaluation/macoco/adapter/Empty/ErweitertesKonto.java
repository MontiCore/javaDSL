package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"ErweitertesKonto"}, template = "${}")
public class ErweitertesKonto extends Konto {
  @Adapt(ref = {"ErweitertesKonto.startDatum"}, template = "${}")
  private Optional<ZonedDateTime> startDatum;

  @Adapt(ref = {"ErweitertesKonto.endDatum"}, template = "${}")
  private Optional<ZonedDateTime> endDatum;

  @Adapt(ref = {"ErweitertesKonto.bewilligungsDatum"}, template = "${}")
  private Optional<ZonedDateTime> bewilligungsDatum;

  @Adapt(ref = {"ErweitertesKonto.verlaengertBisDatum"}, template = "${}")
  private Optional<ZonedDateTime> verlaengertBisDatum;

  @Adapt(ref = {"ErweitertesKonto.aufstockungsDatum"}, template = "${}")
  private Optional<ZonedDateTime> aufstockungsDatum;

  @Adapt(ref = {"ErweitertesKonto.foerderquote"}, template = "${}")
  private Optional<Long> foerderquote;

  @Adapt(ref = {"ErweitertesKonto.aktenzeichen"}, template = "${}")
  private Optional<String> aktenzeichen;

  @Adapt(ref = {"ErweitertesKonto.finanzierteKomplettsumme"}, template = "${}")
  private Optional<Long> finanzierteKomplettsumme;

  @Adapt(ref = {"ErweitertesKonto.referenzSponsor"}, template = "${}")
  private Optional<String> referenzSponsor;

  @Adapt(ref = {"ErweitertesKonto.hatProgrammpauschale"}, template = "${}")
  private boolean hatProgrammpauschale;

}
