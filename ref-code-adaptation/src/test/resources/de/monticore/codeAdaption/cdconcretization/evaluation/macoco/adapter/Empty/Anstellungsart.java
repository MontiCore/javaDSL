package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Anstellungsart"}, template = "${}")
public class Anstellungsart {
  @Adapt(ref = {"Anstellungsart.beschArt"}, template = "${}")
  private BeschaeftigungsArt beschArt;

}
