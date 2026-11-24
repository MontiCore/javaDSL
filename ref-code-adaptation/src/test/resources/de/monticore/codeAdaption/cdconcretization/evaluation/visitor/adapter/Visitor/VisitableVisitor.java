package de.monticore.codeAdaption.cdconcretization.evaluation.visitor.adapter.visitor;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"VisitableVisitor"}, template = "${}")
public interface VisitableVisitor {
  @Adapt(ref = {"VisitableVisitor.visit(Visitable)"}, template = "${}")
  void visit(Visitable visitable);

  @Adapt(ref = {"VisitableVisitor.visit(ConcreteVisitable)"}, template = "${}")
  void visit(ConcreteVisitable concreteVisitable);

}
