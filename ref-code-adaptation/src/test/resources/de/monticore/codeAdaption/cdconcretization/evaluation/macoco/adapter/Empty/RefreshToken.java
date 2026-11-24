package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"RefreshToken"}, template = "${}")
public class RefreshToken {
  @Adapt(ref = {"RefreshToken.userId"}, template = "${}")
  private long userId;

  @Adapt(ref = {"RefreshToken.token"}, template = "${}")
  private String token;

}
