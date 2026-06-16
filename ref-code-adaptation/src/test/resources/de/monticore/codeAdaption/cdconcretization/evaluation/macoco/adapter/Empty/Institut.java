package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Institut"}, template = "${}")
public class Institut {
  @Adapt(ref = {"Institut.institutsKennZiffer"}, template = "${}")
  private String institutsKennZiffer;

  @Adapt(ref = {"Institut.institutsName"}, template = "${}")
  private String institutsName;

  @Adapt(ref = {"Institut.professorName"}, template = "${}")
  private String professorName;

  @Adapt(ref = {"Institut.lokalInstitut"}, template = "${}")
  private boolean lokalInstitut;

}
