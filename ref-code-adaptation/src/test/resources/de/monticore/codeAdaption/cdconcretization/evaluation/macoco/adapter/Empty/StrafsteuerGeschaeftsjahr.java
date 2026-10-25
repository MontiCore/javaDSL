package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"StrafsteuerGeschaeftsjahr"}, template = "${}")
public class StrafsteuerGeschaeftsjahr {
  @Adapt(ref = {"StrafsteuerGeschaeftsjahr.jahr"}, template = "${}")
  private ZonedDateTime jahr;

  @Adapt(ref = {"StrafsteuerGeschaeftsjahr.current"}, template = "${}")
  private boolean current;

}
