package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Akzeptanz"}, template = "${}")
public class Akzeptanz {
  @Adapt(ref = {"Akzeptanz.akzeptanzstatus"}, template = "${}")
  private Akzeptanzstatus akzeptanzstatus;

  @Adapt(ref = {"Akzeptanz.teilbetragCent"}, template = "${}")
  private Optional<long> teilbetragCent;

  @Adapt(ref = {"Akzeptanz.bearbeitung"}, template = "${}")
  private Optional<ZonedDateTime> bearbeitung;

  @Adapt(ref = {"Akzeptanz.istAktiv"}, template = "${}")
  private boolean istAktiv;

  @Adapt(ref = {"Akzeptanz.kommentar"}, template = "${}")
  private Optional<String> kommentar;

}
