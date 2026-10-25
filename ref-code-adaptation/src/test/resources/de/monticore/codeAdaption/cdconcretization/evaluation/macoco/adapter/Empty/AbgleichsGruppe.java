package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"AbgleichsGruppe"}, template = "${}")
public class AbgleichsGruppe {
  @Adapt(ref = {"AbgleichsGruppe.bezeichnung"}, template = "${}")
  private String bezeichnung;

}
