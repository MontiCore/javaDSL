package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Konto"}, template = "${}")
public class Konto {
  @Adapt(ref = {"Konto.name"}, template = "${}")
  private String name;

  @Adapt(ref = {"Konto.pspElement"}, template = "${}")
  private Optional<String> pspElement;

  @Adapt(ref = {"Konto.kontotyp"}, template = "${}")
  private Optional<String> kontotyp;

  @Adapt(ref = {"Konto.sapDatum"}, template = "${}")
  private Optional<ZonedDateTime> sapDatum;

  @Adapt(ref = {"Konto.internesAktenzeichen"}, template = "${}")
  private Optional<String> internesAktenzeichen;

  @Adapt(ref = {"Konto.istPlanKonto"}, template = "${}")
  private boolean istPlanKonto;

  @Adapt(ref = {"Konto.istVerbuchungsKonto"}, template = "${}")
  private boolean istVerbuchungsKonto;

  @Adapt(ref = {"Konto.istAktiv"}, template = "${}")
  private boolean istAktiv;

  @Adapt(ref = {"Konto.vergabeVerordnung"}, template = "${}")
  private Optional<String> vergabeVerordnung;

  @Adapt(ref = {"Konto.farbe"}, template = "${}")
  private Optional<String> farbe;

  @Adapt(ref = {"Konto.aenderungsdatum"}, template = "${}")
  private Optional<ZonedDateTime> aenderungsdatum;

  @Adapt(ref = {"Konto.optStartDatum"}, template = "${}")
  private Optional<ZonedDateTime> optStartDatum;

  @Adapt(ref = {"Konto.optEndDatum"}, template = "${}")
  private Optional<ZonedDateTime> optEndDatum;

  @Adapt(ref = {"Konto.gueltigerGeschaeftsvorgang"}, template = "${}")
  private Geschaeftsvorgang gueltigerGeschaeftsvorgang;

}
