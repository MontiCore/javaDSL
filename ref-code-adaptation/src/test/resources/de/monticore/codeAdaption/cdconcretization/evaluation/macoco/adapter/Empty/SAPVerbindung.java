package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"SAPVerbindung"}, template = "${}")
public class SAPVerbindung {
  @Adapt(ref = {"SAPVerbindung.status"}, template = "${}")
  private SAPverbindungsStatus status;

}
