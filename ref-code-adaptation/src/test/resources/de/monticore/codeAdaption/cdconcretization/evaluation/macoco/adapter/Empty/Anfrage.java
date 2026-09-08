package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Anfrage"}, template = "${}")
public class Anfrage {
  @Adapt(ref = {"Anfrage.ikz"}, template = "${}")
  private String ikz;

  @Adapt(ref = {"Anfrage.bezeichner"}, template = "${}")
  private String bezeichner;

  @Adapt(ref = {"Anfrage.letzteAnfrageErfolgreich"}, template = "${}")
  private boolean letzteAnfrageErfolgreich;

}
