package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"ZeitlicheVerbuchungskontoZuordnung"}, template = "${}")
public class ZeitlicheVerbuchungskontoZuordnung {
  @Adapt(ref = {"ZeitlicheVerbuchungskontoZuordnung.programmPauschale"}, template = "${}")
  private Optional<Long> programmPauschale;

  @Adapt(ref = {"ZeitlicheVerbuchungskontoZuordnung.gemeinkostensatz"}, template = "${}")
  private Optional<Long> gemeinkostensatz;

  @Adapt(ref = {"ZeitlicheVerbuchungskontoZuordnung.startDate"}, template = "${}")
  private Optional<ZonedDateTime> startDate;

  @Adapt(ref = {"ZeitlicheVerbuchungskontoZuordnung.endDate"}, template = "${}")
  private Optional<ZonedDateTime> endDate;

  @Adapt(ref = {"ZeitlicheVerbuchungskontoZuordnung.istHauptVerbuchungskonto"}, template = "${}")
  private boolean istHauptVerbuchungskonto;

}
