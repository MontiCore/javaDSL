package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"TableSetting"}, template = "${}")
public class TableSetting extends UISetting {
  @Adapt(ref = {"TableSetting.typ"}, template = "${}")
  private TableIdentifierTyp typ;

  @Adapt(ref = {"TableSetting.zeigeInaktive"}, template = "${}")
  private boolean zeigeInaktive;

  @Adapt(ref = {"TableSetting.zeilenLimit"}, template = "${}")
  private int zeilenLimit;

  @Adapt(ref = {"TableSetting.gruppiereNach"}, template = "${}")
  private Optional<String> gruppiereNach;

  @Adapt(ref = {"TableSetting.sortiereNach"}, template = "${}")
  private Optional<String> sortiereNach;

  @Adapt(ref = {"TableSetting.sortiereDir"}, template = "${}")
  private Optional<String> sortiereDir;

  @Adapt(ref = {"TableSetting.isShowColor"}, template = "${}")
  private boolean isShowColor;

}
