package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"DefaultFristen"}, template = "${}")
public class DefaultFristen {
  @Adapt(ref = {"DefaultFristen.finanzenStartDatum"}, template = "${}")
  private ZonedDateTime finanzenStartDatum;

  @Adapt(ref = {"DefaultFristen.finanzenFristinWeeks"}, template = "${}")
  private Long finanzenFristinWeeks;

  @Adapt(ref = {"DefaultFristen.personalStartDatum"}, template = "${}")
  private ZonedDateTime personalStartDatum;

  @Adapt(ref = {"DefaultFristen.personalFristinWeeks"}, template = "${}")
  private Long personalFristinWeeks;

}
