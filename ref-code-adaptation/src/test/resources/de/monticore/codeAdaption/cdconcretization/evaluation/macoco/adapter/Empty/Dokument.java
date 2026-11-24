package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Dokument"}, template = "${}")
public class Dokument {
  @Adapt(ref = {"Dokument.groupName"}, template = "${}")
  private DokumentGroup groupName;

  @Adapt(ref = {"Dokument.associationId"}, template = "${}")
  private Long associationId;

  @Adapt(ref = {"Dokument.datum"}, template = "${}")
  private ZonedDateTime datum;

  @Adapt(ref = {"Dokument.bezeichnung"}, template = "${}")
  private String bezeichnung;

  @Adapt(ref = {"Dokument.dateiFormat"}, template = "${}")
  private String dateiFormat;

  @Adapt(ref = {"Dokument.fileSize"}, template = "${}")
  private Long fileSize;

  @Adapt(ref = {"Dokument.anmerkung"}, template = "${}")
  private Optional<String> anmerkung;

  @Adapt(ref = {"Dokument.istAktiv"}, template = "${}")
  private boolean istAktiv;

}
