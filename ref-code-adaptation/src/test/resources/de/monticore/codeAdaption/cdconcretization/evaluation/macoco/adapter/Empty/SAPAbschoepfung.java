package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"SAPAbschoepfung"}, template = "${}")
public class SAPAbschoepfung {
  @Adapt(ref = {"SAPAbschoepfung.betragCent"}, template = "${}")
  private Long betragCent;

  @Adapt(ref = {"SAPAbschoepfung.year"}, template = "${}")
  private ZonedDateTime year;

}
