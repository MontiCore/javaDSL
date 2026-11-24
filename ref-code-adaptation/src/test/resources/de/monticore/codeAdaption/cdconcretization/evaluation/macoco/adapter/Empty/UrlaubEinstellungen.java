package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"UrlaubEinstellungen"}, template = "${}")
public class UrlaubEinstellungen {
  @Adapt(ref = {"UrlaubEinstellungen.urlaubsanspruchInStunden"}, template = "${}")
  private boolean urlaubsanspruchInStunden;

}
