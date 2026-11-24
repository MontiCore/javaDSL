package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"MailAlert"}, template = "${}")
public class MailAlert {
  @Adapt(ref = {"MailAlert.name"}, template = "${}")
  private String name;

  @Adapt(ref = {"MailAlert.alertCondition"}, template = "${}")
  private String alertCondition;

  @Adapt(ref = {"MailAlert.mailBody"}, template = "${}")
  private String mailBody;

  @Adapt(ref = {"MailAlert.repetitions"}, template = "${}")
  private int repetitions;

  @Adapt(ref = {"MailAlert.firstAlert"}, template = "${}")
  private ZonedDateTime firstAlert;

  @Adapt(ref = {"MailAlert.lastAlert"}, template = "${}")
  private ZonedDateTime lastAlert;

  @Adapt(ref = {"MailAlert.receipients"}, template = "${}")
  private Optional<String> receipients;

  @Adapt(ref = {"MailAlert.ccs"}, template = "${}")
  private Optional<String> ccs;

  @Adapt(ref = {"MailAlert.bccs"}, template = "${}")
  private Optional<String> bccs;

  @Adapt(ref = {"MailAlert.a1"}, template = "${}")
  private long a1;

  @Adapt(ref = {"MailAlert.a2"}, template = "${}")
  private long a2;

  @Adapt(ref = {"MailAlert.flag1"}, template = "${}")
  private boolean flag1;

  @Adapt(ref = {"MailAlert.flag2"}, template = "${}")
  private boolean flag2;

  @Adapt(ref = {"MailAlert.s1"}, template = "${}")
  private String s1;

  @Adapt(ref = {"MailAlert.s2"}, template = "${}")
  private String s2;

}
