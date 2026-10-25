package de.monticore.codeAdaption.evaluation.testcase_15_composite_decorator_combined.concrete;

public class ImageNode implements Renderable {
  String source;

  @Override
  public String render(int depth) {
    return source == null ? "" : source;
  }
}
