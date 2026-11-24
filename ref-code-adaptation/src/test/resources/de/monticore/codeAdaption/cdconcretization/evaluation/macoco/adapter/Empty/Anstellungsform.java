package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Anstellungsform"}, template = "${}")
public class Anstellungsform {
  @Adapt(ref = {"Anstellungsform.erstanstellung"}, template = "${}")
  private boolean erstanstellung;

  @Adapt(ref = {"Anstellungsform.entgeltGruppe"}, template = "${}")
  private Optional<String> entgeltGruppe;

  @Adapt(ref = {"Anstellungsform.erfahrungsStufe"}, template = "${}")
  private Optional<String> erfahrungsStufe;

  @Adapt(ref = {"Anstellungsform.unbefristet"}, template = "${}")
  private boolean unbefristet;

  @Adapt(ref = {"Anstellungsform.anstellungVon"}, template = "${}")
  private ZonedDateTime anstellungVon;

  @Adapt(ref = {"Anstellungsform.anstellungBis"}, template = "${}")
  private Optional<ZonedDateTime> anstellungBis;

  @Adapt(ref = {"Anstellungsform.gehaltCent"}, template = "${}")
  private long gehaltCent;

  @Adapt(ref = {"Anstellungsform.istEigenerReferenzwert"}, template = "${}")
  private boolean istEigenerReferenzwert;

  @Adapt(ref = {"Anstellungsform.kommentar"}, template = "${}")
  private List<String> kommentar;

  @Adapt(ref = {"Anstellungsform.berufsgruppe"}, template = "${}")
  private Optional<String> berufsgruppe;

  @Adapt(ref = {"Anstellungsform.aktion"}, template = "${}")
  private Optional<String> aktion;

  @Adapt(ref = {"Anstellungsform.aktionsDatum"}, template = "${}")
  private Optional<ZonedDateTime> aktionsDatum;

}
