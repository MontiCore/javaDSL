package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Rechnungsstellung"}, template = "${}")
public class Rechnungsstellung extends Buchungseintrag {
  @Adapt(ref = {"Rechnungsstellung.rechnungsdatum"}, template = "${}")
  private Optional<ZonedDateTime> rechnungsdatum;

  @Adapt(ref = {"Rechnungsstellung.rechnungsnummer"}, template = "${}")
  private Optional<String> rechnungsnummer;

  @Adapt(ref = {"Rechnungsstellung.status"}, template = "${}")
  private RechnungsstellungStatus status;

  @Adapt(ref = {"Rechnungsstellung.belegnummern"}, template = "${}")
  private List<String> belegnummern;

  @Adapt(ref = {"Rechnungsstellung.gemeinkosten"}, template = "${}")
  private Optional<Long> gemeinkosten;

  @Adapt(ref = {"Rechnungsstellung.honorierung"}, template = "${}")
  private Optional<Long> honorierung;

  @Adapt(ref = {"Rechnungsstellung.projekt"}, template = "${}")
  private Optional<String> projekt;

  @Adapt(ref = {"Rechnungsstellung.debitor"}, template = "${}")
  private Optional<String> debitor;

  @Adapt(ref = {"Rechnungsstellung.mehrwertsteuer"}, template = "${}")
  private Optional<Long> mehrwertsteuer;

  @Adapt(ref = {"Rechnungsstellung.zahlungsziel"}, template = "${}")
  private Optional<ZonedDateTime> zahlungsziel;

}
