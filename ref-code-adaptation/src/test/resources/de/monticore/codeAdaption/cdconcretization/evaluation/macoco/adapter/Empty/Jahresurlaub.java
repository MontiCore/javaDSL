package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Jahresurlaub"}, template = "${}")
public class Jahresurlaub {
  @Adapt(ref = {"Jahresurlaub.jahr"}, template = "${}")
  private int jahr;

  @Adapt(ref = {"Jahresurlaub.tageAnzahl"}, template = "${}")
  private long tageAnzahl;

  @Adapt(ref = {"Jahresurlaub.stundenAnzahl"}, template = "${}")
  private Optional<Long> stundenAnzahl;

  @Adapt(ref = {"Jahresurlaub.istManuellerEintrag"}, template = "${}")
  private boolean istManuellerEintrag;

}
