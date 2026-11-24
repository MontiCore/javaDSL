package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"DruckEinstellungenPerson"}, template = "${}")
public class DruckEinstellungenPerson {
  @Adapt(ref = {"DruckEinstellungenPerson.personId"}, template = "${}")
  private Long personId;

  @Adapt(ref = {"DruckEinstellungenPerson.stundensatz"}, template = "${}")
  private Optional<ZahlenWert> stundensatz;

}
