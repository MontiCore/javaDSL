package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Begruendung"}, template = "${}")
public class Begruendung {
  @Adapt(ref = {"Begruendung.text"}, template = "${}")
  private String text;

  @Adapt(ref = {"Begruendung.erstellung"}, template = "${}")
  private ZonedDateTime erstellung;

  @Adapt(ref = {"Begruendung.letzteBearbeitung"}, template = "${}")
  private Optional<ZonedDateTime> letzteBearbeitung;

  @Adapt(ref = {"Begruendung.geschaeftsjahr"}, template = "${}")
  private Optional<StrafsteuerGeschaeftsjahr> geschaeftsjahr;

}
