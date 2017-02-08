package typeSystemTestModels.valid.interfaces;

import java.lang.Integer;
import java.lang.String;
import java.lang.Double;

public interface InterfaceMethodsNotAllowedModifiers {
  public String getString();
  private Double getDouble();
  protected Integer getInteger();
}