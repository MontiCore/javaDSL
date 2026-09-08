package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Buchungseintrag"}, template = "${}")
public class Buchungseintrag {
  @Adapt(ref = {"Buchungseintrag.datum"}, template = "${}")
  private ZonedDateTime datum;

  @Adapt(ref = {"Buchungseintrag.buchungseintragText"}, template = "${}")
  private Optional<String> buchungseintragText;

  @Adapt(ref = {"Buchungseintrag.betragCent"}, template = "${}")
  private long betragCent;

  @Adapt(ref = {"Buchungseintrag.istAktiv"}, template = "${}")
  private boolean istAktiv;

  @Adapt(ref = {"Buchungseintrag.aenderungsdatum"}, template = "${}")
  private Optional<ZonedDateTime> aenderungsdatum;

  @Adapt(ref = {"Buchungseintrag.gueltigerGeschaeftsvorgang"}, template = "${}")
  private Geschaeftsvorgang gueltigerGeschaeftsvorgang;

  @Adapt(ref = {"Buchungseintrag.lfdeNummer"}, template = "${}")
  private String lfdeNummer;

}
