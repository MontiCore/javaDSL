package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"PersonalExportReportEntry"}, template = "${}")
public class PersonalExportReportEntry extends EventReportEntry {
  @Adapt(ref = {"PersonalExportReportEntry.vorsatzwort"}, template = "${}")
  private Optional<String> vorsatzwort;

  @Adapt(ref = {"PersonalExportReportEntry.vorname"}, template = "${}")
  private String vorname;

  @Adapt(ref = {"PersonalExportReportEntry.nachname"}, template = "${}")
  private String nachname;

  @Adapt(ref = {"PersonalExportReportEntry.beschaeftigungsArt"}, template = "${}")
  private BeschaeftigungsArt beschaeftigungsArt;

  @Adapt(ref = {"PersonalExportReportEntry.monat"}, template = "${}")
  private ZonedDateTime monat;

}
