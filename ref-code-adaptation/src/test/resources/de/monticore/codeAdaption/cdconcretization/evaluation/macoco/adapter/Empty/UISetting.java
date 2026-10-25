package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"UISetting"}, template = "${}")
public class UISetting extends Setting {
  @Adapt(ref = {"UISetting.seite"}, template = "${}")
  private String seite;

}
