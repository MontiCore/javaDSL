package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"CardSetting"}, template = "${}")
public class CardSetting {
  @Adapt(ref = {"CardSetting.page"}, template = "${}")
  private String page;

  @Adapt(ref = {"CardSetting.identifier"}, template = "${}")
  private String identifier;

  @Adapt(ref = {"CardSetting.configuration"}, template = "${}")
  private String configuration;

}
