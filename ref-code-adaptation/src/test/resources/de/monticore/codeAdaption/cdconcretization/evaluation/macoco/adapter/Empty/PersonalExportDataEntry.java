package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"PersonalExportDataEntry"}, template = "${}")
public class PersonalExportDataEntry {
  @Adapt(ref = {"PersonalExportDataEntry.personalnummer"}, template = "${}")
  private String personalnummer;

  @Adapt(ref = {"PersonalExportDataEntry.vorsatzwort"}, template = "${}")
  private Optional<String> vorsatzwort;

  @Adapt(ref = {"PersonalExportDataEntry.nachname"}, template = "${}")
  private String nachname;

  @Adapt(ref = {"PersonalExportDataEntry.vorname"}, template = "${}")
  private String vorname;

  @Adapt(ref = {"PersonalExportDataEntry.von"}, template = "${}")
  private ZonedDateTime von;

  @Adapt(ref = {"PersonalExportDataEntry.bis"}, template = "${}")
  private ZonedDateTime bis;

  @Adapt(ref = {"PersonalExportDataEntry.pspElement"}, template = "${}")
  private String pspElement;

  @Adapt(ref = {"PersonalExportDataEntry.prozentsatz"}, template = "${}")
  private Long prozentsatz;

}
