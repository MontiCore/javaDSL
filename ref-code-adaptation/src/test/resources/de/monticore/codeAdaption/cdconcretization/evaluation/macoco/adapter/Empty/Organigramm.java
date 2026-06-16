package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Organigramm"}, template = "${}")
public class Organigramm {
  @Adapt(ref = {"Organigramm.istStabstelle"}, template = "${}")
  private boolean istStabstelle;

  @Adapt(ref = {"Organigramm.istAktiv"}, template = "${}")
  private boolean istAktiv;

  @Adapt(ref = {"Organigramm.bezeichnung"}, template = "${}")
  private String bezeichnung;

}
