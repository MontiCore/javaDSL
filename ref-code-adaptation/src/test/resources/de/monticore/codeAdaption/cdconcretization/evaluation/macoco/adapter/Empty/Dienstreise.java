package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Dienstreise"}, template = "${}")
public class Dienstreise extends Abwesenheit {
  @Adapt(ref = {"Dienstreise.reisenummer"}, template = "${}")
  private Optional<String> reisenummer;

  @Adapt(ref = {"Dienstreise.dienstreisegrund"}, template = "${}")
  private Optional<String> dienstreisegrund;

  @Adapt(ref = {"Dienstreise.reiseziel"}, template = "${}")
  private Optional<String> reiseziel;

  @Adapt(ref = {"Dienstreise.gesamtkosten"}, template = "${}")
  private Optional<Long> gesamtkosten;

  @Adapt(ref = {"Dienstreise.status"}, template = "${}")
  private Optional<String> status;

  @Adapt(ref = {"Dienstreise.statusText"}, template = "${}")
  private Optional<String> statusText;

}
