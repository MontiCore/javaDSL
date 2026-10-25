package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"PersonalExportReport"}, template = "${}")
public class PersonalExportReport extends EventReport {
  @Adapt(ref = {"PersonalExportReport.dataId"}, template = "${}")
  private Long dataId;

  @Adapt(ref = {"PersonalExportReport.datenStart"}, template = "${}")
  private ZonedDateTime datenStart;

  @Adapt(ref = {"PersonalExportReport.datenEnd"}, template = "${}")
  private ZonedDateTime datenEnd;

  @Adapt(ref = {"PersonalExportReport.countData"}, template = "${}")
  private Long countData;

  @Adapt(ref = {"PersonalExportReport.countError"}, template = "${}")
  private Long countError;

  @Adapt(ref = {"PersonalExportReport.exportStatus"}, template = "${}")
  private EventStatus exportStatus;

}
