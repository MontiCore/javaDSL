package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Vertrag"}, template = "${}")
public class Vertrag {
  @Adapt(ref = {"Vertrag.vertragsBeginn"}, template = "${}")
  private ZonedDateTime vertragsBeginn;

  @Adapt(ref = {"Vertrag.vertragsEnde"}, template = "${}")
  private ZonedDateTime vertragsEnde;

  @Adapt(ref = {"Vertrag.aenderungsvertrag"}, template = "${}")
  private boolean aenderungsvertrag;

  @Adapt(ref = {"Vertrag.vertragsStatus"}, template = "${}")
  private String vertragsStatus;

  @Adapt(ref = {"Vertrag.kommentar"}, template = "${}")
  private List<String> kommentar;

  @Adapt(ref = {"Vertrag.planUmfang"}, template = "${}")
  private ZahlenWert planUmfang;

  @Adapt(ref = {"Vertrag.arbeitsstundenProWoche"}, template = "${}")
  private Optional<Long> arbeitsstundenProWoche;

  @Adapt(ref = {"Vertrag.kuendigungsschutz"}, template = "${}")
  private boolean kuendigungsschutz;

  @Adapt(ref = {"Vertrag.vertragsgrundlage"}, template = "${}")
  private Optional<String> vertragsgrundlage;

  @Adapt(ref = {"Vertrag.aktion"}, template = "${}")
  private Optional<String> aktion;

  @Adapt(ref = {"Vertrag.aktionsDatum"}, template = "${}")
  private Optional<ZonedDateTime> aktionsDatum;

}
