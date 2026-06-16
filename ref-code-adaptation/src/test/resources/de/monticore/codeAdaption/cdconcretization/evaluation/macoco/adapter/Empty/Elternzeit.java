package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Elternzeit"}, template = "${}")
public class Elternzeit {
  @Adapt(ref = {"Elternzeit.von"}, template = "${}")
  private Optional<ZonedDateTime> von;

  @Adapt(ref = {"Elternzeit.bis"}, template = "${}")
  private Optional<ZonedDateTime> bis;

  @Adapt(ref = {"Elternzeit.umfang"}, template = "${}")
  private ZahlenWert umfang;

}
