package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Kostenstelle"}, template = "${}")
public class Kostenstelle {
  @Adapt(ref = {"Kostenstelle.verbuchungsBeginn"}, template = "${}")
  private ZonedDateTime verbuchungsBeginn;

  @Adapt(ref = {"Kostenstelle.verbuchungsEnde"}, template = "${}")
  private ZonedDateTime verbuchungsEnde;

  @Adapt(ref = {"Kostenstelle.bezeichnung"}, template = "${}")
  private KostenstelleBezeichnung bezeichnung;

  @Adapt(ref = {"Kostenstelle.kommentar"}, template = "${}")
  private List<String> kommentar;

  @Adapt(ref = {"Kostenstelle.beschaeftigungsUmfang"}, template = "${}")
  private ZahlenWert beschaeftigungsUmfang;

  @Adapt(ref = {"Kostenstelle.buchungenNeuErzeugen"}, template = "${}")
  private boolean buchungenNeuErzeugen;

  @Adapt(ref = {"Kostenstelle.buchungenLoeschen"}, template = "${}")
  private boolean buchungenLoeschen;

  @Adapt(ref = {"Kostenstelle.gesperrt"}, template = "${}")
  private boolean gesperrt;

}
