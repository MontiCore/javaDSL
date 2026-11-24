package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Budget"}, template = "${}")
public class Budget {
  @Adapt(ref = {"Budget.typ"}, template = "${}")
  private String typ;

  @Adapt(ref = {"Budget.startDatum"}, template = "${}")
  private Optional<ZonedDateTime> startDatum;

  @Adapt(ref = {"Budget.endDatum"}, template = "${}")
  private Optional<ZonedDateTime> endDatum;

  @Adapt(ref = {"Budget.kommentar"}, template = "${}")
  private Optional<String> kommentar;

  @Adapt(ref = {"Budget.budgetRahmenCent"}, template = "${}")
  private Optional<long> budgetRahmenCent;

  @Adapt(ref = {"Budget.eigenAnteilCent"}, template = "${}")
  private Optional<long> eigenAnteilCent;

  @Adapt(ref = {"Budget.jahresBudgets"}, template = "${}")
  private List<long> jahresBudgets;

  @Adapt(ref = {"Budget.proportionaleVerteilung"}, template = "${}")
  private boolean proportionaleVerteilung;

  @Adapt(ref = {"Budget.forOverheads"}, template = "${}")
  private boolean forOverheads;

  @Adapt(ref = {"Budget.budgetKategorie"}, template = "${}")
  private Optional<String> budgetKategorie;

  @Adapt(ref = {"Budget.budgetDepth"}, template = "${}")
  private int budgetDepth;

}
