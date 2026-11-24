package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"DruckEinstellungenProjekt"}, template = "${}")
public class DruckEinstellungenProjekt {
  @Adapt(ref = {"DruckEinstellungenProjekt.projektId"}, template = "${}")
  private Long projektId;

  @Adapt(ref = {"DruckEinstellungenProjekt.jahr"}, template = "${}")
  private Integer jahr;

  @Adapt(ref = {"DruckEinstellungenProjekt.monat"}, template = "${}")
  private Integer monat;

  @Adapt(ref = {"DruckEinstellungenProjekt.mittelabrufnummer"}, template = "${}")
  private Optional<String> mittelabrufnummer;

  @Adapt(ref = {"DruckEinstellungenProjekt.belegnummer"}, template = "${}")
  private Optional<String> belegnummer;

}
