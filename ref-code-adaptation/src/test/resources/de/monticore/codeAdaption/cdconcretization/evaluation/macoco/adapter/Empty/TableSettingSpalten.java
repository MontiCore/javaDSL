package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"TableSettingSpalten"}, template = "${}")
public class TableSettingSpalten {
  @Adapt(ref = {"TableSettingSpalten.name"}, template = "${}")
  private String name;

  @Adapt(ref = {"TableSettingSpalten.istAktiv"}, template = "${}")
  private boolean istAktiv;

  @Adapt(ref = {"TableSettingSpalten.breite"}, template = "${}")
  private long breite;

}
