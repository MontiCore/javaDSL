package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Anstellung"}, template = "${}")
public class Anstellung {
  @Adapt(ref = {"Anstellung.bezeichnung"}, template = "${}")
  private Optional<String> bezeichnung;

  @Adapt(ref = {"Anstellung.umfang"}, template = "${}")
  private ZahlenWert umfang;

  @Adapt(ref = {"Anstellung.von"}, template = "${}")
  private ZonedDateTime von;

  @Adapt(ref = {"Anstellung.bis"}, template = "${}")
  private ZonedDateTime bis;

  @Adapt(ref = {"Anstellung.verfaellt"}, template = "${}")
  private boolean verfaellt;

  @Adapt(ref = {"Anstellung.min"}, template = "${}")
  private Optional<Stellentyp> min;

  @Adapt(ref = {"Anstellung.max"}, template = "${}")
  private Optional<Stellentyp> max;

  @Adapt(ref = {"Anstellung.pM"}, template = "${}")
  private long pM;

  @Adapt(ref = {"Anstellung.beschaeftigungsArt"}, template = "${}")
  private BeschaeftigungsArt beschaeftigungsArt;

}
