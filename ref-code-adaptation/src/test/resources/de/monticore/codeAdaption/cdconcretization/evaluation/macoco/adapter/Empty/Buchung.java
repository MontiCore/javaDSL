package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Buchung"}, template = "${}")
public class Buchung extends Buchungseintrag {
  @Adapt(ref = {"Buchung.belegdatum"}, template = "${}")
  private ZonedDateTime belegdatum;

  @Adapt(ref = {"Buchung.zahlungsgrund"}, template = "${}")
  private String zahlungsgrund;

  @Adapt(ref = {"Buchung.kreditorDebitor"}, template = "${}")
  private Optional<String> kreditorDebitor;

  @Adapt(ref = {"Buchung.sachkonto"}, template = "${}")
  private List<String> sachkonto;

  @Adapt(ref = {"Buchung.buchungsdatum"}, template = "${}")
  private Optional<ZonedDateTime> buchungsdatum;

  @Adapt(ref = {"Buchung.status"}, template = "${}")
  private BuchungsStatus status;

  @Adapt(ref = {"Buchung.belegnummern"}, template = "${}")
  private List<String> belegnummern;

  @Adapt(ref = {"Buchung.auftragsnummer"}, template = "${}")
  private List<String> auftragsnummer;

  @Adapt(ref = {"Buchung.bereich"}, template = "${}")
  private Optional<String> bereich;

  @Adapt(ref = {"Buchung.projekt"}, template = "${}")
  private Optional<String> projekt;

  @Adapt(ref = {"Buchung.geschaeftsjahr"}, template = "${}")
  private Optional<ZonedDateTime> geschaeftsjahr;

  @Adapt(ref = {"Buchung.steuer"}, template = "${}")
  private Optional<Long> steuer;

  @Adapt(ref = {"Buchung.skonto"}, template = "${}")
  private Optional<Long> skonto;

  @Adapt(ref = {"Buchung.gez_Skonto"}, template = "${}")
  private Optional<Long> gez_Skonto;

  @Adapt(ref = {"Buchung.erfasser"}, template = "${}")
  private Optional<String> erfasser;

  @Adapt(ref = {"Buchung.belegart"}, template = "${}")
  private Optional<String> belegart;

  @Adapt(ref = {"Buchung.referenz"}, template = "${}")
  private Optional<String> referenz;

  @Adapt(ref = {"Buchung.stornonummer"}, template = "${}")
  private Optional<String> stornonummer;

  @Adapt(ref = {"Buchung.stkz"}, template = "${}")
  private Optional<String> stkz;

  @Adapt(ref = {"Buchung.erfassungsdatum"}, template = "${}")
  private Optional<ZonedDateTime> erfassungsdatum;

  @Adapt(ref = {"Buchung.ausgleichsdatum"}, template = "${}")
  private Optional<ZonedDateTime> ausgleichsdatum;

  @Adapt(ref = {"Buchung.betragCentOriginal"}, template = "${}")
  private long betragCentOriginal;

  @Adapt(ref = {"Buchung.budgetChangedManually"}, template = "${}")
  private boolean budgetChangedManually;

  @Adapt(ref = {"Buchung.kostenarten"}, template = "${}")
  private Optional<String> kostenarten;

  @Adapt(ref = {"Buchung.bezugsdatumJahr"}, template = "${}")
  private Optional<Integer> bezugsdatumJahr;

}
