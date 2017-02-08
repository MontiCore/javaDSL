package typeSystemTestModels.invalid.interfaces;

import java.lang.String;
import java.lang.Double;
import java.lang.Integer;

public interface InterfaceMethodsNotAllowedModifiers {

  strictfp Double getDouble();
  native String getString();
  synchronized Integer getInteger();
}