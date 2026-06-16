package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"AccessPolicy"}, template = "${}")
public class AccessPolicy {
  @Adapt(ref = {"AccessPolicy.roleName"}, template = "${}")
  private String roleName;

  @Adapt(ref = {"AccessPolicy.objId"}, template = "${}")
  private Optional<Long> objId;

}
