package de.monticore.codeAdaption.cdconcretization.evaluation.visitor.adapter.visitor;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Visitable"}, template = "${}")
public interface Visitable {
  @Adapt(ref = {"Visitable.accept"}, template = "${}")
  void accept(VisitableVisitor visitor);

}
