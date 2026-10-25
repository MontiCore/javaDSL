package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"ErweiterbareListen"}, template = "${}")
public class ErweiterbareListen {
  @Adapt(ref = {"ErweiterbareListen.bereich"}, template = "${}")
  private String bereich;

  @Adapt(ref = {"ErweiterbareListen.feldbezeichnung"}, template = "${}")
  private String feldbezeichnung;

  @Adapt(ref = {"ErweiterbareListen.inhalte"}, template = "${}")
  private List<String> inhalte;

}
