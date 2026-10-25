package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"ZahlenWert"}, template = "${}")
public class ZahlenWert {
  @Adapt(ref = {"ZahlenWert.zahlenTyp"}, template = "${}")
  private ZahlenTyp zahlenTyp;

  @Adapt(ref = {"ZahlenWert.wert"}, template = "${}")
  private long wert;

}
