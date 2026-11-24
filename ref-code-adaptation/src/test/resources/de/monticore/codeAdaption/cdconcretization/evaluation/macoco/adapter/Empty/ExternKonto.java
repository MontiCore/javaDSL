package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"ExternKonto"}, template = "${}")
public class ExternKonto extends Konto {
  @Adapt(ref = {"ExternKonto.startDatum"}, template = "${}")
  private Optional<ZonedDateTime> startDatum;

  @Adapt(ref = {"ExternKonto.endDatum"}, template = "${}")
  private Optional<ZonedDateTime> endDatum;

  @Adapt(ref = {"ExternKonto.importVermerk"}, template = "${}")
  private Optional<String> importVermerk;

  @Adapt(ref = {"ExternKonto.abgleichVerhalten"}, template = "${}")
  private AbgleichType abgleichVerhalten;

}
