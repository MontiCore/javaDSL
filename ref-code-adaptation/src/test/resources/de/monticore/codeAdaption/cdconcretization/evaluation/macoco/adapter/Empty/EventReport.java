package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"EventReport"}, template = "${}")
public class EventReport {
  @Adapt(ref = {"EventReport.eventStart"}, template = "${}")
  private ZonedDateTime eventStart;

  @Adapt(ref = {"EventReport.eventEnd"}, template = "${}")
  private Optional<ZonedDateTime> eventEnd;

  @Adapt(ref = {"EventReport.status"}, template = "${}")
  private EventStatus status;

  @Adapt(ref = {"EventReport.message"}, template = "${}")
  private String message;

}
