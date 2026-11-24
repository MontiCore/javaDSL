package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Auftragsprojekt"}, template = "${}")
public class Auftragsprojekt extends Projekt {
  @Adapt(ref = {"Auftragsprojekt.nummer"}, template = "${}")
  private Optional<String> nummer;

  @Adapt(ref = {"Auftragsprojekt.typ"}, template = "${}")
  private ProjektTyp typ;

  @Adapt(ref = {"Auftragsprojekt.regelung"}, template = "${}")
  private Optional<String> regelung;

}
