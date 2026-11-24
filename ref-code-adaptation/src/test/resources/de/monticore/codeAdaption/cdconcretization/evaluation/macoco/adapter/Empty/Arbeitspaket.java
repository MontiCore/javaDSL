package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Arbeitspaket"}, template = "${}")
public class Arbeitspaket {
  @Adapt(ref = {"Arbeitspaket.nummer"}, template = "${}")
  private String nummer;

  @Adapt(ref = {"Arbeitspaket.name"}, template = "${}")
  private String name;

  @Adapt(ref = {"Arbeitspaket.beschreibung"}, template = "${}")
  private Optional<String> beschreibung;

  @Adapt(ref = {"Arbeitspaket.beginnDatum"}, template = "${}")
  private Optional<ZonedDateTime> beginnDatum;

  @Adapt(ref = {"Arbeitspaket.endeDatum"}, template = "${}")
  private Optional<ZonedDateTime> endeDatum;

  @Adapt(ref = {"Arbeitspaket.pMs"}, template = "${}")
  private Optional<Long> pMs;

  @Adapt(ref = {"Arbeitspaket.stunden"}, template = "${}")
  private Optional<Long> stunden;

  @Adapt(ref = {"Arbeitspaket.aenderungsdatum"}, template = "${}")
  private Optional<ZonedDateTime> aenderungsdatum;

}
