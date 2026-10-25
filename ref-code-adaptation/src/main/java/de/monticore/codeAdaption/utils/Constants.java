package de.monticore.codeAdaption.utils;

/** Names and placeholder syntax shared by {@link Adapt} parsing and matcher templates. */
public final class Constants {

  private Constants() {}

  public static final String TEMPLATE = "template";
  public static final String REFERENCE = "ref";
  public static final String ANNOT_NAME = "Adapt";
  public static final String ANNOT_PACKAGE = "de.monticore.codeAdaption.utils.Adapt";
  public static final String IGNORE = "ignore";
  public static final String CAP_FIRST = "cap_first";
  public static final String UNCAP_FIRST = "uncap_first";

  public static final String PLACE_HOLDER_REGEX = "\\$\\{(.*?)}";
  public static final String SIMPLE_PLACE_HOLDER = "${}";
  public static final String CAP_FIRST_PLACE_HOLDER = "${" + CAP_FIRST + "}";
  public static final String UNCAP_FIRST_PLACE_HOLDER = "${" + UNCAP_FIRST + "}";
  public static final String GENERATE_TEMPLATE = "genTemplate";
}
