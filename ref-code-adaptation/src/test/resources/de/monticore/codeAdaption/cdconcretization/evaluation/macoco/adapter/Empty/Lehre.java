package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Lehre"}, template = "${}")
public class Lehre extends Projekt {
  @Adapt(ref = {"Lehre.stunden"}, template = "${}")
  private Optional<Long> stunden;

}
