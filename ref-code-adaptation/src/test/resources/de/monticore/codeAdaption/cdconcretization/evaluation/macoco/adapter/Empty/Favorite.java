package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Favorite"}, template = "${}")
public class Favorite {
  @Adapt(ref = {"Favorite.title"}, template = "${}")
  private String title;

  @Adapt(ref = {"Favorite.url"}, template = "${}")
  private String url;

}
