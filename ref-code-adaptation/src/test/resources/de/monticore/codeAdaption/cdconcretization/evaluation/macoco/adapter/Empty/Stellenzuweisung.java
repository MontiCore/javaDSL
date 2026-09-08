package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Stellenzuweisung"}, template = "${}")
public class Stellenzuweisung {
  @Adapt(ref = {"Stellenzuweisung.erstellDatum"}, template = "${}")
  private ZonedDateTime erstellDatum;

  @Adapt(ref = {"Stellenzuweisung.startDatum"}, template = "${}")
  private Optional<ZonedDateTime> startDatum;

  @Adapt(ref = {"Stellenzuweisung.endDatum"}, template = "${}")
  private Optional<ZonedDateTime> endDatum;

  @Adapt(ref = {"Stellenzuweisung.wert"}, template = "${}")
  private long wert;

  @Adapt(ref = {"Stellenzuweisung.kennung"}, template = "${}")
  private String kennung;

  @Adapt(ref = {"Stellenzuweisung.status"}, template = "${}")
  private StellenzuweisungStatus status;

  @Adapt(ref = {"Stellenzuweisung.stellenumfang"}, template = "${}")
  private ZahlenWert stellenumfang;

  @Adapt(ref = {"Stellenzuweisung.istAktiv"}, template = "${}")
  private boolean istAktiv;

  @Adapt(ref = {"Stellenzuweisung.aenderungsdatum"}, template = "${}")
  private Optional<ZonedDateTime> aenderungsdatum;

}
