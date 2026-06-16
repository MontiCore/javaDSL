package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Industrieprojekt"}, template = "${}")
public class Industrieprojekt extends ErweitertesKonto {
  @Adapt(ref = {"Industrieprojekt.auftraggeber"}, template = "${}")
  private Optional<String> auftraggeber;

  @Adapt(ref = {"Industrieprojekt.istSammelkonto"}, template = "${}")
  private boolean istSammelkonto;

  @Adapt(ref = {"Industrieprojekt.mehrwertsteuer"}, template = "${}")
  private Optional<Long> mehrwertsteuer;

}
