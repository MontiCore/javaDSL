package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"EventReportEntry"}, template = "${}")
public class EventReportEntry {
  @Adapt(ref = {"EventReportEntry.type"}, template = "${}")
  private EventType type;

  @Adapt(ref = {"EventReportEntry.targetId"}, template = "${}")
  private Optional<Long> targetId;

  @Adapt(ref = {"EventReportEntry.count"}, template = "${}")
  private Optional<Integer> count;

  @Adapt(ref = {"EventReportEntry.message"}, template = "${}")
  private String message;

  @Adapt(ref = {"EventReportEntry.status"}, template = "${}")
  private EventStatus status;

}
