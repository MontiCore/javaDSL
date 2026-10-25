package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"CommandLog"}, template = "${}")
public class CommandLog {
  @Adapt(ref = {"CommandLog.timestamp"}, template = "${}")
  private ZonedDateTime timestamp;

  @Adapt(ref = {"CommandLog.diff"}, template = "${}")
  private List<String> diff;

  @Adapt(ref = {"CommandLog.affectedArea"}, template = "${}")
  private String affectedArea;

  @Adapt(ref = {"CommandLog.affectedObject"}, template = "${}")
  private String affectedObject;

}
