package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"PersonalExportEinstellungen"}, template = "${}")
public class PersonalExportEinstellungen {
  @Adapt(ref = {"PersonalExportEinstellungen.pspElement"}, template = "${}")
  private String pspElement;

  @Adapt(ref = {"PersonalExportEinstellungen.bezeichnungen"}, template = "${}")
  private List<String> bezeichnungen;

  @Adapt(ref = {"PersonalExportEinstellungen.startmonat"}, template = "${}")
  private ZonedDateTime startmonat;

  @Adapt(ref = {"PersonalExportEinstellungen.endmonat"}, template = "${}")
  private ZonedDateTime endmonat;

}
