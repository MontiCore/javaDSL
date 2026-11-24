package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"FilterSetting"}, template = "${}")
public class FilterSetting extends UISetting {
  @Adapt(ref = {"FilterSetting.filterValues"}, template = "${}")
  private List<String> filterValues;

}
