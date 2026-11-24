package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Planstelle"}, template = "${}")
public class Planstelle {
  @Adapt(ref = {"Planstelle.bezeichnung"}, template = "${}")
  private String bezeichnung;

  @Adapt(ref = {"Planstelle.minEntgeltgruppe"}, template = "${}")
  private Optional<String> minEntgeltgruppe;

  @Adapt(ref = {"Planstelle.minEntgeltstufe"}, template = "${}")
  private Optional<String> minEntgeltstufe;

  @Adapt(ref = {"Planstelle.maxEntgeltgruppe"}, template = "${}")
  private Optional<String> maxEntgeltgruppe;

  @Adapt(ref = {"Planstelle.maxEntgeltstufe"}, template = "${}")
  private Optional<String> maxEntgeltstufe;

  @Adapt(ref = {"Planstelle.verfuegbarVon"}, template = "${}")
  private Optional<ZonedDateTime> verfuegbarVon;

  @Adapt(ref = {"Planstelle.verfuegbarBis"}, template = "${}")
  private Optional<ZonedDateTime> verfuegbarBis;

  @Adapt(ref = {"Planstelle.kommentar"}, template = "${}")
  private Optional<String> kommentar;

  @Adapt(ref = {"Planstelle.planUmfang"}, template = "${}")
  private ZahlenWert planUmfang;

  @Adapt(ref = {"Planstelle.aktiv"}, template = "${}")
  private boolean aktiv;

}
