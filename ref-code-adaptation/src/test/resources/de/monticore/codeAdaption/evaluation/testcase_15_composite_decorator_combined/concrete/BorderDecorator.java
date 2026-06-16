package de.monticore.codeAdaption.evaluation.testcase_15_composite_decorator_combined.concrete;

public class BorderDecorator extends RenderDecorator {
  String borderStyle;

  public BorderDecorator(Renderable wrapped) {
    super(wrapped);
  }

  @Override
  public String render(int depth) {
    return "[" + wrapped.render(depth) + "]";
  }
}
