package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"StundenzettelHistorieEintrag"}, template = "${}")
public class StundenzettelHistorieEintrag {
  @Adapt(ref = {"StundenzettelHistorieEintrag.bearbeitungsdatum"}, template = "${}")
  private ZonedDateTime bearbeitungsdatum;

  @Adapt(ref = {"StundenzettelHistorieEintrag.editorId"}, template = "${}")
  private Optional<Long> editorId;

  @Adapt(ref = {"StundenzettelHistorieEintrag.editedBy"}, template = "${}")
  private Optional<String> editedBy;

  @Adapt(ref = {"StundenzettelHistorieEintrag.monat"}, template = "${}")
  private String monat;

  @Adapt(ref = {"StundenzettelHistorieEintrag.jahr"}, template = "${}")
  private int jahr;

  @Adapt(ref = {"StundenzettelHistorieEintrag.changeOperation"}, template = "${}")
  private String changeOperation;

}
