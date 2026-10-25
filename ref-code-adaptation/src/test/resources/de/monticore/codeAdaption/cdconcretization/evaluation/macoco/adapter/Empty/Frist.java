package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Frist"}, template = "${}")
public class Frist {
  @Adapt(ref = {"Frist.beschreibung"}, template = "${}")
  private String beschreibung;

  @Adapt(ref = {"Frist.status"}, template = "${}")
  private Friststatus status;

  @Adapt(ref = {"Frist.fristobjekt"}, template = "${}")
  private Fristobjekt fristobjekt;

  @Adapt(ref = {"Frist.faelligkeitsdatum"}, template = "${}")
  private ZonedDateTime faelligkeitsdatum;

  @Adapt(ref = {"Frist.notiz"}, template = "${}")
  private Optional<String> notiz;

  @Adapt(ref = {"Frist.manuellerEintrag"}, template = "${}")
  private Boolean manuellerEintrag;

  @Adapt(ref = {"Frist.bezugsobjektId"}, template = "${}")
  private Long bezugsobjektId;

  @Adapt(ref = {"Frist.bezugsobjektName"}, template = "${}")
  private String bezugsobjektName;

  @Adapt(ref = {"Frist.aenderungsdatum"}, template = "${}")
  private Optional<ZonedDateTime> aenderungsdatum;

  @Adapt(ref = {"Frist.istAktiv"}, template = "${}")
  private boolean istAktiv;

}
