package de.monticore.codeAdaption.evaluation.testcase_15_composite_decorator_combined.concrete;

public abstract class RenderDecorator implements Renderable {
  protected final Renderable wrapped;

  protected RenderDecorator(Renderable wrapped) {
    this.wrapped = wrapped;
  }

  @Override
  public String render(int depth) {
    return wrapped.render(depth);
  }
}
