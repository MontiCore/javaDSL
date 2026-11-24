package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Abwesenheit"}, template = "${}")
public class Abwesenheit {
  @Adapt(ref = {"Abwesenheit.grund"}, template = "${}")
  private Abwesenheitsgrund grund;

  @Adapt(ref = {"Abwesenheit.datumVon"}, template = "${}")
  private ZonedDateTime datumVon;

  @Adapt(ref = {"Abwesenheit.datumBis"}, template = "${}")
  private ZonedDateTime datumBis;

  @Adapt(ref = {"Abwesenheit.kommentar"}, template = "${}")
  private Optional<String> kommentar;

  @Adapt(ref = {"Abwesenheit.uhrzeitVon"}, template = "${}")
  private Optional<ZonedDateTime> uhrzeitVon;

  @Adapt(ref = {"Abwesenheit.uhrzeitBis"}, template = "${}")
  private Optional<ZonedDateTime> uhrzeitBis;

  @Adapt(ref = {"Abwesenheit.minutenProTag"}, template = "${}")
  private Optional<Long> minutenProTag;

  @Adapt(ref = {"Abwesenheit.tageGesamt"}, template = "${}")
  private long tageGesamt;

}
