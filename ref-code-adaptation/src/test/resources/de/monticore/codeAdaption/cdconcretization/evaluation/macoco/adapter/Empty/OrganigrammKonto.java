package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"OrganigrammKonto"}, template = "${}")
public class OrganigrammKonto {
  @Adapt(ref = {"OrganigrammKonto.von"}, template = "${}")
  private Optional<ZonedDateTime> von;

  @Adapt(ref = {"OrganigrammKonto.bis"}, template = "${}")
  private Optional<ZonedDateTime> bis;

  @Adapt(ref = {"OrganigrammKonto.umfang"}, template = "${}")
  private Optional<ZahlenWert> umfang;

}
