package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"UrlaubsGenehmigung"}, template = "${}")
public class UrlaubsGenehmigung {
  @Adapt(ref = {"UrlaubsGenehmigung.beantragt"}, template = "${}")
  private Optional<ZonedDateTime> beantragt;

  @Adapt(ref = {"UrlaubsGenehmigung.genehmigt"}, template = "${}")
  private Optional<ZonedDateTime> genehmigt;

  @Adapt(ref = {"UrlaubsGenehmigung.geprueft"}, template = "${}")
  private Optional<ZonedDateTime> geprueft;

  @Adapt(ref = {"UrlaubsGenehmigung.storniert"}, template = "${}")
  private Optional<ZonedDateTime> storniert;

  @Adapt(ref = {"UrlaubsGenehmigung.status"}, template = "${}")
  private Urlaubssstatus status;

  @Adapt(ref = {"UrlaubsGenehmigung.stornierungsStatus"}, template = "${}")
  private Stornierungsstatus stornierungsStatus;

}
