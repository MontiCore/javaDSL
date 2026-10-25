package de.monticore.codeAdaption.evaluation.testcase_15_composite_decorator_combined.concrete;

public class TextNode implements Renderable {
  String text;

  @Override
  public String render(int depth) {
    return text == null ? "" : text;
  }
}
