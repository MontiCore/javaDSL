package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"InstanzSetting"}, template = "${}")
public class InstanzSetting extends Setting {
  @Adapt(ref = {"InstanzSetting.value"}, template = "${}")
  private String value;

}
