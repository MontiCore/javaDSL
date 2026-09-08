package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"BlacklistedToken"}, template = "${}")
public class BlacklistedToken {
  @Adapt(ref = {"BlacklistedToken.token"}, template = "${}")
  private String token;

  @Adapt(ref = {"BlacklistedToken.addedAt"}, template = "${}")
  private ZonedDateTime addedAt;

  @Adapt(ref = {"BlacklistedToken.expiresAt"}, template = "${}")
  private ZonedDateTime expiresAt;

}
