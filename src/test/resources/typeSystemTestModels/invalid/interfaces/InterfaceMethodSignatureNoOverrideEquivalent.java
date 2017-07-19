package typeSystemTestModels.invalid.interfaces;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.lang.String;
import java.lang.Integer;

public interface InterfaceMethodSignatureNoOverrideEquivalent {

  public String getName();

  public String getName();

  public String getName();

  public void setName(String s);

  public void setName(String a);

  public void setList(List<Integer> list, ArrayList<Integer> array);

  public void setList(List<String> list, ArrayList<String> array);

  public <T extends List<?>> T get(T t);

  public <T extends List<String>> T get(T t);

  public void setNaming(List<Integer> n);

  public <T extends List<Integer> & Serializable> void setNaming(T t);

}
