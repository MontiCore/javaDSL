package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Person"}, template = "${}")
public class Person {
  @Adapt(ref = {"Person.vorname"}, template = "${}")
  private String vorname;

  @Adapt(ref = {"Person.vorsatzwort"}, template = "${}")
  private Optional<String> vorsatzwort;

  @Adapt(ref = {"Person.nachname"}, template = "${}")
  private String nachname;

  @Adapt(ref = {"Person.kuerzel"}, template = "${}")
  private String kuerzel;

  @Adapt(ref = {"Person.personalnummer"}, template = "${}")
  private Optional<String> personalnummer;

  @Adapt(ref = {"Person.gebDatum"}, template = "${}")
  private Optional<ZonedDateTime> gebDatum;

  @Adapt(ref = {"Person.beschBeginn"}, template = "${}")
  private Optional<ZonedDateTime> beschBeginn;

  @Adapt(ref = {"Person.beschEnde"}, template = "${}")
  private Optional<ZonedDateTime> beschEnde;

  @Adapt(ref = {"Person.kommentar"}, template = "${}")
  private List<String> kommentar;

  @Adapt(ref = {"Person.istAktiv"}, template = "${}")
  private boolean istAktiv;

  @Adapt(ref = {"Person.istStundenzettelpflichtig"}, template = "${}")
  private boolean istStundenzettelpflichtig;

  @Adapt(ref = {"Person.stundenzettelpflichtigVon"}, template = "${}")
  private Optional<ZonedDateTime> stundenzettelpflichtigVon;

  @Adapt(ref = {"Person.stundenzettelpflichtigBis"}, template = "${}")
  private Optional<ZonedDateTime> stundenzettelpflichtigBis;

  @Adapt(ref = {"Person.ueberStundenUebertrag"}, template = "${}")
  private Optional<Long> ueberStundenUebertrag;

  @Adapt(ref = {"Person.darfWochenendarbeiten"}, template = "${}")
  private boolean darfWochenendarbeiten;

  @Adapt(ref = {"Person.hoechstbeschaeftigungBis"}, template = "${}")
  private Optional<ZonedDateTime> hoechstbeschaeftigungBis;

  @Adapt(ref = {"Person.staatsangehoerigkeiten"}, template = "${}")
  private List<String> staatsangehoerigkeiten;

  @Adapt(ref = {"Person.telefonnummer"}, template = "${}")
  private Optional<String> telefonnummer;

  @Adapt(ref = {"Person.arbeitserlaubnisBis"}, template = "${}")
  private Optional<ZonedDateTime> arbeitserlaubnisBis;

  @Adapt(ref = {"Person.extern"}, template = "${}")
  private boolean extern;

  @Adapt(ref = {"Person.titel"}, template = "${}")
  private Optional<String> titel;

  @Adapt(ref = {"Person.rufname"}, template = "${}")
  private Optional<String> rufname;

  @Adapt(ref = {"Person.adresse"}, template = "${}")
  private Optional<String> adresse;

  @Adapt(ref = {"Person.email"}, template = "${}")
  private Optional<String> email;

  @Adapt(ref = {"Person.geburtsname"}, template = "${}")
  private Optional<String> geburtsname;

  @Adapt(ref = {"Person.geschlecht"}, template = "${}")
  private Optional<String> geschlecht;

  @Adapt(ref = {"Person.lBVNummer"}, template = "${}")
  private Optional<String> lBVNummer;

  @Adapt(ref = {"Person.entfristung"}, template = "${}")
  private Optional<ZonedDateTime> entfristung;

  @Adapt(ref = {"Person.qualifikationstitel"}, template = "${}")
  private Optional<String> qualifikationstitel;

  @Adapt(ref = {"Person.speicherungGewuenscht"}, template = "${}")
  private boolean speicherungGewuenscht;

  @Adapt(ref = {"Person.urlaubskommentar"}, template = "${}")
  private Optional<String> urlaubskommentar;

  @Adapt(ref = {"Person.aenderungsdatum"}, template = "${}")
  private Optional<ZonedDateTime> aenderungsdatum;

}
