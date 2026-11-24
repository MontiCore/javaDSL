package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Stellentyp"}, template = "${}")
public class Stellentyp {
  @Adapt(ref = {"Stellentyp.entgeltgruppe"}, template = "${}")
  private Optional<String> entgeltgruppe;

  @Adapt(ref = {"Stellentyp.entgeltstufe"}, template = "${}")
  private Optional<String> entgeltstufe;

}
