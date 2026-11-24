package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"MacocoUser"}, template = "${}")
public class MacocoUser {
  @Adapt(ref = {"MacocoUser.username"}, template = "${}")
  private String username;

  @Adapt(ref = {"MacocoUser.encodedPassword"}, template = "${}")
  private Optional<String> encodedPassword;

  @Adapt(ref = {"MacocoUser.passwordSaltBase64"}, template = "${}")
  private String passwordSaltBase64;

  @Adapt(ref = {"MacocoUser.registrationDate"}, template = "${}")
  private ZonedDateTime registrationDate;

  @Adapt(ref = {"MacocoUser.initials"}, template = "${}")
  private Optional<String> initials;

  @Adapt(ref = {"MacocoUser.activated"}, template = "${}")
  private MacocoUserActivationStatus activated;

  @Adapt(ref = {"MacocoUser.enabled"}, template = "${}")
  private boolean enabled;

  @Adapt(ref = {"MacocoUser.email"}, template = "${}")
  private String email;

  @Adapt(ref = {"MacocoUser.authentifiziert"}, template = "${}")
  private boolean authentifiziert;

  @Adapt(ref = {"MacocoUser.timID"}, template = "${}")
  private Optional<String> timID;

  @Adapt(ref = {"MacocoUser.sapAccessToken"}, template = "${}")
  private Optional<String> sapAccessToken;

  @Adapt(ref = {"MacocoUser.sapRefreshToKen"}, template = "${}")
  private Optional<String> sapRefreshToKen;

}
