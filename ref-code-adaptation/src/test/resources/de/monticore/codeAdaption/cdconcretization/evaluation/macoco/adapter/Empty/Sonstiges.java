package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Sonstiges"}, template = "${}")
public class Sonstiges extends ErweitertesKonto {
  @Adapt(ref = {"Sonstiges.auftraggeber"}, template = "${}")
  private Optional<String> auftraggeber;

  @Adapt(ref = {"Sonstiges.geschaeftsvorgang"}, template = "${}")
  private Geschaeftsvorgang geschaeftsvorgang;

  @Adapt(ref = {"Sonstiges.mehrwertsteuer"}, template = "${}")
  private Optional<Long> mehrwertsteuer;

  @Adapt(ref = {"Sonstiges.istSammelkonto"}, template = "${}")
  private boolean istSammelkonto;

}
