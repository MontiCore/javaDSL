package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"OrganigrammPersonInfo"}, template = "${}")
public class OrganigrammPersonInfo {
  @Adapt(ref = {"OrganigrammPersonInfo.von"}, template = "${}")
  private Optional<ZonedDateTime> von;

  @Adapt(ref = {"OrganigrammPersonInfo.bis"}, template = "${}")
  private Optional<ZonedDateTime> bis;

  @Adapt(ref = {"OrganigrammPersonInfo.umfang"}, template = "${}")
  private Optional<ZahlenWert> umfang;

}
