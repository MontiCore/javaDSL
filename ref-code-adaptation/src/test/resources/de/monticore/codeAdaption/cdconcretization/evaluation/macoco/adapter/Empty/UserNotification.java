package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"UserNotification"}, template = "${}")
public class UserNotification {
  @Adapt(ref = {"UserNotification.notificationType"}, template = "${}")
  private UserNotificationType notificationType;

  @Adapt(ref = {"UserNotification.title"}, template = "${}")
  private String title;

  @Adapt(ref = {"UserNotification.message"}, template = "${}")
  private String message;

  @Adapt(ref = {"UserNotification.timeStamp"}, template = "${}")
  private ZonedDateTime timeStamp;

  @Adapt(ref = {"UserNotification.link"}, template = "${}")
  private Optional<String> link;

  @Adapt(ref = {"UserNotification.seen"}, template = "${}")
  private boolean seen;

  @Adapt(ref = {"UserNotification.pinned"}, template = "${}")
  private boolean pinned;

}
