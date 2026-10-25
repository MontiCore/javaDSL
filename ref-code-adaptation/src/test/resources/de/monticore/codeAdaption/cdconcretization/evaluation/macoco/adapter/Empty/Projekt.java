package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Projekt"}, template = "${}")
public class Projekt {
  @Adapt(ref = {"Projekt.name"}, template = "${}")
  private String name;

  @Adapt(ref = {"Projekt.kuerzel"}, template = "${}")
  private String kuerzel;

  @Adapt(ref = {"Projekt.minPM"}, template = "${}")
  private Optional<Long> minPM;

  @Adapt(ref = {"Projekt.maxPM"}, template = "${}")
  private Optional<Long> maxPM;

  @Adapt(ref = {"Projekt.finanzPM"}, template = "${}")
  private Optional<Long> finanzPM;

  @Adapt(ref = {"Projekt.laufzeitVon"}, template = "${}")
  private Optional<ZonedDateTime> laufzeitVon;

  @Adapt(ref = {"Projekt.laufzeitBis"}, template = "${}")
  private Optional<ZonedDateTime> laufzeitBis;

  @Adapt(ref = {"Projekt.kommentar"}, template = "${}")
  private Optional<String> kommentar;

  @Adapt(ref = {"Projekt.foerdergeberAZ"}, template = "${}")
  private Optional<String> foerdergeberAZ;

  @Adapt(ref = {"Projekt.status"}, template = "${}")
  private ProjektStatus status;

  @Adapt(ref = {"Projekt.istAktiv"}, template = "${}")
  private boolean istAktiv;

  @Adapt(ref = {"Projekt.lockedForStundenzettel"}, template = "${}")
  private boolean lockedForStundenzettel;

  @Adapt(ref = {"Projekt.lockedMonths"}, template = "${}")
  private List<ZonedDateTime> lockedMonths;

  @Adapt(ref = {"Projekt.aenderungsdatum"}, template = "${}")
  private Optional<ZonedDateTime> aenderungsdatum;

}
