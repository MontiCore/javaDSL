package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Nebentaetigkeit"}, template = "${}")
public class Nebentaetigkeit {
  @Adapt(ref = {"Nebentaetigkeit.von"}, template = "${}")
  private Optional<ZonedDateTime> von;

  @Adapt(ref = {"Nebentaetigkeit.bis"}, template = "${}")
  private Optional<ZonedDateTime> bis;

  @Adapt(ref = {"Nebentaetigkeit.umfang"}, template = "${}")
  private ZahlenWert umfang;

  @Adapt(ref = {"Nebentaetigkeit.arbeitgeber"}, template = "${}")
  private Optional<String> arbeitgeber;

  @Adapt(ref = {"Nebentaetigkeit.taetigkeitsbereich"}, template = "${}")
  private Optional<String> taetigkeitsbereich;

}
