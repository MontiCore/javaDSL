package de.monticore.codeAdaption.evaluation.testcase_15_composite_decorator_combined.concrete;

public class ShadowDecorator extends RenderDecorator {
  int radius;

  public ShadowDecorator(Renderable wrapped) {
    super(wrapped);
  }

  @Override
  public String render(int depth) {
    return wrapped.render(depth) + "#shadow";
  }
}
